/*
 * Copyright (c) 2026 Nate Reprogle and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

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
