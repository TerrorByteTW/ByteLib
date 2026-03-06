# Configuring SQLite API

`SqliteModule` can be configured with either:

- a fixed `SqliteConfig` instance
- a `Function<BytePluginConfig, SqliteConfig>` factory

## Basic usage

<code-block lang="java">
new SqliteModule(&quot;data.db&quot;, SqliteConfig.defaults());
</code-block>

<code-block lang="java">
new SqliteModule(&quot;data.db&quot;, byteConfig -&gt; {
    SqliteConfig base = SqliteConfig.defaults();
    return new SqliteConfig(
            true,
            &quot;WAL&quot;,
            &quot;NORMAL&quot;,
            5000,
            java.time.Duration.ofMillis(20),
            SqliteConfig.MainThreadPolicy.WARN,
            SqliteConfig.TimeoutBehavior.THROW,
            java.time.Duration.ofMillis(10),
            base.cache()
    );
});
</code-block>

## `SqliteConfig` options

### `foreignKeys` (`boolean`)

- Controls `PRAGMA foreign_keys=ON`.
- Values: `true`, `false`.
- Default: `true`.

### `journalMode` (`String`)

- Passed directly to `PRAGMA journal_mode=<value>`.
- The library does not validate this value.
- Common SQLite values: `WAL`, `DELETE`, `TRUNCATE`, `PERSIST`, `MEMORY`, `OFF`.
- Default: `"WAL"`.

### `synchronous` (`String`)

- Passed directly to `PRAGMA synchronous=<value>`.
- The library does not validate this value.
- Common SQLite values: `OFF`, `NORMAL`, `FULL`, `EXTRA` (or numeric `0`-`3`).
- Default: `"NORMAL"`.

### `busyTimeoutMs` (`int`)

- Passed to `PRAGMA busy_timeout=<value>`.
- Unit: milliseconds.
- Default: `5000`.

### `mainThreadTimeout` (`Duration`)

- Timeout applied to blocking DB calls made on the Bukkit main thread.
- `null`, zero, or negative disables timeout behavior for main-thread calls.
- Default: `Duration.ofMillis(20)`.

### `mainThreadPolicy` (`MainThreadPolicy`)

- Controls what happens when DB calls are made on the main thread.
- Values:
    - `ALLOW`: permit silently
    - `WARN`: permit and allow slow-call warnings
    - `DISALLOW`: throw `DbMainThreadDisallowedException`
- Default: `WARN`.

### `timeoutBehavior` (`TimeoutBehavior`)

- Controls behavior when a main-thread call exceeds `mainThreadTimeout`.
- Values:
    - `FAIL_OPEN`: return fallback result (`null`; query wrappers convert to empty list)
    - `FAIL_CLOSED`: throw `DbTimeoutException`
    - `THROW`: throw `DbTimeoutException`
- Default: `THROW`.

### `slowQueryWarnThreshold` (`Duration`)

- Used only when `mainThreadPolicy == WARN`.
- If the elapsed main-thread DB call time is greater than or equal to this threshold, a warning is logged.
- `null`, zero, or negative disables slow-query warning logs.
- Default: `Duration.ofMillis(10)`.

### `cache` (`SqliteConfig.CacheConfig`)

- Query result cache settings.
- Default: `SqliteConfig.CacheConfig.defaults()`.

## `CacheConfig` options

### `ttl` (`Duration`)

- Time-to-live for cache entries.
- `null`, zero, or negative means entries do not expire by TTL.
- Default: `Duration.ofSeconds(30)`.

### `refreshAfter` (`Duration`)

- Age after which reads may trigger background refresh.
- `null`, zero, or negative disables refresh-after behavior.
- Default: `Duration.ofSeconds(10)`.

### `serveStaleWhileRefreshing` (`boolean`)

- If `true`, stale entries can be served while a refresh is running.
- Values: `true`, `false`.
- Default: `true`.

### `maxSize` (`int`)

- Maximum number of cached query keys.
- Special values:
    - `0`: disable cache
    - `-1`: unlimited cache size
    - `< -1`: disable cache
- Default: `50000`.

## Defaults snapshot

<code-block lang="java">
SqliteConfig.defaults();
// foreignKeys=true
// journalMode=&quot;WAL&quot;
// synchronous=&quot;NORMAL&quot;
// busyTimeoutMs=5000
// mainThreadTimeout=Duration.ofMillis(20)
// mainThreadPolicy=MainThreadPolicy.WARN
// timeoutBehavior=TimeoutBehavior.THROW
// slowQueryWarnThreshold=Duration.ofMillis(10)
// cache=CacheConfig.defaults()
</code-block>
