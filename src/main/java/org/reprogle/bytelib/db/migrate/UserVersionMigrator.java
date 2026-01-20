package org.reprogle.bytelib.db.migrate;

import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

import java.util.Comparator;
import java.util.List;

public final class UserVersionMigrator {
    private final List<MigrationStep> steps;

    public UserVersionMigrator(List<MigrationStep> steps) {
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(MigrationStep::targetVersion))
                .toList();
    }

    public void migrate(SqliteDatabase db) {
        db.transaction(tx -> {
            Integer current = tx.queryOne("PRAGMA user_version;", row -> row.i32("user_version"));
            int ver = current == null ? 0 : current;

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
