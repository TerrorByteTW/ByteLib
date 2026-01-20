package org.reprogle.bytelib.db.migrate;

import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

@FunctionalInterface
public interface Migration {
    void apply(SqliteDatabase.Tx tx) throws Exception;
}
