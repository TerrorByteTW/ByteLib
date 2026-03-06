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

    public Boolean bool(String col) throws SQLException {
        return SqlType.BOOLEAN.read(rs, col);
    }

    public <T> T get(String col, SqlType<T> type) throws SQLException {
        return type.read(rs, col);
    }
}
