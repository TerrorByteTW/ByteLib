# Custom Data Types

If you want to create a custom type, create a variable typed as `SqlType<T>`, where `T` is your custom data
type, then set it to a new `SqlType<>()` and implement the `bind` and `read` functions.

Here is how DimensionPause implements the `SqlType` for `Instant`, while supporting nullability as well

<code-block lang="java">
public static final SqlType&lt;Instant&gt; INSTANT = new SqlType&lt;&gt;() {
        public void bind(PreparedStatement ps, int index, Instant value) throws SQLException {
            if (value != null) ps.setLong(4, value.toEpochMilli());
            else ps.setNull(4, Types.BIGINT);
        }

        public @Nullable Instant read(ResultSet rs, String column) throws SQLException {
            Object raw = rs.getObject(column);
            Long expiresAtMs = (raw instanceof Number n) ? n.longValue() : null;
            return expiresAtMs != null ? Instant.ofEpochMilli(expiresAtMs) : null;
        }
    };

}
</code-block>

Since SQLite has no idea what an `Instant` is, we use `BIGINT` as the backing data type. Think of `bind()` as a
serializer and `read()` as a deserializer: `bind()` writes a `long` to the DB, and `read()` converts it back to an
`Instant`, or returns null if the column is null.
