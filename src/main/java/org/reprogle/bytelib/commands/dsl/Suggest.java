/*
 * Copyright (c) 2026 Nate Reprogle and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

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