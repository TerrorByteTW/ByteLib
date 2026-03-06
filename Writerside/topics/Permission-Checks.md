# Permission Checks

The Commands API's `requires()` method uses Predicates, just like Brigadier. However, unlike Brigadier, it also provides
a set of prebuilt Predicates for Permission checks specifically. These Predicates allow for much more powerful
permission checks without complicated `&&` and `||` chains.

> The `PermissionChecks` predicates do not support dynamic checks based on execution context. Therefore, you cannot
> check permissions based on things such as command arguments. The same applies to Brigadier. If you need dynamic
> permission checks, check permissions within the `executes()` handler

The following Predicates are included for you under the `PermissionChecks` class:

- `permission("node")`
- `playerOnly()`
- `consoleOnly()`
- `isOp()`
- `senderType(...)`
- `allOf(...)`
- `anyOf(...)`
- `not(...)`

Example:

<code-block lang="java" noinject="true">
.requires(PermissionChecks.allOf(
    PermissionChecks.playerOnly(),
    PermissionChecks.permission("bytelib.guild.admin")
))
</code-block>

