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
