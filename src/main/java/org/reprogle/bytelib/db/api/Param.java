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
