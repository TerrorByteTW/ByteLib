package org.reprogle.bytelib.boot.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;

public final class CoreModule extends AbstractModule {

    private final PluginMeta meta;
    private final Path dataDir;
    private final ComponentLogger logger;

    public CoreModule(PluginMeta meta, Path dataDir, ComponentLogger logger) {
        this.meta = meta;
        this.dataDir = dataDir;
        this.logger = logger;
    }

    @Provides
    @Singleton
    public PluginMeta pluginMeta() {
        return meta;
    }

    @Provides
    @Singleton
    public Path dataDirectory() {
        return dataDir;
    }

    @Provides
    @Singleton
    public ComponentLogger logger() {
        return logger;
    }
}
