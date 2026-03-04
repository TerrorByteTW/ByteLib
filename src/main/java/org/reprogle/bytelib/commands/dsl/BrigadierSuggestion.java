package org.reprogle.bytelib.commands.dsl;

import net.kyori.adventure.text.Component;

/**
 * A single tab completion suggestion with an optional Adventure tooltip.
 * 
 * <p>
 * This record combines a suggestion value with an optional tooltip component
 * that will be
 * displayed to the player during command tab completion.
 * 
 * @param value   the text value to suggest
 * @param tooltip the optional Adventure Component to display as a tooltip, or
 *                null for no tooltip
 */
public record BrigadierSuggestion(String value, Component tooltip) {
    /**
     * Creates a suggestion with no tooltip.
     * 
     * @param value the suggestion text
     * @return a new BrigadierSuggestion without a tooltip
     */
    public static BrigadierSuggestion of(String value) {
        return new BrigadierSuggestion(value, null);
    }

    /**
     * Creates a suggestion with an optional tooltip.
     * 
     * @param value   the suggestion text
     * @param tooltip the Adventure Component to display as a tooltip
     * @return a new BrigadierSuggestion with the specified tooltip
     */
    public static BrigadierSuggestion of(String value, Component tooltip) {
        return new BrigadierSuggestion(value, tooltip);
    }
}