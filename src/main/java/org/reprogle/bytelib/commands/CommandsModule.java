package org.reprogle.bytelib.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.reprogle.bytelib.lifecycle.PluginLifecycle;

public final class CommandsModule extends AbstractModule {
    @Override
    protected void configure() {
        // Allow plugins to contribute command registrations
        Multibinder.newSetBinder(binder(), CommandRegistration.class);

        // Ensure the lifecycle hook runs
        Multibinder.newSetBinder(binder(), PluginLifecycle.class)
                .addBinding()
                .to(CommandsLifecycle.class);
    }
}
