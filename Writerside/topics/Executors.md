# Executors

The Commands API supports two ways of executing a command.

## Inline lambda

Inline lambda's provide the `CommandContext<CommandSourceStack>` for a command, but do not provide any other
functionality. Use this for simple commands that don't require any complex logic.

<code-block lang="java" noinject="true">
.executes(ctx -> {
    return 1;
})
</code-block>

## Callback class

For large and/or complex commands, putting the entire command in a single lambda may prove difficult. A version of the
`executes()` method exists which takes a `CommandCallback` class and a factory, and executes the command that way.

<code-block lang="java" noinject="true">
.executes(MyCallback.class, factory)
</code-block>

To use a Callback class, first create a constructor for your `CommandsRegistration` and inject `CommandFactory`, which
you previously saw in the Quick Start section.

Create a new class and ensure it implements `CommandCallback`. `CommandCallback` classes require an
`execute(CommandContext&lt;CommandSourceStack&gt; context)` method. However, they are also able to inject dependencies
in
their constructor (Thanks to the `CommandFactory` you provided to the `LiteralNode`'s `executes()` method).

Below is an example of a `CommandCallback` class. This would be passed into the `executes()` method in your
`CommandRegistration` implementation and executed from there:

<code-block lang="java" noinject="true">
public class MyCommandCallback implements CommandCallback {

    private final BytePluginConfig config;

    @Inject
    public MyCommandCallback(BytePluginConfig config) {
        this.config = config;
    }

    @Override
    public int execute(CommandContext&lt;CommandSourceStack&gt; context) {
        String msg = config.config().getString("my-secret-message"); // Do something that uses a dependency
        context.getSource().getSender().sendPlainMessage("The secret message is: " + msg); // Send the msg to the command sender.
        return Command.SINGLE_SUCCESS; // Command.SINGLE_SUCCESS is equivalent to 1. You can also just do `return 1;`
    }

}
</code-block>

## Inherited executors

One special feature of ByteLib that Brigadier does not support is "inherited executors."

Let's say you are making a command that has a bunch of optional arguments but you want the same handler to handle it.
You'd have to define the same ".executes()" lambda on every single node of the tree. With ByteLib, the Commands API will
automatically pass down the parent node's `.executes()` method to branches in the tree unless explicitly overridden.

Take this snippet from the "Honeypot" plugin:

<code-block lang="java" noinject="true">
.then(
    CommandDsl.literal("history")
        .requires(
            PermissionChecks.anyOf(
                PermissionChecks.permission("honeypot.history"),
                PermissionChecks.permission("honeypot.*"),
                PermissionChecks.isOp()
            )
        )
        .then(
            CommandDsl.argument("action", StringArgumentType.string())
                .suggests(
                    Suggest.fixedWithTooltip(List.of(
                            Suggest.suggestion("delete", Component.text("Delete history record for a player")),
                            Suggest.suggestion("query", Component.text("Query history for a player")),
                            Suggest.suggestion("purge", Component.text("Purges all history for all players"))
                        )
                    )
                )
                .then(
                    CommandDsl.argument("player", ArgumentTypes.player())
                        .suggests(
                            Suggest.dynamic(
                                (ctx, remaining) ->
                                    Bukkit.getOnlinePlayers()
                                        .stream()
                                        .filter(player -> player.getName().startsWith(remaining))
                                        .map(player -> Suggest.suggestion(player.getName()))
                                        .toList()
                            )
                        )
                        .then(
                            CommandDsl.argument("count", IntegerArgumentType.integer(1, 100000))
                        )
                )
                .executes(HoneypotHistory.class, factory))
)
</code-block>

Without inherited executors, you'd have to write this instead:

<code-block lang="java" noinject="true">
.then(
    CommandDsl.literal("history")
        .requires(
            PermissionChecks.anyOf(
                PermissionChecks.permission("honeypot.history"),
                PermissionChecks.permission("honeypot.*"),
                PermissionChecks.isOp()
            )
        )
        .then(
            CommandDsl.argument("action", StringArgumentType.string())
                .suggests(
                    Suggest.fixedWithTooltip(List.of(
                            Suggest.suggestion("delete", Component.text("Delete history record for a player")),
                            Suggest.suggestion("query", Component.text("Query history for a player")),
                            Suggest.suggestion("purge", Component.text("Purges all history for all players"))
                        )
                    )
                )
                .executes(HoneypotHelp.class, factory)
                .then(
                    CommandDsl.argument("player", ArgumentTypes.player())
                        .suggests(
                            Suggest.dynamic(
                                (ctx, remaining) ->
                                    Bukkit.getOnlinePlayers()
                                        .stream()
                                        .filter(player -> player.getName().startsWith(remaining))
                                        .map(player -> Suggest.suggestion(player.getName()))
                                        .toList()
                            )
                        )
                        .executes(HoneypotHistory.class, factory)
                        .then(
                            CommandDsl.argument("count", IntegerArgumentType.integer(1, 100000))
                                .executes(HoneypotHistory.class, factory)
                        )
                )
                .executes(HoneypotHistory.class, factory))
        .executes(HoneypotHelp.class, factory)
)
</code-block>

Every single tree needs an `execute()` branch in Brigadier. In ByteLib, the parent's executor is passed down unless it's
overridden by an explicit call to `executes()`. This happens automatically and cannot be disabled.
