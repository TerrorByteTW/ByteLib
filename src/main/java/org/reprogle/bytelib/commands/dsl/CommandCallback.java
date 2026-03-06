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