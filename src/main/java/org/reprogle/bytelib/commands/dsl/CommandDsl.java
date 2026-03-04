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