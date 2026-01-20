package org.reprogle.bytelib.boot.wiring;

import com.google.inject.Module;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.util.List;

public interface PluginWiring {
    List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger);
}
