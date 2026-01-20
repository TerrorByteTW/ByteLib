package org.reprogle.bytelib.db.api;

import java.util.List;

public interface DatabaseTx {
    int execute(String sql, Param<?>... params);

    <T> List<T> query(String sql, RowMapper<T> mapper, Param<?>... params);

    <T> T queryOne(String sql, RowMapper<T> mapper, Param<?>... params);
}
