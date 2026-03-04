package org.reprogle.bytelib.commands;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.Objects;

/**
 * CommandFactory implementation that uses Guice for dependency injection.
 * 
 * <p>
 * Delegates instance creation to a Guice Injector to provide full DI support
 * for CommandCallback implementations.
 */
public final class GuiceCommandFactory implements CommandFactory {
    private final Injector injector;

    /**
     * Creates a GuiceCommandFactory with the Guice Injector.
     * 
     * @param injector the Guice Injector for creating instances
     */
    @Inject
    public GuiceCommandFactory(Injector injector) {
        this.injector = Objects.requireNonNull(injector, "injector");
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
        return injector.getInstance(type);
    }
}