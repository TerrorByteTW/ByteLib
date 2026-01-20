package org.reprogle.bytelib.db.migrate;

public record MigrationStep(int targetVersion, Migration migration) {
}
