package org.reprogle.bytelib.commands.dsl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
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
        builder.requires(predicate::test);
        return this;
    }

    /**
     * Sets the command executor for this literal node.
     * 
     * @param callback the Brigadier command to execute
     * @return this LiteralNode for method chaining
     */
    public LiteralNode executes(Command<CommandSourceStack> callback) {
        builder.executes(callback);
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
        for (Object child : children) {
            if (child instanceof LiteralNode literal) {
                builder.then(literal.build());
            } else if (child instanceof ArgumentNode<?> arg) {
                builder.then(arg.build());
            }
        }
        return builder;
    }
}