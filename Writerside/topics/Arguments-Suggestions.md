# Arguments &amp; Suggestions

Commands can be made extremely flexible via arguments. Brigadier (And by proxy, the Commands API) uses the concept of
a "Command tree," where each "branch" is its own "command". In a sense, arguments are just nested commands.

To create an argument, chain your `LiteralNode` with `.then()` to create a new branch in the Command Tree, and then call
`CommandDsl.argument()` inside it. The first argument is the argument name, and the second is the `ArgumentType`.

<code-block lang="java" noinject="true">
// Snippet
LiteralNode root = CommandDsl.literal("money")
                .requires(PermissionChecks.permission("money.balance.self"))
                .executes(ctx -&gt; {
                    ctx.getSource().getSender().sendPlainMessage("You have $xxx!"); // Do whatever you need to return the balance
                })
                .then(CommandDsl.argument("player", ArgumentTypes.player())
                    .requires(PermissionChecks.permission("money.balance.other"))
                    .executes(ctx -&gt; {
                        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                        ctx.getSource().getSender().sendPlainMessage("Player " + target.getName() + " has $xxx!");
                    })
                );

</code-block>

You may also chain `.then()` with for loops. This generates branches like:

- `/module chat <state>`
- `/module guilds <state>`
- `/module economy <state>`

<code-block lang="java" noinject="true">
LiteralNode root = CommandDsl.literal("module");

List&lt;String&gt; modules = List.of("chat", "guilds", "economy");

for (String module : modules) {
    root.then(CommandDsl.literal(module)
            .then(CommandDsl.argument("state", StringArgumentType.word())
            .executes(ctx -&gt; {
                String state = ctx.getArgument("state", String.class);
                return 1;
            })
        )
    );
}
</code-block>

> `.then()` normally takes `CommandDsl.argument()`, but can also take `CommandDsl.literal()`. It's important to
> understand the drawbacks of using `.literal()` in a `.then()` of a Command
> Tree. [See the Paper docs for more info](https://docs.papermc.io/paper/dev/command-api/basics/arguments-and-literals/).
> {style="note"}

`.then()` takes either a `LiteralNode` or an `ArgumentNode`, both of which are created by using `CommandDsl`. If you
inspect them carefully, the inside of `.then()` looks exactly like a regular Brigadier command, and that's because it is!

When a player runs "/money" in-game and then presses the space bar to go to the next argument, the game will allow them
to enter a Player. You'll notice that the argument's value is stored in the `CommandContext<CommandSourceStack>`.
Argument resolution and types are beyond the scope of this guide, [see Paper's documentation on Arguments and
`ArgumentType`s](https://docs.papermc.io/paper/dev/command-api/arguments/minecraft/) for more information.

## Argument suggestions

You can suggest values for arguments to the player by using `suggests()`. These can be done explicitly or dynamically /
programmatically.

The most basic way to suggest values for an argument is to use `Suggest.fixed()`

<code-block lang="java" noinject="true">
.suggests(Suggest.fixed("member", "officer", "leader"))
</code-block>

If you want to add tooltips to the suggestions (Text that displays when the player's mouse hovers over a suggested
value), you can use `Suggest.fixedWithTooltip()`

<code-block lang="java" noinject="true">
// Assuming `MM` is MiniMessage
.suggests(Suggest.fixedWithTooltip(List.of(
    Suggest.suggestion("member", MM.deserialize("&lt;gray&gt;Default guild member&lt;/gray&gt;")),
    Suggest.suggestion("officer", MM.deserialize("&lt;yellow&gt;Can manage invites&lt;/yellow&gt;"))
)))
</code-block>

Suggestions may also be made dynamically using `Suggest.dynamic()`. Use `Suggest.suggestion()` to actually create the
suggestion.

<code-block lang="java" noinject="true">
.suggests(Suggest.dynamic((ctx, remaining) -&gt;
    rankService.getRanks().stream()
        .filter(rank -&gt; rank.startsWith(remaining))
        .map(Suggest::suggestion)
        .toList()
))
</code-block>

<code-block lang="java" noinject="true">
.suggests(Suggest.dynamic((ctx, remaining) -&gt;
    rankService.getRanks().stream()
        .filter(rank -&gt; rank.startsWith(remaining))
        .map(rank -&gt; Suggest.suggestion(
            rank,
            MM.deserialize("&lt;gray&gt;Rank:&lt;/gray&gt; &lt;yellow&gt;" + rank + "&lt;/yellow&gt;")
        ))
        .toList()
))
</code-block>

---
