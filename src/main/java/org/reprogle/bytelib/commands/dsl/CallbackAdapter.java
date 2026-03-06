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

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Objects;

import org.reprogle.bytelib.commands.CommandFactory;

/**
 * Adapts a DI-resolved CommandCallback class into a Brigadier Command.
 * 
 * <p>
 * This adapter handles instantiation of CommandCallback classes via a
 * CommandFactory
 * and wraps checked exceptions as RuntimeExceptions for Brigadier
 * compatibility.
 */
public final class CallbackAdapter {
    /**
     * Utility class, not instantiable.
     */
    private CallbackAdapter() {
    }

    /**
     * Creates a Brigadier Command from a callback class.
     * 
     * <p>
     * The callback class will be instantiated via the provided factory when the
     * command is executed.
     * Any checked exceptions thrown by the callback are wrapped in
     * RuntimeException.
     * 
     * @param callbackClass the CommandCallback subclass to instantiate
     * @param factory       the CommandFactory to use for instantiation
     * @return a Brigadier Command that executes the callback
     */
    public static Command<CommandSourceStack> fromClass(
            Class<? extends CommandCallback> callbackClass,
            CommandFactory factory) {
        Objects.requireNonNull(callbackClass, "callbackClass");
        Objects.requireNonNull(factory, "factory");

        return context -> {
            final CommandCallback callback = factory.create(callbackClass);
            try {
                return callback.execute(context);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(
                        "Command callback threw checked exception: " + callbackClass.getName(), ex);
            }
        };
    }
}