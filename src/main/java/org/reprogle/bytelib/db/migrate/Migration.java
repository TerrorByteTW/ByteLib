package org.reprogle.bytelib.db.migrate;

import org.reprogle.bytelib.db.api.DatabaseTx;

@FunctionalInterface
public interface Migration {
    void apply(DatabaseTx tx) throws Exception;
}