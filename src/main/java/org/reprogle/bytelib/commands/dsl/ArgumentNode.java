package org.reprogle.bytelib.commands.dsl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.reprogle.bytelib.commands.CommandFactory;

/**
 * Represents a required argument node in a command tree.
 * 
 * <p>
 * ArgumentNode is a builder for Brigadier's RequiredArgumentBuilder, providing
 * a fluent API
 * for constructing typed arguments that can execute commands, suggest values,
 * and have child nodes.
 * 
 * @param <T> the type of argument this node represents
 */
public final class ArgumentNode<T> {
    private final RequiredArgumentBuilder<CommandSourceStack, T> builder;
    private final List<Object> children = new ArrayList<>();

    /**
     * Creates a new ArgumentNode with the specified name and argument type.
     * 
     * @param name the name of the argument
     * @param type the Brigadier ArgumentType for this argument
     */
    ArgumentNode(String name, ArgumentType<T> type) {
        this.builder = RequiredArgumentBuilder.argument(name, type);
    }

    /**
     * Sets the command executor for this argument node.
     * 
     * @param callback the Brigadier command to execute
     * @return this ArgumentNode for method chaining
     */
    public ArgumentNode<T> executes(Command<CommandSourceStack> callback) {
        builder.executes(callback);
        return this;
    }

    /**
     * Sets the command executor using a DI-resolved callback class.
     * 
     * @param callbackClass the callback class to instantiate via DI
     * @param factory       the CommandFactory to create the callback instance
     * @return this ArgumentNode for method chaining
     */
    public ArgumentNode<T> executes(Class<? extends CommandCallback> callbackClass, CommandFactory factory) {
        return executes(CallbackAdapter.fromClass(callbackClass, factory));
    }

    /**
     * Sets the suggestion provider for this argument node.
     * 
     * <p>
     * The provider will be called with the command context and remaining input to
     * generate
     * suggestions for tab completion, optionally including Adventure tooltips.
     * 
     * @param provider the BrigadierSuggestionProvider to use
     * @return this ArgumentNode for method chaining
     */
    public ArgumentNode<T> suggests(BrigadierSuggestionProvider provider) {
        Objects.requireNonNull(provider, "provider");
        builder.suggests((context, suggestionsBuilder) -> {
            final String remaining = suggestionsBuilder.getRemaining();
            for (BrigadierSuggestion suggestion : provider.provide(context, remaining)) {
                if (suggestion.tooltip() != null) {
                    suggestionsBuilder.suggest(
                            suggestion.value(),
                            MessageComponentSerializer.message().serialize(suggestion.tooltip()));
                } else {
                    suggestionsBuilder.suggest(suggestion.value());
                }
            }
            return suggestionsBuilder.buildFuture();
        });
        return this;
    }

    /**
     * Adds a literal node as a child of this argument node.
     * 
     * @param child the literal node to add
     * @return this ArgumentNode for method chaining
     */
    public ArgumentNode<T> then(LiteralNode child) {
        children.add(child);
        return this;
    }

    /**
     * Adds an argument node as a child of this argument node.
     * 
     * @param <X>   the type of the child argument node
     * @param child the argument node to add
     * @return this ArgumentNode for method chaining
     */
    public <X> ArgumentNode<T> then(ArgumentNode<X> child) {
        children.add(child);
        return this;
    }

    /**
     * Builds the Brigadier RequiredArgumentBuilder with all configured children.
     * 
     * @return the built RequiredArgumentBuilder
     */
    RequiredArgumentBuilder<CommandSourceStack, T> build() {
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