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
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.reprogle.bytelib.commands.CommandFactory;

/**
 * Represents a literal (fixed text) node in a command tree.
 * 
 * <p>
 * LiteralNode is a builder for Brigadier's LiteralArgumentBuilder, providing a
 * fluent API
 * for constructing commands with fixed names that can have permission
 * requirements, execute
 * callbacks, and child nodes.
 */
public final class LiteralNode {
    private final LiteralArgumentBuilder<CommandSourceStack> builder;
    private final List<Object> children = new ArrayList<>();
    private Command<CommandSourceStack> executor;

    /**
     * Creates a new LiteralNode with the specified name.
     * 
     * @param name the literal command name
     */
    LiteralNode(String name) {
        this.builder = LiteralArgumentBuilder.literal(name);
    }

    /**
     * Sets a permission or execution requirement for this command.
     * 
     * <p>
     * The predicate will be evaluated for each command sender. If false, the
     * command
     * will not be available to them.
     * 
     * @param predicate a predicate that returns true if the sender can execute this
     *                  command
     * @return this LiteralNode for method chaining
     */
    public LiteralNode requires(Predicate<CommandSourceStack> predicate) {
        builder.requires(predicate);
        return this;
    }

    /**
     * Sets the command executor for this literal node.
     * 
     * @param callback the Brigadier command to execute
     * @return this LiteralNode for method chaining
     */
    public LiteralNode executes(Command<CommandSourceStack> callback) {
        Objects.requireNonNull(callback, "callback");
        builder.executes(callback);
        this.executor = callback;
        return this;
    }

    /**
     * Sets the command executor using a DI-resolved callback class.
     * 
     * @param callbackClass the callback class to instantiate via DI
     * @param factory       the CommandFactory to create the callback instance
     * @return this LiteralNode for method chaining
     */
    public LiteralNode executes(Class<? extends CommandCallback> callbackClass, CommandFactory factory) {
        return executes(CallbackAdapter.fromClass(callbackClass, factory));
    }

    /**
     * Adds a literal node as a child of this literal node.
     * 
     * @param child the literal node to add
     * @return this LiteralNode for method chaining
     */
    public LiteralNode then(LiteralNode child) {
        children.add(child);
        return this;
    }

    /**
     * Adds an argument node as a child of this literal node.
     * 
     * @param <T>   the type of the child argument node
     * @param child the argument node to add
     * @return this LiteralNode for method chaining
     */
    public <T> LiteralNode then(ArgumentNode<T> child) {
        children.add(child);
        return this;
    }

    /**
     * Builds the Brigadier LiteralArgumentBuilder with all configured children.
     * 
     * @return the built LiteralArgumentBuilder
     */
    LiteralArgumentBuilder<CommandSourceStack> build() {
        return build(null);
    }

    /**
     * Builds the Brigadier LiteralArgumentBuilder with all configured children.
     * 
     * <p>
     * If this node does not define an executor, it inherits the closest parent
     * executor to allow fallback execution on deeper nodes.
     * 
     * @param inheritedExecutor the closest parent executor, or null if none exists
     * @return the built LiteralArgumentBuilder
     */
    LiteralArgumentBuilder<CommandSourceStack> build(Command<CommandSourceStack> inheritedExecutor) {
        final Command<CommandSourceStack> effectiveExecutor = executor != null ? executor : inheritedExecutor;
        if (executor == null && inheritedExecutor != null) {
            builder.executes(inheritedExecutor);
        }

        for (Object child : children) {
            if (child instanceof LiteralNode literal) {
                builder.then(literal.build(effectiveExecutor));
            } else if (child instanceof ArgumentNode<?> arg) {
                builder.then(arg.build(effectiveExecutor));
            }
        }
        return builder;
    }

    /**
     * Converts this DSL node tree into a Brigadier command node that can be
     * registered directly with Paper commands registrar.
     *
     * @return the built Brigadier LiteralCommandNode
     */
    public LiteralCommandNode<CommandSourceStack> toCommandNode() {
        return build().build();
    }
}
