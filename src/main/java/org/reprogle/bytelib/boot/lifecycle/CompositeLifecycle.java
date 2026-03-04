package org.reprogle.bytelib.boot.lifecycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Singleton
public final class CompositeLifecycle implements PluginLifecycle {
    private final List<PluginLifecycle> lifecycles;

    @Inject
    public CompositeLifecycle(
            @InternalLifecycles Set<PluginLifecycle> internalLifecycles,
            Set<PluginLifecycle> externalLifecycles
    ) {
        this.lifecycles = Stream.concat(
                        sortByPriority(internalLifecycles).stream(),
                        sortByPriority(externalLifecycles).stream()
                )
                .toList();
    }

    private List<PluginLifecycle> sortByPriority(Set<PluginLifecycle> lifecycles) {
        return lifecycles.stream()
                .sorted(Comparator.comparing(this::priorityOf))
                .toList();
    }

    private PluginLifecycle.Priority priorityOf(PluginLifecycle lifecycle) {
        LifecyclePriority annotation = lifecycle.getClass().getAnnotation(LifecyclePriority.class);
        return annotation == null ? PluginLifecycle.Priority.NORMAL : annotation.value();
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

