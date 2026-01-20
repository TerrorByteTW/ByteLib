package org.reprogle.bytelib.db.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface SqlType<T> {
    void bind(PreparedStatement ps, int index, T value) throws SQLException;

    T read(ResultSet rs, String column) throws SQLException;

    SqlType<Integer> I32 = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, Integer value) throws SQLException {
            if (value == null)
                ps.setNull(index, java.sql.Types.INTEGER);
            else
                ps.setInt(index, value);
        }

        public Integer read(ResultSet rs, String column) throws SQLException {
            int v = rs.getInt(column);
            return rs.wasNull() ? null : v;
        }
    };

    SqlType<Long> I64 = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, Long value) throws SQLException {
            if (value == null)
                ps.setNull(index, java.sql.Types.BIGINT);
            else
                ps.setLong(index, value);
        }

        public Long read(ResultSet rs, String column) throws SQLException {
            long v = rs.getLong(column);
            return rs.wasNull() ? null : v;
        }
    };

    SqlType<Double> F64 = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, Double value) throws SQLException {
            if (value == null)
                ps.setNull(index, java.sql.Types.DOUBLE);
            else
                ps.setDouble(index, value);
        }

        public Double read(ResultSet rs, String column) throws SQLException {
            double v = rs.getDouble(column);
            return rs.wasNull() ? null : v;
        }
    };

    SqlType<String> TEXT = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, String value) throws SQLException {
            ps.setString(index, value);
        }

        public String read(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }
    };

    SqlType<byte[]> BLOB = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, byte[] value) throws SQLException {
            ps.setBytes(index, value);
        }

        public byte[] read(ResultSet rs, String column) throws SQLException {
            return rs.getBytes(column);
        }
    };

    SqlType<UUID> UUID_TEXT = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, UUID value) throws SQLException {
            ps.setString(index, value == null ? null : value.toString());
        }

        public UUID read(ResultSet rs, String column) throws SQLException {
            String s = rs.getString(column);
            return s == null ? null : UUID.fromString(s);
        }
    };

    SqlType<Boolean> BOOLEAN = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, Boolean value) throws SQLException {
            ps.setInt(index, value ? 1 : 0);
        }

        public Boolean read(ResultSet rs, String column) throws SQLException {
            return rs.getInt(column) == 1;
        }
    };
}
