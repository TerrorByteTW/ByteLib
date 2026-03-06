package org.reprogle.bytelib.commands;

import com.google.inject.Inject;
import org.reprogle.bytelib.ByteLibPlugin;

import java.util.Objects;

/**
 * CommandFactory implementation that uses Guice for dependency injection.
 *
 * <p>
 * Delegates instance creation to a Guice Injector to provide full DI support
 * for CommandCallback implementations.
 */
public final class GuiceCommandFactory implements CommandFactory {
    private final ByteLibPlugin plugin;

    /**
     * Creates a GuiceCommandFactory
     *
     * @param plugin the ByteLibPlugin instance for creating commands
     */
    @Inject
    public GuiceCommandFactory(ByteLibPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Creates an instance of the specified class using the Guice Injector.
     *
     * @param <T>  the type to instantiate
     * @param type the class to instantiate
     * @return an instance with dependencies injected by Guice
     */
    @Override
    public <T> T create(Class<T> type) {
        return plugin.injector().getInstance(type);
    }
}