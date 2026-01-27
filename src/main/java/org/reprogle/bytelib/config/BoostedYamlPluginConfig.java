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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BoostedYamlPluginConfig implements BytePluginConfig {

    private final JavaPlugin plugin;
    private final PluginMeta meta;
    private final Path dataDir;
    private final ComponentLogger logger;

    // Loaded documents by name (ex: "config", "lang", "menus", "items", ...)
    private final Map<String, YamlDocument> docs = new ConcurrentHashMap<>();

    // Registered specs by name
    private final Map<String, YamlSpec> specs = new ConcurrentHashMap<>();

    private volatile String locale = "en_US";

    @Inject
    public BoostedYamlPluginConfig(JavaPlugin plugin, PluginMeta meta, Path dataDirectory, ComponentLogger logger) {
        this.plugin = plugin;
        this.meta = meta;
        this.dataDir = dataDirectory;
        this.logger = logger;

        // Default built-ins
        register("config", YamlSpec.of(dataDir.resolve("config.yml"), "config.yml", "file-version"));

        // lang spec is dynamic (depends on locale), so we register a "template" key here
        // and materialize it during reload() after config has been read.
        // (We still keep it in specs so plugins can introspect.)
        register("lang", YamlSpec.of(dataDir.resolve("lang").resolve("en_US.yml"), "lang/en_US.yml", "language-version"));

        reload(); // eager init is fine because this is child injector only
    }

    // --- Helper methods ---

    @Override
    public YamlDocument config() {
        return require("config");
    }

    @Override
    public YamlDocument lang() {
        return require("lang");
    }

    @Override
    public String locale() {
        return locale;
    }

    // --- Load a specific YAML file ---

    /**
     * Returns a loaded YAML doc by name, or null if not loaded.
     */
    @Override
    public YamlDocument yaml(String name) {
        return docs.get(name);
    }

    /**
     * Returns a loaded YAML doc by name, throwing if missing.
     */
    @Override
    public YamlDocument require(String name) {
        YamlDocument doc = docs.get(name);
        if (doc == null) throw new IllegalStateException("YAML not loaded: " + name + " (" + meta.getName() + ")");
        return doc;
    }

    /**
     * Register (or replace) a YAML spec. Does not load it until reload/reload(name).
     */
    @Override
    public void register(String name, YamlSpec spec) {
        specs.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(spec, "spec"));
    }

    /**
     * Reload just one named YAML, using its registered spec.
     */
    @Override
    public void reload(String name) {
        YamlSpec spec = specs.get(name);
        if (spec == null) throw new IllegalArgumentException("No YAML spec registered for: " + name);

        try {
            Files.createDirectories(spec.outFile().getParent());

            YamlDocument doc = loadYaml(
                    spec.outFile().toFile(),
                    spec.resourcePath(),
                    spec.versionKey(),
                    spec.requiredResource()
            );

            docs.put(name, doc);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load YAML '" + name + "' for " + meta.getName(), e);
        }
    }

    /**
     * Reload everything (config first, then locale, then lang, then any other registered YAML).
     */
    @Override
    public void reload() {
        try {
            Files.createDirectories(dataDir);

            // 1) Load config first (locale depends on it)
            reload("config");

            YamlDocument cfg = require("config");
            String configured = cfg.getString("language");
            boolean bypass = cfg.getBoolean("bypass-language-check");
            this.locale = resolveLocale(configured, bypass);

            // 2) Materialize lang spec for resolved locale, then load it
            Path langDir = dataDir.resolve("lang");
            Files.createDirectories(langDir);

            YamlSpec langSpec = YamlSpec.of(
                    langDir.resolve(locale + ".yml"),
                    "lang/" + locale + ".yml",
                    "language-version"
            );
            // overwrite "lang" spec to point at the active locale file
            specs.put("lang", langSpec);
            reload("lang");

            // 3) Load any other registered docs (but avoid reloading config/lang again)
            for (String name : specs.keySet()) {
                if (name.equals("config") || name.equals("lang")) continue;
                reload(name);
            }

            logger.info("Loaded YAML (locale={}) for {}. Keys={}", locale, meta.getName(), specs.keySet());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load YAML for " + meta.getName(), e);
        }
    }

    private YamlDocument loadYaml(
            File outFile,
            String resourcePath,
            String versionKey,
            boolean requiredResource
    ) throws IOException {

        InputStream resource = resourcePath.isEmpty()
                ? null
                : plugin.getResource(resourcePath);

        if (resource == null) {
            if (requiredResource && !resourcePath.isEmpty()) {
                throw new IllegalStateException("Missing resource in jar: " + resourcePath);
            }

            if (!outFile.exists()) {
                Files.createDirectories(outFile.toPath().getParent());
                Files.writeString(outFile.toPath(), "# Created by " + meta.getName() + "\n");
            }

            resource = new java.io.ByteArrayInputStream(new byte[0]);
        }

        UpdaterSettings updaterSettings =
                (versionKey == null)
                        ? UpdaterSettings.DEFAULT
                        : UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning(versionKey))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                        .build();

        YamlDocument doc = YamlDocument.create(
                outFile,
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                updaterSettings
        );

        doc.update();
        doc.save();
        return doc;
    }


    private String resolveLocale(String configured, boolean bypass) {
        if (configured == null || configured.isBlank()) return "en_US";
        if (bypass) return configured;

        String resourcePath = "lang/" + configured + ".yml";
        try (InputStream ignored = plugin.getResource(resourcePath)) {
            if (ignored != null) return configured;
        } catch (IOException ignored) {
            // nothing
        }

        logger.warn("Unsupported language '{}', defaulting to en_US (missing {})", configured, resourcePath);
        return "en_US";
    }

    /**
     * Small value object describing how to load a YAML.
     */
    public record YamlSpec(
            Path outFile,
            String resourcePath,
            @Nullable String versionKey,
            boolean requiredResource
    ) {
        public static YamlSpec of(Path outFile, String resourcePath, String versionKey) {
            return new YamlSpec(outFile, resourcePath, versionKey, true);
        }

        public static YamlSpec of(Path outFile, String resourcePath) {
            return new YamlSpec(outFile, resourcePath, null, true);
        }

        public static YamlSpec externalOnly(Path outFile) {
            return new YamlSpec(outFile, "", null, false);
        }

        public static YamlSpec externalOnly(Path outFile, String versionKey) {
            return new YamlSpec(outFile, "", versionKey, false);
        }
    }

}
