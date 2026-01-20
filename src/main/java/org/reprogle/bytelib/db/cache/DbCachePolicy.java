package org.reprogle.bytelib.db.cache;


import java.time.Duration;

public record DbCachePolicy(Duration ttl,
                            Duration refreshAfter,
                            Duration blockingTimeout,
                            boolean serveStaleWhileRefreshing,
                            int maxSize
) {
    public static DbCachePolicy blockingFirstDefaults() {
        return new DbCachePolicy(
                Duration.ofSeconds(30),
                Duration.ofSeconds(10),
                Duration.ofMillis(15),
                true,
                50_000);
    }
}