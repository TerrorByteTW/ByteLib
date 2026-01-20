package org.reprogle.bytelib.db.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Row {
    private final ResultSet rs;

    public Row(ResultSet rs) {
        this.rs = rs;
    }

    public String string(String col) throws SQLException {
        return SqlType.TEXT.read(rs, col);
    }

    public Integer i32(String col) throws SQLException {
        return SqlType.I32.read(rs, col);
    }

    public Long i64(String col) throws SQLException {
        return SqlType.I64.read(rs, col);
    }

    public Double f64(String col) throws SQLException {
        return SqlType.F64.read(rs, col);
    }

    public byte[] blob(String col) throws SQLException {
        return SqlType.BLOB.read(rs, col);
    }

    public UUID uuid(String col) throws SQLException {
        return SqlType.UUID_TEXT.read(rs, col);
    }

    public <T> T get(String col, SqlType<T> type) throws SQLException {
        return type.read(rs, col);
    }
}
