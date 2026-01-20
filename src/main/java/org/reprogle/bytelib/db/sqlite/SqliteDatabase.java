package org.reprogle.bytelib.db.sqlite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.db.api.*;
import org.reprogle.bytelib.db.api.Database;
import org.reprogle.bytelib.db.api.DatabaseTx;
import org.reprogle.bytelib.db.api.RowMapper;
import org.reprogle.bytelib.db.api.TransactionCallback;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.exceptions.DbMainThreadDisallowedException;
import org.reprogle.bytelib.db.api.exceptions.DbTimeoutException;

import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public final class SqliteDatabase implements Database {
    private final JavaPlugin plugin;
    private final Path dbFile;
    private final ExecutorService executor;
    private final SqliteConfig config;

    public SqliteDatabase(JavaPlugin plugin, Path dbFile, SqliteConfig config) {
        this.plugin = Objects.requireNonNull(plugin);
        this.dbFile = Objects.requireNonNull(dbFile);
        this.config = Objects.requireNonNull(config);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "bytelib-sqlite-" + dbFile.getFileName());
            t.setDaemon(true);
            return t;
        });

        // warm up
        runOnDbThread(() -> {
            try (Connection conn = openConnection()) {
                applyPragmas(conn);
            }
            return null;
        });
    }

    // ----------------------
    // Unguarded “sync semantics”
    // ----------------------

    @Override
    public int execute(String sql, Param<?>... params) {
        return executeBlocking(sql, new BlockingOptions(Duration.ZERO, MainThreadPolicy.ALLOW, TimeoutBehavior.THROW, Duration.ZERO), params);
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params) {
        return queryBlocking(sql, mapper, new BlockingOptions(Duration.ZERO, MainThreadPolicy.ALLOW, TimeoutBehavior.THROW, Duration.ZERO), params);
    }

    @Override
    public <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params) {
        var list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.getFirst();
    }

    // ----------------------
    // Guarded blocking (event-safe-ish)
    // ----------------------

    @Override
    public int executeBlocking(String sql, BlockingOptions options, Param<?>... params) {
        return guardedBlock("execute", options, () ->
                runSql(conn -> {
                    applyPragmas(conn);
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        bind(ps, params);
                        return ps.executeUpdate();
                    }
                })
        );
    }

    @Override
    public <T> List<T> queryBlocking(String sql, RowMapper<T> mapper, BlockingOptions options, Param<?>... params) {
        return guardedBlock("query", options, () ->
                runSql(conn -> {
                    applyPragmas(conn);
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        bind(ps, params);
                        try (ResultSet rs = ps.executeQuery()) {
                            List<T> out = new ArrayList<>();
                            while (rs.next()) out.add(mapper.map(new Row(rs)));
                            return out;
                        }
                    }
                })
        );
    }

    @Override
    public <T> T queryOneBlocking(String sql, RowMapper<T> mapper, BlockingOptions options, Param<?>... params) {
        List<T> list = queryBlocking(sql, mapper, options, params);
        return list.isEmpty() ? null : list.getFirst();
    }

    // ----------------------
    // Transactions
    // ----------------------

    @Override
    public <T> T transaction(TransactionCallback<T> work) {
        return runOnDbThread(() -> {
            try (Connection conn = openConnection()) {
                applyPragmas(conn);

                boolean prev = conn.getAutoCommit();
                conn.setAutoCommit(false);

                try {
                    DatabaseTx tx = new TxImpl(conn);
                    T result = work.run(tx);
                    conn.commit();
                    return result;
                } catch (Exception e) {
                    conn.rollback();
                    throw wrap(e);
                } finally {
                    conn.setAutoCommit(prev);
                }
            }
        });
    }

    // ----------------------
    // Async
    // ----------------------

    @Override
    public CompletableFuture<Integer> executeAsync(String sql, Param<?>... params) {
        return CompletableFuture.supplyAsync(() -> execute(sql, params), executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> queryAsync(String sql, RowMapper<T> mapper, Param<?>... params) {
        return CompletableFuture.supplyAsync(() -> query(sql, mapper, params), executor);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    // ----------------------
    // Guarding + blocking core
    // ----------------------

    private <T> T guardedBlock(String opName, BlockingOptions options, Callable<T> call) {
        boolean main = Bukkit.isPrimaryThread();
        if (main) {
            switch (options.mainThreadPolicy()) {
                case DISALLOW -> throw new DbMainThreadDisallowedException("DB " + opName + " called on main thread");
                case WARN -> {
                    // no-op here; we’ll log duration below
                }
                case ALLOW -> {
                }
            }
        }

        long startNanos = System.nanoTime();
        try {
            if (options.timeout().isZero() || options.timeout().isNegative()) {
                T result = call.call();
                logSlowIfNeeded(main, options, opName, startNanos);
                return result;
            }

            Future<T> f = executor.submit(call);
            try {
                T result = f.get(options.timeout().toMillis(), TimeUnit.MILLISECONDS);
                logSlowIfNeeded(main, options, opName, startNanos);
                return result;
            } catch (TimeoutException te) {
                f.cancel(true);
                return onTimeout(options, opName, te);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private <T> T onTimeout(BlockingOptions options, String opName, TimeoutException te) {
        switch (options.timeoutBehavior()) {
            case FAIL_OPEN -> {
                return null; // caller decides default
            }
            case FAIL_CLOSED, THROW ->
                    throw new DbTimeoutException("DB " + opName + " timed out after " + options.timeout().toMillis() + "ms", te);
            default -> throw new DbTimeoutException("DB " + opName + " timed out", te);
        }
    }

    private void logSlowIfNeeded(boolean main, BlockingOptions options, String opName, long startNanos) {
        if (!main) return;
        if (options.mainThreadPolicy() != MainThreadPolicy.WARN) return;
        if (options.slowQueryWarnThreshold().isZero() || options.slowQueryWarnThreshold().isNegative()) return;

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        if (elapsedMs >= options.slowQueryWarnThreshold().toMillis()) {
            plugin.getLogger().warning("[ByteLib-DB] Slow main-thread DB " + opName + ": " + elapsedMs + "ms");
        }
    }

    // ----------------------
    // JDBC internals
    // ----------------------

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
    }

    private void applyPragmas(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            if (config.foreignKeys()) st.execute("PRAGMA foreign_keys=ON;");
            st.execute("PRAGMA busy_timeout=" + config.busyTimeoutMs() + ";");
            st.execute("PRAGMA journal_mode=" + config.journalMode() + ";");
            st.execute("PRAGMA synchronous=" + config.synchronous() + ";");
        }
    }

    private <T> T runSql(SqlWork<T> work) {
        return runOnDbThread(() -> {
            try (Connection conn = openConnection()) {
                return work.run(conn);
            }
        });
    }

    private <T> T runOnDbThread(Callable<T> work) {
        try {
            return executor.submit(work).get();
        } catch (ExecutionException e) {
            throw wrap(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw wrap(e);
        }
    }

    private static void bind(PreparedStatement ps, Param<?>... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            Param<?> p = params[i];
            if (p == null) throw new SQLException("Null Param at index " + i);
            p.bind(ps, i + 1);
        }
    }

    private static RuntimeException wrap(Throwable t) {
        return (t instanceof RuntimeException re) ? re : new RuntimeException(t);
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection conn) throws Exception;
    }

    private record TxImpl(Connection conn) implements DatabaseTx {

        @Override
        public int execute(String sql, Param<?>... params) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            } catch (Exception e) {
                throw wrap(e);
            }
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> out = new ArrayList<>();
                    while (rs.next()) out.add(mapper.map(new Row(rs)));
                    return out;
                }
            } catch (Exception e) {
                throw wrap(e);
            }
        }

        @Override
        public <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params) {
            var l = query(sql, mapper, params);
            return l.isEmpty() ? null : l.getFirst();
        }
    }
}