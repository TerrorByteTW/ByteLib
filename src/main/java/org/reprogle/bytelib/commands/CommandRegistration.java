package org.reprogle.bytelib.commands;

import io.papermc.paper.command.brigadier.Commands;

@FunctionalInterface
public interface CommandRegistration {
    void register(Commands commands);
}
