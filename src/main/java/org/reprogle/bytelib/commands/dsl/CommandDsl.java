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

import com.mojang.brigadier.arguments.ArgumentType;

/**
 * Entry point for the ByteLib command DSL (Domain Specific Language).
 * 
 * <p>
 * Provides factory methods to construct command trees using a fluent builder
 * pattern.
 * Use the methods in this class to start building command nodes, then chain
 * methods to add
 * handlers, suggestions, and child nodes.
 */
public final class CommandDsl {
    /**
     * Utility class, not instantiable.
     */
    private CommandDsl() {
    }

    /**
     * Creates a literal command node with the given name.
     * 
     * <p>
     * Literal nodes represent fixed command words that must be matched exactly.
     * 
     * @param name the literal command name
     * @return a new LiteralNode for building
     */
    public static LiteralNode literal(String name) {
        return new LiteralNode(name);
    }

    /**
     * Creates a required argument node with the given name and type.
     * 
     * <p>
     * Argument nodes represent typed command arguments that are parsed by
     * Brigadier.
     * 
     * @param <T>  the type of argument
     * @param name the argument name
     * @param type the Brigadier ArgumentType for parsing
     * @return a new ArgumentNode for building
     */
    public static <T> ArgumentNode<T> argument(String name, ArgumentType<T> type) {
        return new ArgumentNode<>(name, type);
    }
}