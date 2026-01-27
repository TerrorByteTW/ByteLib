package org.reprogle.bytelib.db.migrate;

import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

import java.util.Comparator;
import java.util.List;

public final class UserVersionMigrator {
    private final List<MigrationStep> steps;
    private final String anchorTable;

    public UserVersionMigrator(String anchorTable, List<MigrationStep> steps) {
        this.anchorTable = anchorTable;
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(MigrationStep::targetVersion))
                .toList();
    }

    public void migrate(SqliteDatabase db) {
        db.transaction(tx -> {
            Integer current = tx.queryOne("PRAGMA user_version;", row -> row.i32("user_version"));
            int ver = current == null ? 0 : current;

            // Catches null as well
            boolean hasAnchor = Boolean.TRUE.equals(tx.queryOne("""
                    SELECT 1
                    FROM sqlite_master
                    WHERE type='table' AND name = ?
                    LIMIT 1;
                    """, row -> true, Param.text(anchorTable)));

            int latest = steps.isEmpty() ? 0 : steps.getLast().targetVersion();

            if (ver == 0 && !hasAnchor) {
                tx.execute("PRAGMA user_version = " + latest + ";");
                return null;
            }

            for (MigrationStep step : steps) {
                if (step.targetVersion() > ver) {
                    step.migration().apply(tx);
                    tx.execute("PRAGMA user_version = " + step.targetVersion() + ";");
                    ver = step.targetVersion();
                }
            }
            return null;
        });
    }
}
