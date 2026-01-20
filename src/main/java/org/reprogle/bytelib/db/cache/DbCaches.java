package org.reprogle.bytelib.db.cache;

import org.reprogle.bytelib.db.api.BlockingOptions;
import org.reprogle.bytelib.db.api.Database;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.RowMapper;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public final class DbCaches {
    private DbCaches() {
    }

    @FunctionalInterface
    public interface KeyParams<K> {
        Param<?>[] paramsFor(K key);
    }

    public static <K, V> DbBackedCache<K, V> queryOneCache(
            Database db,
            DbCachePolicy policy,
            String sql,
            RowMapper<V> mapper,
            KeyParams<K> keyParams,
            Executor refreshExecutor
    ) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(sql);
        Objects.requireNonNull(mapper);
        Objects.requireNonNull(keyParams);

        CacheLoader<K, V> loader = key -> {
            V v = db.queryOne(sql, mapper, keyParams.paramsFor(key));
            return Optional.ofNullable(v);
        };

        return new DbBackedCache<>(
                policy,
                loader,
                CacheWriter.noop(),
                CacheDeleter.noop(),
                refreshExecutor
        );
    }

    public static <K, V> DbBackedCache<K, V> queryOneBlockingCache(
            Database db,
            DbCachePolicy policy,
            String sql,
            BlockingOptions blockingOptions,
            RowMapper<V> mapper,
            KeyParams<K> keyParams,
            Executor refreshExecutor
    ) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(sql);
        Objects.requireNonNull(mapper);
        Objects.requireNonNull(keyParams);

        CacheLoader<K, V> loader = key -> {
            V v = db.queryOneBlocking(sql, mapper, blockingOptions, keyParams.paramsFor(key));
            return Optional.ofNullable(v);
        };

        return new DbBackedCache<>(
                policy,
                loader,
                CacheWriter.noop(),
                CacheDeleter.noop(),
                refreshExecutor
        );
    }
}
