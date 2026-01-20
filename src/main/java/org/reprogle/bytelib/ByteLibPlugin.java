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