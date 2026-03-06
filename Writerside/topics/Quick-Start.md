# Quick Start

Below is an example of how to register a command using the Commands API. It provides a
bare minimum proof-of-concept.

```java
package my.plugin.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.bytelib.commands.dsl.*;

import java.util.List;

public final class ExampleGuildCommandRegistration implements CommandRegistration {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final CommandFactory factory;

    @Inject
    public ExampleGuildCommandRegistration(CommandFactory factory) {
        this.factory = factory;
    }

    @Override
    public void register(Commands commands) {
        LiteralNode root = CommandDsl.literal("guild")
            .requires(PermissionChecks.permission("bytelib.guild.use"))
            .then(CommandDsl.literal("invite")
                .requires(PermissionChecks.playerOnly())
                .then(CommandDsl.argument("player", ArgumentTypes.player())
                    .executes(GuildInviteCallback.class, factory)))
            .then(CommandDsl.literal("setrank")
                .requires(PermissionChecks.allOf(
                    PermissionChecks.playerOnly(),
                    PermissionChecks.permission("bytelib.guild.admin")
                ))
                .then(CommandDsl.argument("rank", StringArgumentType.word())
                    .suggests(Suggest.fixedWithTooltip(List.of(
                        Suggest.suggestion("member", MM.deserialize("<gray>Default guild member</gray>")),
                        Suggest.suggestion("officer", MM.deserialize("<yellow>Can manage invites</yellow>")),
                        Suggest.suggestion("leader", MM.deserialize("<red>Full guild control</red>"))
                    )))
                    .executes(ctx -> 1)));

        commands.register(root.toCommandNode());
    }
}
```

