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

package org.reprogle.bytelib;

import com.google.inject.Injector;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycleRunner;

import java.nio.file.Path;
import java.util.Objects;

public class ByteLibPlugin extends JavaPlugin {
    private Injector injector;
    protected final PluginMeta meta;
    protected final Path dataDir;
    protected final ComponentLogger logger;

    protected ByteLibPlugin(Injector bootstrapInjector, PluginMeta meta, Path dataDir, ComponentLogger logger) {
        this.injector = Objects.requireNonNull(bootstrapInjector, "bootstrapInjector");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public final Injector injector() {
        return injector;
    }

    public final void attachInjector(Injector pluginInjector) {
        if (this.injector == null) throw new IllegalStateException("Injector already cleared?");
        // Prevent double-set
        if (this.injector == pluginInjector) return;
        this.injector = Objects.requireNonNull(pluginInjector, "pluginInjector");
    }

    @Override
    public final void onLoad() {
        injector().getInstance(PluginLifecycleRunner.class).load();
    }

    @Override
    public final void onEnable() {
        injector().getInstance(PluginLifecycleRunner.class).enable();
    }

    @Override
    public final void onDisable() {
        injector().getInstance(PluginLifecycleRunner.class).disable();
    }

}