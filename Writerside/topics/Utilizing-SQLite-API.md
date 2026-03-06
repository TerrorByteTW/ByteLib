# Utilizing SQLite API

To use the SQLite API, you need to inject `SqliteDatabase` wherever needed.

`SqliteDatabase` houses convenient helper functions for basic CRUD operations (insert, update, delete, selectAll, and
selectWhere), but also exposes type-safe functions that allow raw SQL usage (`execute`, `transaction`, `query`,
`queryOne`, `executeAsync`, and `queryAsync`).

You should first define a contract for the data you're storing. A good way to do this would be via a record.

```java
public record TrackedWorld(
    String world,
    World.Environment environment,
    boolean enabled,
    Instant expiresAt
) {
}
```

Now that your DTO has been created, you may also optionally create a `Table` if you plan on just using the basic CRUD
operations. These operations are nice as they abstract away raw SQL but do not allow as much customization.

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

Notice how the `WORLDS` table has an `updatedAt` column while the record does not. Your record does not necessarily
need to contain all columns your table will have unless you plan on `SELECT`ing these. Only columns that will be
returned in `ResultSet`s will need to be listed in your record. For the CRUD operations, you must list all columns in
your `Table`, as those use `SELECT *` under the hood.

Finally, let's do some querying! Let's do a basic query for if a world's dimension is enabled

```java
WorldPauseStatus status = db.queryOne("""
        SELECT enabled, expiresAt
        FROM dimensionpause_worlds
        WHERE world = ? AND dimension = ?;
        """,
    row -> new WorldPauseStatus(
        row.bool("enabled"),
        row.get("expiresAt", PaperSqlTypes.INSTANT)
    ),
    Param.text(world), Tables.dimension.param(dimension));
```

Let's break this down. I don't want the entire table returned to me, so I'm not using the built-in CRUD operations.
Instead, I'm using the `queryOne` function. I pass in raw SQL in the first argument, and the second argument is a
`RowMapper`. `RowMapper`s tell the API how to convert the ResultSet to an object. `row` refers to `Row`, a
super tiny lightweight wrapper around a ResultSet that tells `SqliteDatabase` how to convert a column to a Java object.

You may have noticed a few things here:

* `row.get("expiresAt", PaperSqlTypes.INSTANT)`
* `Param.text(world)`
* `Tables.dimension.param(dimension)`

Let's go over these:

1. `row.get()` is a method that allows you to define a custom type when parsing a `ResultSet` column for a row.
   `PaperSqlTypes` is a custom type in DimensionPause that contains a bunch of static `SqlType<T>`s. There are already
   built-in `SqlType<T>`'s for `int`, `long`, `double`, `String`, `byte[]`, `UUID`, and `Boolean` (Note that Boolean is
   an Integer in SQLite, so be sure to set up your `boolean` columns as `int`) as well as associated methods for row (
   `row.string`, `row.i32`, `row.i64`, `row.f64`, etc.), but you can of course create your own by referring to
   the [](Custom-Data-Types.md) documentation.
2. `Param.text(world)` is similar to `row.get()`, except it converts Java objects to SQLite instead of
   converting SQLite results to Java objects. It's meant to explicitly bind parameters to a concrete type. If `Param`
   doesn't contain a type for your data, you can create your own by using `SqlType<T>` and calling
   `Param.of(SqlType<T>, value)`. For example, to _write_ an `Instant` instead of reading one like in the previous
   example, you can do `Param.of(PaperSqlTypes.INSTANT, expiresAt)`
3. `Tables.dimension.param(dimension)` is the exact equivalent of `Param.of(PaperSqlTypes.ENV, dimension)`.
   `Table.Column<T>` contains a method `param(T value)` which returns a `Param<T>` of the column. This is useful for if
   you already have the type defined, and you don't want to type `Param.of` everywhere, and is also necessary when using
   the CRUD operators.
