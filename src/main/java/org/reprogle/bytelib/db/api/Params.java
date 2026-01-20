package org.reprogle.bytelib.db.api;

import java.util.UUID;

public class Params {
    private Params() {
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
}
