package org.reprogle.bytelib.db.api;

@FunctionalInterface
public interface TransactionCallback<T> {
    T run(DatabaseTx tx) throws Exception;
}
