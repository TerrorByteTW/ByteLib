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

package org.reprogle.bytelib.db.migrate;

import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
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
