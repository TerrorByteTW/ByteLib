package org.reprogle.bytelib.db.sqlite;

import java.time.Duration;

public record SqliteConfig(
        boolean foreignKeys,
        String journalMode,
        String synchronous,
        int busyTimeoutMs,
        Duration mainThreadTimeout,
        MainThreadPolicy mainThreadPolicy,
        TimeoutBehavior timeoutBehavior,
        Duration slowQueryWarnThreshold,
        CacheConfig cache
) {
    public static SqliteConfig defaults() {
        return new SqliteConfig(
                true,
                "WAL",
                "NORMAL",
                5000,
                Duration.ofMillis(20),
                MainThreadPolicy.WARN,
                TimeoutBehavior.THROW,
                Duration.ofMillis(10),
                CacheConfig.defaults()
        );
    }

    public SqliteConfig withCache(CacheConfig cache) {
        return new SqliteConfig(
                foreignKeys,
                journalMode,
                synchronous,
                busyTimeoutMs,
                mainThreadTimeout,
                mainThreadPolicy,
                timeoutBehavior,
                slowQueryWarnThreshold,
                cache
        );
    }

    public enum MainThreadPolicy {
        ALLOW,
        WARN,
        DISALLOW
    }

    public enum TimeoutBehavior {
        FAIL_OPEN,
        FAIL_CLOSED,
        THROW
    }

    public record CacheConfig(
            Duration ttl,
            Duration refreshAfter,
            boolean serveStaleWhileRefreshing,
            int maxSize
    ) {
        public static CacheConfig defaults() {
            return new CacheConfig(
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(10),
                    true,
                    50_000
            );
        }
    }
}
