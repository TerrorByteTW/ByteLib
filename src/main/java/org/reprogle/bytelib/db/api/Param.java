package org.reprogle.bytelib.db.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public record Param<T>(SqlType<T> type, T value) {
    public void bind(PreparedStatement ps, int index) throws SQLException {
        type.bind(ps, index, value);
    }

    public static <T> Param<T> of(SqlType<T> type, T value) {
        return new Param<>(type, value);
    }

    public static Param<Integer> i32(Integer v) {
        return new Param<>(SqlType.I32, v);
    }

    public static Param<Long> i64(Long v) {
        return new Param<>(SqlType.I64, v);
    }

    public static Param<Double> f64(Double v) {
        return new Param<>(SqlType.F64, v);
    }

    public static Param<String> text(String v) {
        return new Param<>(SqlType.TEXT, v);
    }

    public static Param<byte[]> blob(byte[] v) {
        return new Param<>(SqlType.BLOB, v);
    }

    public static Param<UUID> uuid(UUID v) {
        return new Param<>(SqlType.UUID_TEXT, v);
    }

    public static Param<Boolean> bool(Boolean v) {
        return new Param<>(SqlType.BOOLEAN, v);
    }
}
