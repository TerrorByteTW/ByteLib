package org.reprogle.bytelib.db.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public record Param<T>(SqlType<T> type, T value) {
    public void bind(PreparedStatement ps, int index) throws SQLException {
        type.bind(ps, index, value);
    }
}
