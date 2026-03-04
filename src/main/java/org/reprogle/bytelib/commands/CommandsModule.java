package org.reprogle.bytelib.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.reprogle.bytelib.boot.lifecycle.InternalLifecycles;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

/**
 * Guice module that configures command system bindings.
 * This is an internal module to ByteLib, you should not instantiate
 * this module directly.
 * 
 * <p>
 * Sets up the following:
 * <ul>
 * <li>CommandFactory binding to GuiceCommandFactory for DI-based callback
 * instantiation</li>
 * <li>Multibinder for CommandRegistration implementations</li>
 * <li>CommandsLifecycle registration for plugin enable hook</li>
 * </ul>
 */
public final class CommandsModule extends AbstractModule {
    /**
     * Configures the Guice bindings for the command system.
     */
    @Override
    protected void configure() {
        // Existing extension point remains.
        Multibinder.newSetBinder(binder(), CommandRegistration.class);

        // New: default DI-backed command callback factory.
        bind(CommandFactory.class).to(GuiceCommandFactory.class);

        // Existing lifecycle hook remains.
        Multibinder.newSetBinder(binder(), PluginLifecycle.class, InternalLifecycles.class)
                .addBinding()
                .to(CommandsLifecycle.class);
    }
}