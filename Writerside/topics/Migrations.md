# Migrations

The SQLite API has built-in support for database migrations. Migrations don't happen automatically, but the API provides
methods for doing so, and it is extremely simple to wire this up.

## Configuring Migrations

First, create a `List` of `MigrationStep` objects. Each object consists of the DB version and a transaction.


<code-block lang="java">
List&lt;MigrationStep&gt; steps = List.of(
    new MigrationStep(1, tx -&gt; {
        tx.execute(&quot;&quot;&quot;
                CREATE TABLE IF NOT EXISTS regions (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL
                );
            &quot;&quot;&quot;);
    }),
    new MigrationStep(2, tx -&gt; {
        tx.execute(&quot;&quot;&quot;
                CREATE VIRTUAL TABLE IF NOT EXISTS region_index
                USING rtree(id, minX, maxX, minZ, maxZ);
            &quot;&quot;&quot;);
    })
);
</code-block>

This migration on version 1 would create a regions table, and on version 2 would create a regions index using `rtree`.
Once you have this list of `MigrationStep`s created, initialize a `UserVersionMigrator` and call its `migrate` method,
providing your instance of `SqliteDatabase` as its sole parameter

<code-block lang="java">
new UserVersionMigrator(&quot;anchor_table&quot;,steps).migrate(db);
</code-block>

`UserVersionMigrator` does not use dependency injection, so you should instantiate it with `new`.

The migrator will automatically look at your DB's version and run the migrations that need to be run. The version is
stored in the `user_version` pragma, so it is recommended not to manually change this value _ever_.

To prevent the migrator from attempting to run migrations on a brand new DB, it requires an "anchor table" to look up.
The anchor table is just a table that should exist in the database. If it doesn't exist (i.e., the database has never
been created), then migrations are skipped.

> Because migrations are skipped if an anchor table doesn't exist (By design), you should create your DB schema
> **AFTER** you call the `UserVersionMigrator`. Creating the schema and then calling the migrator may cause the migrator
> to run migrations on tables that don't need it.
>
> There are two design patterns you can choose here:
>
> 1. Create a base schema for the first version of your plugin. Create migrations that you expect to be run even if the
     plugin is being run for the first time. Doing this means you can never touch your base schema ever again, and all
     changes to the DB must be done via migrations. As well, first-time setup is done primarily through migrations and
     not your schema.
> 2. Have a base schema that is always the "most recent" schema, so new databases are created correctly the first time.
     Create migrations that are only meant for existing databases. Create schema only after migrations are called that
     way it doesn't attempt to migrate them the first time.
>
> TerrorByteTW plugins follow the 2nd design pattern, but you can choose whatever you want.