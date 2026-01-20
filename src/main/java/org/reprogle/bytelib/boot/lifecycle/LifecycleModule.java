package org.reprogle.bytelib.boot.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class LifecycleModule extends AbstractModule {

    @Override
    protected void configure() {
        // This enables Set<PluginLifecycle> injection
        Multibinder.newSetBinder(binder(), PluginLifecycle.class);

        // Core runner & fan-out
        bind(CompositeLifecycle.class);
        bind(PluginLifecycleRunner.class);
    }
}
