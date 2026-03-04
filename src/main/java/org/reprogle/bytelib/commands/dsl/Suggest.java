package org.reprogle.bytelib.commands.dsl;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Utility class for creating tab completion suggestion providers.
 * 
 * <p>
 * Provides factory methods for creating different types of suggestion
 * providers:
 * <ul>
 * <li>Fixed: Static list of suggestions</li>
 * <li>Fixed with tooltips: Static suggestions with Adventure tooltips</li>
 * <li>Dynamic: Custom suggestions based on context</li>
 * </ul>
 */
public final class Suggest {
    /**
     * Utility class, not instantiable.
     */
    private Suggest() {
    }

    /**
     * Creates a suggestion provider with a fixed set of string suggestions.
     * 
     * @param values the static suggestion values
     * @return a provider that always suggests these values
     */
    public static BrigadierSuggestionProvider fixed(String... values) {
        return (ctx, remaining) -> {
            List<BrigadierSuggestion> out = new ArrayList<>();
            for (String value : values)
                out.add(BrigadierSuggestion.of(value));
            return out;
        };
    }

    /**
     * Creates a suggestion provider with a fixed set of suggestions including
     * tooltips.
     * 
     * @param suggestions the static suggestions with optional tooltips
     * @return a provider that always suggests these values
     */
    public static BrigadierSuggestionProvider fixedWithTooltip(Collection<BrigadierSuggestion> suggestions) {
        return (ctx, remaining) -> suggestions;
    }

    /**
     * Creates a dynamic suggestion provider based on a custom function.
     * 
     * <p>
     * The function receives the command context and remaining input, allowing for
     * context-aware suggestions.
     * 
     * @param provider function that generates suggestions based on context and
     *                 remaining input
     * @return a provider using the custom logic
     */
    public static BrigadierSuggestionProvider dynamic(
            BiFunction<CommandContext<CommandSourceStack>, String, Collection<BrigadierSuggestion>> provider) {
        return provider::apply;
    }

    /**
     * Creates a suggestion without a tooltip.
     * 
     * @param value the suggestion text
     * @return a new suggestion
     */
    public static BrigadierSuggestion suggestion(String value) {
        return BrigadierSuggestion.of(value);
    }

    /**
     * Creates a suggestion with an optional tooltip.
     * 
     * @param value   the suggestion text
     * @param tooltip the Adventure Component to display as a tooltip
     * @return a new suggestion with tooltip
     */
    public static BrigadierSuggestion suggestion(String value, Component tooltip) {
        return BrigadierSuggestion.of(value, tooltip);
    }
}