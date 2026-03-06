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

package org.reprogle.bytelib.commands;

import io.papermc.paper.command.brigadier.Commands;

/**
 * Registers command nodes with Paper's Brigadier command system.
 * 
 * <p>
 * Implementations define how to register command trees or individual commands.
 * Multiple registrations can be bound via Guice's Multibinder to compose
 * commands.
 */
@FunctionalInterface
public interface CommandRegistration {
    /**
     * Registers commands with the provided Commands registrar.
     * 
     * @param commands the Paper Brigadier Commands registrar
     */
    void register(Commands commands);
}
