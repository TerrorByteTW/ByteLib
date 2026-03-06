# DslCommandRegistration

`DslCommandRegistration` is an **optional adapter** that wraps a prebuilt `LiteralNode` and exposes it as a `CommandRegistration`.

### Why you might want it
Use it when you want to:

- Build command trees in separate providers/builders
- Reuse command roots across modules/tests
- Keep registration classes thin (or avoid writing one per command)
- Standardize registration of prebuilt DSL roots

### Why you might skip it
If you prefer explicit classes like `ExampleGuildCommandRegistration` for each top-level command, you don’t need this adapter.

### Example usage

```java
public final class GuildRegistrationFactory {
    private final CommandFactory factory;

    @Inject
    public GuildRegistrationFactory(CommandFactory factory) {
        this.factory = factory;
    }

    public CommandRegistration create() {
        LiteralNode root = CommandDsl.literal("guild")
            .requires(PermissionChecks.permission("bytelib.guild.use"))
            .then(CommandDsl.literal("invite")
                .then(CommandDsl.argument("player", ArgumentTypes.player())
                    .executes(GuildInviteCallback.class, factory)));

        return new DslCommandRegistration(root);
    }
}
```

Then add the returned `CommandRegistration` to your DI registration set (pattern depends on your module design). An example of doing this would be as follows:

```java
public final class YourPluginCommandsModule extends AbstractModule {
    @ProvidesIntoSet
    CommandRegistration provideGuildRegistration(CommandFactory factory) {
        LiteralNode root = CommandDsl.literal("guild")
            .then(CommandDsl.literal("setrank")
                .then(CommandDsl.argument("rank", StringArgumentType.word())
                    .executes(ctx -> 1)));

        return new DslCommandRegistration(root);
    }
}
```

To actually have that command be picked up by ByteLib when your plugin loads, you must add that module to your Wiring class (See the "Bootstrapping a Plugin" page for more info):

```java
@Override
public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
    return List.of(
            new YourPluginModule(),
            new YourPluginCommandsModule()
    );
}
```
