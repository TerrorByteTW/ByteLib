package org.reprogle.bytelib.commands.dsl;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;

/**
 * Provides tab completion suggestions for command arguments.
 * 
 * <p>
 * Implementations of this interface generate dynamic suggestions based on the
 * current
 * command context and remaining user input. Suggestions can optionally include
 * tooltips
 * for additional user guidance.
 */
@FunctionalInterface
public interface BrigadierSuggestionProvider {
    /**
     * Provides suggestions for the given command context and remaining input.
     * 
     * @param context   the current command context
     * @param remaining the remaining user input after the last space
     * @return a collection of suggestions, optionally filtered to the remaining
     *         input
     */
    Collection<BrigadierSuggestion> provide(CommandContext<CommandSourceStack> context, String remaining);
}