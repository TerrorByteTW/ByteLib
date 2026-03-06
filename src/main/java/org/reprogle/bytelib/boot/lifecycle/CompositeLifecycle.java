/*
 * Copyright (c) 2026 Nate Reprogle and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

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

