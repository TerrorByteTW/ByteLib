package org.reprogle.bytelib.commands.dsl;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * A callback interface for command execution with dependency injection support.
 * 
 * <p>
 * Implementations should be registered in the DI container and instantiated via
 * CommandFactory.
 * The execute method receives the Brigadier command context and can perform
 * permission and
 * argument validation.
 */
@FunctionalInterface
public interface CommandCallback {
    /**
     * Executes the command logic.
     * 
     * @param context the Brigadier command context containing the command sender
     *                and parsed arguments
     * @return a Brigadier command result code (typically 1 for success, 0 for
     *         failure)
     * @throws Exception if command execution fails, will be wrapped in
     *                   RuntimeException
     */
    int execute(CommandContext<CommandSourceStack> context) throws Exception;
}