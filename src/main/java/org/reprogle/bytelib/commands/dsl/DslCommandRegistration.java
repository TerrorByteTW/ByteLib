package org.reprogle.bytelib.commands.dsl;

import org.reprogle.bytelib.commands.CommandRegistration;

import com.google.inject.Inject;
import io.papermc.paper.command.brigadier.Commands;

/**
 * CommandRegistration adapter for registering DSL-built command trees.
 * 
 * <p>
 * Bridges the gap between the command DSL and the registration infrastructure
 * by implementing
 * CommandRegistration. Allows DSL-built LiteralNode trees to be registered via
 * the standard
 * lifecycle and Guice module system.
 * 
 * <p>
 * <strong>Note:</strong> Paper Brigadier uses the LiteralCommandNode as the
 * command identity;
 * no separate label argument is passed to Commands#register(...).
 */
public final class DslCommandRegistration implements CommandRegistration {
    private final LiteralNode root;

    /**
     * Creates a DslCommandRegistration with the root command node.
     * 
     * @param root the root LiteralNode of the command tree to register
     */
    @Inject
    public DslCommandRegistration(LiteralNode root) {
        this.root = root;
    }

    /**
     * Registers the DSL-built command tree with Paper Brigadier.
     * 
     * @param commands the Paper Brigadier Commands registrar
     */
    @Override
    public void register(Commands commands) {
        commands.register(root.build().build());
    }
}