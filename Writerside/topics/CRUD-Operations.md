# CRUD Operations

The SQLite API provides a set of basic CRUD operators that you can use to query data without writing raw SQL.

This is where tables really shine! Let's insert a single record into the DB. First, let's bring back our `WORLDS` table
we created in a previous topic:

```java
public final class Tables {
    public static final Table WORLDS = Table.of("worlds");
    public static final Table.Column<String> world = WORLDS.col("world", SqlType.TEXT);
    public static final Table.Column<World.Environment> dimension = WORLDS.col("dimension", PaperSqlTypes.ENV);
    public static final Table.Column<Boolean> enabled = WORLDS.col("enabled", SqlType.BOOLEAN);
    public static final Table.Column<String> updatedAt = WORLDS.col("updatedAt", SqlType.TEXT);
    public static final Table.Column<Instant> expiresAt = WORLDS.col("expiresAt", PaperSqlTypes.INSTANT);

    private Tables() {
    }
}
```

<code-block lang="java">
db.insert(
    WORLDS,
    Map.of(
        WORLD, &quot;world&quot;,
        DIMENSION, World.Environment.NORMAL,
        ENABLED, true,
        EXPIRES_AT, null // This would actually fail because Map.of() doesn't allow null values, but it's here just for example
    )
);
</code-block>

Because we already defined the concrete types for the `WORLDS` table, `SqliteDatabase` can take the `Map` we provided,
convert each of those values to their SQLite-compatible types, and write to the correct table. It's that easy!

> Note the omission of the `updatedAt` column. Because this is a custom type we defined, it has built-in handling for
> null values. Therefore, if we want to pass null, we can just omit it. Check the [](Custom-Data-Types.md)
> documentation to learn how to define your own types
> {style="note"}

Let's say you want to query, it's just as easy:

```java
List<WorldPauseStatus> statuses = db.query(
    WORLDS,
    row -> new WorldPauseStatus(
        row.bool("enabled"),
        row.get("expiresAt", PaperSqlTypes.INSTANT)
    )
);
```

The `query()` method takes a `Table` and a `RowMapper`. The Table is provided so it knows which table to query when
calling `SELECT * FROM`, and the RowMapper is used to convert the results to objects.

Need to filter your query? Use `selectWhere`

```java
List<WorldPauseStatus> statuses = db.selectWhere(
    WORLDS,
    "world = ?",
    row -> new WorldPauseStatus(
        row.bool("enabled"),
        row.get("expiresAt", PaperSqlTypes.INSTANT)
    ),
    Param.text("world")
);
```

`selectWhere()` takes four or more arguments. The first and third arguments are the same as `query()`, just representing
your
`Table` and `RowMapper`. However, the second argument is a raw SQL representation of a `WHERE` clause. The fourth and
following arguments are `Param` objects representing the values that will be inserted into the `WHERE` clause.

> Do not add `WHERE` in the filter string, `selectWhere()` does that automatically

The API handles type conversion automatically, while still requiring you to write some manual SQL. It's not meant to be
an ORM, but it is made to simplify your life a little bit
