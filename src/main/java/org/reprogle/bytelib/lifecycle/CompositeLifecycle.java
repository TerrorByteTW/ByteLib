package org.reprogle.bytelib.lifecycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Set;

@Singleton
public final class CompositeLifecycle implements PluginLifecycle {
    private final Set<PluginLifecycle> lifecycles;

    @Inject
    public CompositeLifecycle(Set<PluginLifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    @Override
    public void onLoad() {
        lifecycles.forEach(PluginLifecycle::onLoad);
    }

    @Override
    public void onEnable() {
        lifecycles.forEach(PluginLifecycle::onEnable);
    }

    @Override
    public void onDisable() {
        lifecycles.forEach(PluginLifecycle::onDisable);
    }
}
