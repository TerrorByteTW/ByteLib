package org.reprogle.bytelib.db.api;

@FunctionalInterface
public interface RowMapper<T> {
    T map(Row row) throws Exception;
}
