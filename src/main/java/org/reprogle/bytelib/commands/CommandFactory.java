package org.reprogle.bytelib.commands;

/**
 * Factory for creating command handler instances via dependency injection.
 * 
 * <p>
 * Implementations should integrate with the DI framework to instantiate and
 * inject
 * dependencies into CommandCallback implementations.
 */
public interface CommandFactory {
    /**
     * Creates an instance of the specified class, injecting dependencies.
     * 
     * @param <T>  the type to instantiate
     * @param type the class to instantiate
     * @return an instance of the class with dependencies injected
     */
    <T> T create(Class<T> type);
}