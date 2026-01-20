package org.reprogle.bytelib.db.sqlite;

import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.RowMapper;
import org.reprogle.bytelib.db.api.SqlType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SqliteQueryCache {
    private static final Pattern INSERT_TABLE = Pattern.compile("(?i)\\binsert\\s+into\\s+([\\w_.]+)");
    private static final Pattern UPDATE_TABLE = Pattern.compile("(?i)\\bupdate\\s+([\\w_.]+)");
    private static final Pattern DELETE_TABLE = Pattern.compile("(?i)\\bdelete\\s+from\\s+([\\w_.]+)");

    private final SqliteConfig config;
    private final Executor executor;
    private final ConcurrentHashMap<QueryKey, CacheEntry> queryCache = new ConcurrentHashMap<>();

    SqliteQueryCache(SqliteConfig config, Executor executor) {
        this.config = Objects.requireNonNull(config, "config");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    interface QueryLoader {
        <T> List<T> load(String sql, RowMapper<T> mapper, Param<?>... params) throws Exception;
    }

    <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>[] params, QueryLoader loader) {
        SqliteConfig.CacheConfig cache = config.cache();
        if (cache.maxSize() <= 0) {
            return load(loader, sql, mapper, params);
        }

        QueryKey key = QueryKey.of(sql, mapper, params);
        long now = System.nanoTime();
        CacheEntry cached = queryCache.get(key);

        if (cached != null && !cached.isExpired(now, cache.ttl())) {
            maybeRefresh(key, cached, now, false, sql, mapper, params, loader);
            return castList(cached.value);
        }

        if (cached != null && cache.serveStaleWhileRefreshing()) {
            maybeRefresh(key, cached, now, true, sql, mapper, params, loader);
            return castList(cached.value);
        }

        List<T> loaded = load(loader, sql, mapper, params);
        queryCache.put(key, CacheEntry.fresh(loaded, now));
        evictIfNeeded();
        return loaded;
    }

    void invalidateForWrite(String sql) {
        String table = extractTableName(sql);
        if (table == null) {
            invalidateAll();
            return;
        }
        invalidateTable(table);
    }

    void invalidateAfterTransaction(Set<String> touchedTables, boolean clearAllOnCommit) {
        if (clearAllOnCommit) {
            invalidateAll();
            return;
        }
        if (touchedTables == null || touchedTables.isEmpty()) return;
        for (String table : touchedTables) {
            invalidateTable(table);
        }
    }

    void invalidateAll() {
        queryCache.clear();
    }

    void invalidateTable(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            invalidateAll();
            return;
        }
        String tableLower = tableName.toLowerCase(Locale.ROOT);
        Pattern tablePattern = Pattern.compile("\\b" + Pattern.quote(tableLower) + "\\b");
        queryCache.keySet().removeIf(key -> tablePattern.matcher(key.sqlLower).find());
    }

    static String extractTableName(String sql) {
        if (sql == null) return null;
        String s = sql.trim();
        Matcher insert = INSERT_TABLE.matcher(s);
        if (insert.find()) return insert.group(1);
        Matcher update = UPDATE_TABLE.matcher(s);
        if (update.find()) return update.group(1);
        Matcher delete = DELETE_TABLE.matcher(s);
        if (delete.find()) return delete.group(1);
        return null;
    }

    private static <T> List<T> load(QueryLoader loader, String sql, RowMapper<T> mapper, Param<?>[] params) {
        try {
            return loader.load(sql, mapper, params);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private void evictIfNeeded() {
        int maxSize = config.cache().maxSize();
        if (maxSize <= 0) return;
        int size = queryCache.size();
        if (size <= maxSize) return;

        int toRemove = size - maxSize;
        Iterator<QueryKey> it = queryCache.keySet().iterator();
        while (toRemove > 0 && it.hasNext()) {
            it.next();
            it.remove();
            toRemove--;
        }
    }

    private <T> void maybeRefresh(
            QueryKey key,
            CacheEntry entry,
            long now,
            boolean forced,
            String sql,
            RowMapper<T> mapper,
            Param<?>[] params,
            QueryLoader loader
    ) {
        Duration refreshAfter = config.cache().refreshAfter();
        if (!forced) {
            if (refreshAfter == null || refreshAfter.isZero() || refreshAfter.isNegative()) return;
            long ageNanos = now - entry.writtenAtNanos;
            if (ageNanos < refreshAfter.toNanos()) return;
        }

        if (entry.loadingFuture != null && !entry.loadingFuture.isDone()) return;

        CompletableFuture<List<?>> fut = CompletableFuture.supplyAsync(
                () -> load(loader, sql, mapper, params),
                executor
        );

        queryCache.computeIfPresent(key, (k, old) -> old.withLoadingFuture(fut));

        fut.whenComplete((list, err) -> {
            if (err != null) return;
            queryCache.put(key, CacheEntry.fresh(list, System.nanoTime()));
            evictIfNeeded();
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(List<?> list) {
        return (List<T>) list;
    }

    private static RuntimeException wrap(Throwable t) {
        return (t instanceof RuntimeException re) ? re : new RuntimeException(t);
    }

    private record QueryKey(String sql, String sqlLower, List<ParamKey> params, RowMapper<?> mapper) {
        static QueryKey of(String sql, RowMapper<?> mapper, Param<?>... params) {
            List<ParamKey> keys;
            if (params == null || params.length == 0) {
                keys = List.of();
            } else {
                keys = Arrays.stream(params).map(ParamKey::of).toList();
            }
            return new QueryKey(sql, sql.toLowerCase(Locale.ROOT), keys, mapper);
        }
    }

    private record ParamKey(SqlType<?> type, Object value, int hash) {
        static ParamKey of(Param<?> param) {
            if (param == null) throw new IllegalArgumentException("Null Param");
            SqlType<?> type = param.type();
            Object raw = param.value();
            if (raw instanceof byte[] bytes) {
                byte[] copy = Arrays.copyOf(bytes, bytes.length);
                int h = 31 * System.identityHashCode(type) + Arrays.hashCode(copy);
                return new ParamKey(type, copy, h);
            }
            int h = 31 * System.identityHashCode(type) + Objects.hashCode(raw);
            return new ParamKey(type, raw, h);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParamKey other)) return false;
            if (type != other.type) return false;
            if (value instanceof byte[] a && other.value instanceof byte[] b) {
                return Arrays.equals(a, b);
            }
            return Objects.equals(value, other.value);
        }
    }

    private static final class CacheEntry {
        private final List<?> value;
        private final long writtenAtNanos;
        private final CompletableFuture<List<?>> loadingFuture;

        private CacheEntry(List<?> value, long writtenAtNanos, CompletableFuture<List<?>> loadingFuture) {
            this.value = value;
            this.writtenAtNanos = writtenAtNanos;
            this.loadingFuture = loadingFuture;
        }

        static CacheEntry fresh(List<?> value, long now) {
            return new CacheEntry(value, now, null);
        }

        CacheEntry withLoadingFuture(CompletableFuture<List<?>> future) {
            return new CacheEntry(this.value, this.writtenAtNanos, future);
        }

        boolean isExpired(long now, Duration ttl) {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) return false;
            return (now - writtenAtNanos) >= ttl.toNanos();
        }
    }
}
