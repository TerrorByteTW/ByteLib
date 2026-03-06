# Commands API

<primary-label ref="subject-to-change" />
<secondary-label ref="wip"/>

The Commands API is a DSL for Brigadier that makes command authoring easier while still providing all the amazing
features Brigadier offers over the legacy `CommandExecutor` API in Spigot.

### Key capabilities

- Fluent Brigadier DSL (`literal`, `argument`, `then`, `requires`, `executes`, `suggests`)
- DI-backed command callbacks
- Complex permission logic (`allOf`, `anyOf`, `not`)
- Static and dynamic suggestions
- Dynamic generation of subcommands/subarguments at registration time

## Brigadier vs Commands API {collapsible="true"}

You may be asking yourself: "Why would I use ByteLib's Commands API instead of Brigadier, when they're so similar?" Read
the blurb below to understand the benefits or skip down to the Quick Start section to just get started.

The benefit to Commands API is that it provides helpers to reduce boilerplate code that Brigadier typically requires 
and also provides first class support for Google Guice out of the box. Here's an example of two identical commands, one
written with Brigadier and one written with the Commands API:

<compare first-title="Brigadier" second-title="Commands API">
<code-block lang="java">
public final class BrigadierCommand {

    public static void register(Commands commands) {
        LiteralArgumentBuilder&lt;CommandSourceStack&gt; root =
            LiteralArgumentBuilder.&lt;CommandSourceStack&gt;literal("rank")
                .requires(src -&gt; src.getSender().hasPermission("rank.use"))
                .then(LiteralArgumentBuilder.&lt;CommandSourceStack&gt;literal("set")
                    .requires(src -&gt; src.getSender().hasPermission("rank.set"))
                    .then(RequiredArgumentBuilder
                        .&lt;CommandSourceStack, String&gt;argument("player", StringArgumentType.word())
                        .then(RequiredArgumentBuilder
                            .&lt;CommandSourceStack, String&gt;argument("rank", StringArgumentType.word())
                            .suggests((ctx, builder) -&gt; {
                                builder.suggest("member");
                                builder.suggest("officer");
                                builder.suggest("leader");
                                return builder.buildFuture();
                            })
                            .executes(ctx -&gt; {
                                String player = ctx.getArgument("player", String.class);
                                String rank = ctx.getArgument("rank", String.class);

                                ctx.getSource().getSender()
                                    .sendMessage("Set " + player + " to rank " + rank);
                                return 1;
                            }))));

        commands.register(root.build());
    }
}
</code-block>
<code-block lang="java">
public final class CommandsAPICommandRegistration implements CommandRegistration {
    @Override
    public void register(Commands commands) {
        LiteralNode root = CommandDsl.literal("rank")
            .requires(PermissionChecks.permission("rank.use"))
            .then(CommandDsl.literal("set")
                .requires(PermissionChecks.permission("rank.set"))
                .then(CommandDsl.argument("player", StringArgumentType.word())
                    .then(CommandDsl.argument("rank", StringArgumentType.word())
                        .suggests(Suggest.fixed("member", "officer", "leader"))
                        .executes(ctx -> {
                            String player = ctx.getArgument("player", String.class);
                            String rank = ctx.getArgument("rank", String.class);

                            ctx.getSource().getSender()
                                .sendMessage("Set " + player + " to rank " + rank);
                            return 1;
                        }))));

        commands.register(root.toCommandNode());
    }
}
</code-block>
</compare>

They both look similar, but the Commands API command has slightly less boilerplate. This alone isn't enough to convince
someone to use the Commands API over Brigadier. However, once you start getting into `CommandCallback`s, complicated
permission checks, variable arguments & suggestions, and dependency hell, the Commands API makes it trivially easy to
expand your command(s).
