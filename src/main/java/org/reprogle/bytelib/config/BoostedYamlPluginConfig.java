package org.reprogle.bytelib.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class BoostedYamlPluginConfig implements BytePluginConfig {

    private final JavaPlugin plugin;
    private final PluginMeta meta;
    private final Path dataDir;
    private final ComponentLogger logger;

    private YamlDocument config;
    private YamlDocument lang;
    private String locale;

    @Inject
    public BoostedYamlPluginConfig(JavaPlugin plugin, PluginMeta meta, Path dataDirectory, ComponentLogger logger) {
        this.plugin = plugin;
        this.meta = meta;
        this.dataDir = dataDirectory;
        this.logger = logger;

        reload(); // eager init is fine because this is child injector only
    }

    @Override
    public YamlDocument config() {
        return config;
    }

    @Override
    public YamlDocument lang() {
        return lang;
    }

    @Override
    public String locale() {
        return locale;
    }

    @Override
    public void reload() {
        try {
            Files.createDirectories(dataDir);

            this.config = loadYaml(
                    dataDir.resolve("config.yml").toFile(),
                    "config.yml",
                    "file-version"
            );

            String configured = config.getString("language");
            boolean bypass = config.getBoolean("bypass-language-check");

            this.locale = resolveLocale(configured, bypass);

            Path langDir = dataDir.resolve("lang");
            Files.createDirectories(langDir);

            this.lang = loadYaml(
                    langDir.resolve(locale + ".yml").toFile(),
                    "lang/" + locale + ".yml",
                    "language-version"
            );

            logger.info("Loaded config + language (locale={}) for {}", locale, meta.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config/language for " + meta.getName(), e);
        }
    }

    private YamlDocument loadYaml(File outFile, String resourcePath, String versionKey) throws IOException {
        // plugin.getResource returns InputStream or null
        InputStream resource = plugin.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing resource in jar: " + resourcePath);
        }

        YamlDocument doc = YamlDocument.create(
                outFile,
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning(versionKey))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                        .build()
        );

        doc.update();
        doc.save();
        return doc;
    }

    private String resolveLocale(String configured, boolean bypass) {
        if (configured == null || configured.isBlank()) return "en_US";
        if (bypass) return configured;

        // simplest “supported” check: does the resource exist in the jar?
        String resourcePath = "lang/" + configured + ".yml";
        try (InputStream ignored = plugin.getResource(resourcePath)) {
            if (ignored != null) return configured;
        } catch (IOException ignored) {
            // nothing
        }

        logger.warn("Unsupported language '{}', defaulting to en_US (missing {})", configured, resourcePath);
        return "en_US";
    }

}
