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