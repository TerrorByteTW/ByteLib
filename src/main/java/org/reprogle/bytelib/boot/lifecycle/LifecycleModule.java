package org.reprogle.bytelib.boot.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class LifecycleModule extends AbstractModule {

    @Override
    protected void configure() {
        // External/plugin-contributed lifecycles (default set for compatibility).
        Multibinder.newSetBinder(binder(), PluginLifecycle.class);
        // ByteLib internal lifecycles.
        Multibinder.newSetBinder(binder(), PluginLifecycle.class, InternalLifecycles.class);

        // Core runner & fan-out
        bind(CompositeLifecycle.class);
        bind(PluginLifecycleRunner.class);
    }
}
