package org.reprogle.bytelib.db.sqlite;

public record SqliteConfig(
        boolean foreignKeys,
        String journalMode,
        String synchronous,
        int busyTimeoutMs
) {
    public static SqliteConfig defaults() {
        return new SqliteConfig(true, "WAL", "NORMAL", 5000);
    }
}
