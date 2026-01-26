package org.reprogle.bytelib.db.sqlite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.Row;
import org.reprogle.bytelib.db.api.RowMapper;
import org.reprogle.bytelib.db.api.SqlType;
import org.reprogle.bytelib.db.api.Table;
import org.reprogle.bytelib.db.api.exceptions.DbMainThreadDisallowedException;
import org.reprogle.bytelib.db.api.exceptions.DbTimeoutException;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public final class SqliteDatabase implements AutoCloseable {
    private final JavaPlugin plugin;
    private final Path dbFile;
    private final ExecutorService executor;
    private final SqliteConfig config;
    private final SqliteQueryCache cache;
    private volatile Thread dbThread;

    /**
     * Creates a new instance SqliteDatabase, creating a new Executor, new cache, and setting up the DB for use.
     * This should not be instantiated manually, but rather should be created via {@link SqliteModule}.
     *
     * @param plugin An instance of JavaPlugin, which should be the plugin that was booted by this library
     * @param dbFile The path you want your Database file to be written at. Should include an extension
     * @param config The configuration for the SqliteDatabase.
     */
    public SqliteDatabase(JavaPlugin plugin, Path dbFile, SqliteConfig config) {
        this.plugin = Objects.requireNonNull(plugin);
        this.dbFile = Objects.requireNonNull(dbFile);
        this.config = Objects.requireNonNull(config);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "bytelib-sqlite-" + dbFile.getFileName());
            t.setDaemon(true);
            return t;
        });

        this.cache = new SqliteQueryCache(config, executor);

        initDbThread();

        runOnDbThread(() -> {
            try (Connection conn = openConnection()) {
                applyPragmas(conn);
            }
            return null;
        });
    }

    // ----------------------
    // Raw SQL
    // ----------------------

    public int execute(String sql, Param<?>... params) {
        Objects.requireNonNull(sql, "sql");
        Integer result = blockingCall("execute", () ->
                runSql(conn -> {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        bind(ps, params);
                        return ps.executeUpdate();
                    }
                })
        );

        if (result == null) result = 0;
        cache.invalidateForWrite(sql);
        return result;
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params) {
        Objects.requireNonNull(sql, "sql");
        Objects.requireNonNull(mapper, "mapper");
        return cache.query(sql, mapper, params, this::loadQueryBlocking);
    }

    public <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params) {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.getFirst();
    }

    public CompletableFuture<Integer> executeAsync(String sql, Param<?>... params) {
        return CompletableFuture.supplyAsync(() -> execute(sql, params), executor);
    }

    public <T> CompletableFuture<List<T>> queryAsync(String sql, RowMapper<T> mapper, Param<?>... params) {
        return CompletableFuture.supplyAsync(() -> query(sql, mapper, params), executor);
    }

    // ----------------------
    // CRUD helpers
    // ----------------------

    public int insert(Table table, Map<Table.Column<?>, ?> values) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) throw new IllegalArgumentException("values is empty");

        StringJoiner cols = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        List<Param<?>> params = new ArrayList<>(values.size());

        for (Map.Entry<Table.Column<?>, ?> entry : values.entrySet()) {
            Table.Column<?> col = Objects.requireNonNull(entry.getKey(), "column");
            cols.add(col.name());
            placeholders.add("?");
            params.add(paramFor(col, entry.getValue()));
        }

        String sql = "INSERT INTO " + table.name() + " (" + cols + ") VALUES (" + placeholders + ")";
        return execute(sql, params.toArray(new Param<?>[0]));
    }

    public int update(Table table, Map<Table.Column<?>, ?> values, String whereSql, Param<?>... whereParams) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) throw new IllegalArgumentException("values is empty");

        StringJoiner set = new StringJoiner(", ");
        List<Param<?>> params = new ArrayList<>(values.size() + (whereParams == null ? 0 : whereParams.length));

        for (Map.Entry<Table.Column<?>, ?> entry : values.entrySet()) {
            Table.Column<?> col = Objects.requireNonNull(entry.getKey(), "column");
            set.add(col.name() + " = ?");
            params.add(paramFor(col, entry.getValue()));
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(table.name()).append(" SET ").append(set);
        if (whereSql != null && !whereSql.isBlank()) {
            sql.append(" WHERE ").append(whereSql);
            if (whereParams != null && whereParams.length > 0) {
                params.addAll(Arrays.asList(whereParams));
            }
        }

        return execute(sql.toString(), params.toArray(new Param<?>[0]));
    }

    public int delete(Table table, String whereSql, Param<?>... params) {
        Objects.requireNonNull(table, "table");
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table.name());
        if (whereSql != null && !whereSql.isBlank()) {
            sql.append(" WHERE ").append(whereSql);
        }
        return execute(sql.toString(), params);
    }

    public <T> List<T> selectAll(Table table, RowMapper<T> mapper) {
        Objects.requireNonNull(table, "table");
        return query("SELECT * FROM " + table.name(), mapper);
    }

    public <T> List<T> selectWhere(Table table, String whereSql, RowMapper<T> mapper, Param<?>... params) {
        Objects.requireNonNull(table, "table");
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table.name());
        if (whereSql != null && !whereSql.isBlank()) {
            sql.append(" WHERE ").append(whereSql);
        }
        return query(sql.toString(), mapper, params);
    }

    // ----------------------
    // Transactions
    // ----------------------

    public <T> T transaction(Transaction<T> work) {
        Objects.requireNonNull(work, "work");
        return blockingCall("transaction", () -> {
            try (Connection conn = openConnection()) {
                applyPragmas(conn);

                boolean prev = conn.getAutoCommit();
                conn.setAutoCommit(false);

                TxImpl tx = new TxImpl(conn);
                boolean committed = false;
                try {
                    T result = work.run(tx);
                    conn.commit();
                    committed = true;
                    return result;
                } catch (Exception e) {
                    conn.rollback();
                    throw wrap(e);
                } finally {
                    conn.setAutoCommit(prev);
                    if (committed) {
                        cache.invalidateAfterTransaction(tx.touchedTables, tx.clearAllOnCommit);
                    }
                }
            }
        });
    }

    public interface Tx {
        @SuppressWarnings("UnusedReturnValue")
        int execute(String sql, Param<?>... params);

        <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params);

        <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params);
    }

    @FunctionalInterface
    public interface Transaction<T> {
        T run(Tx tx) throws Exception;
    }

    // ----------------------
    // Guarding + blocking core
    // ----------------------

    private <T> T blockingCall(String opName, Callable<T> call) throws RuntimeException {
        boolean main = Bukkit.isPrimaryThread();
        if (main) {
            switch (config.mainThreadPolicy()) {
                case DISALLOW -> throw new DbMainThreadDisallowedException("DB " + opName + " called on main thread");
                case WARN, ALLOW -> {
                }
            }
        }

        long startNanos = System.nanoTime();
        try {
            Duration timeout = main ? config.mainThreadTimeout() : null;
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                T result = runOnDbThread(call);
                logSlowIfNeeded(main, opName, startNanos);
                return result;
            }

            Future<T> f = executor.submit(call);
            try {
                T result = f.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                logSlowIfNeeded(true, opName, startNanos);
                return result;
            } catch (TimeoutException te) {
                f.cancel(true);
                return onTimeout(opName, te);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private <T> T onTimeout(String opName, TimeoutException te) {
        switch (config.timeoutBehavior()) {
            case FAIL_OPEN -> {
                return null;
            }
            case FAIL_CLOSED, THROW ->
                    throw new DbTimeoutException("DB " + opName + " timed out after " + config.mainThreadTimeout().toMillis() + "ms", te);
            default -> throw new DbTimeoutException("DB " + opName + " timed out", te);
        }
    }

    private void logSlowIfNeeded(boolean main, String opName, long startNanos) {
        if (!main) return;
        if (config.mainThreadPolicy() != SqliteConfig.MainThreadPolicy.WARN) return;
        if (config.slowQueryWarnThreshold() == null || config.slowQueryWarnThreshold().isZero() || config.slowQueryWarnThreshold().isNegative())
            return;

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        if (elapsedMs >= config.slowQueryWarnThreshold().toMillis()) {
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

    private <T> T runSql(SqlWork<T> work) throws RuntimeException {
        return runOnDbThread(() -> {
            try (Connection conn = openConnection()) {
                applyPragmas(conn);
                return work.run(conn);
            }
        });
    }

    private <T> T runOnDbThread(Callable<T> work) {
        try {
            if (Thread.currentThread() == dbThread) {
                return work.call();
            }
            return executor.submit(work).get();
        } catch (ExecutionException e) {
            throw wrap(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw wrap(e);
        } catch (Exception e) {
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

    @SuppressWarnings("unchecked")
    private static Param<?> paramFor(Table.Column<?> col, Object value) {
        SqlType<Object> type = (SqlType<Object>) col.type();
        return new Param<>(type, value);
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection conn) throws Exception;
    }

    private void initDbThread() {
        try {
            executor.submit(() -> dbThread = Thread.currentThread()).get();
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void invalidateTable(String tableName) {
        cache.invalidateTable(tableName);
    }

    private <T> List<T> loadQuery(String sql, RowMapper<T> mapper, Param<?>... params) throws RuntimeException {
        return runSql(conn -> queryOnConnection(conn, sql, mapper, params));
    }

    private <T> List<T> queryOnConnection(Connection conn, String sql, RowMapper<T> mapper, Param<?>... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(new Row(rs)));
                return out;
            }
        }
    }

    private <T> List<T> loadQueryBlocking(String sql, RowMapper<T> mapper, Param<?>... params) throws RuntimeException {
        List<T> result = blockingCall("query", () -> loadQuery(sql, mapper, params));
        return result == null ? List.of() : result;
    }

    private final class TxImpl implements Tx {
        private final Connection conn;
        private final Set<String> touchedTables = ConcurrentHashMap.newKeySet();
        private boolean clearAllOnCommit;

        private TxImpl(Connection conn) {
            this.conn = conn;
        }

        @Override
        public int execute(String sql, Param<?>... params) {
            recordTable(sql);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            } catch (Exception e) {
                throw wrap(e);
            }
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params) {
            try {
                return queryOnConnection(conn, sql, mapper, params);
            } catch (Exception e) {
                throw wrap(e);
            }
        }

        @Override
        public <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params) {
            List<T> list = query(sql, mapper, params);
            return list.isEmpty() ? null : list.getFirst();
        }

        private void recordTable(String sql) {
            String table = SqliteQueryCache.extractTableName(sql);
            if (table == null) {
                clearAllOnCommit = true;
                return;
            }
            touchedTables.add(table);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
