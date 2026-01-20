package org.reprogle.bytelib.lifecycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class PluginLifecycleRunner {
    private final CompositeLifecycle lifecycle;

    @Inject
    public PluginLifecycleRunner(CompositeLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void load() {
        lifecycle.onLoad();
    }

    public void enable() {
        lifecycle.onEnable();
    }

    public void disable() {
        lifecycle.onDisable();
    }
}
