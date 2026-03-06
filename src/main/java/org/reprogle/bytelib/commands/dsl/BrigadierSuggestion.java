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