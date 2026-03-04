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