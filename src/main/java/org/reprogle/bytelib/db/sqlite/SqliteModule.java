package org.reprogle.bytelib.db.sqlite;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

public final class SqliteModule extends AbstractModule {
    private final String fileName;
    private final SqliteConfig fixedConfig;
    private final Function<BytePluginConfig, SqliteConfig> configFactory;

    @SuppressWarnings("unused")
    public SqliteModule(String fileName) {
        this(fileName, SqliteConfig.defaults());
    }

    public SqliteModule(String fileName, SqliteConfig config) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.fixedConfig = Objects.requireNonNull(config, "config");
        this.configFactory = null;
    }

    @SuppressWarnings("unused")
    public SqliteModule(String fileName, Function<BytePluginConfig, SqliteConfig> configFactory) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.fixedConfig = null;
        this.configFactory = Objects.requireNonNull(configFactory, "configFactory");
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    public SqliteConfig sqliteConfig(Injector injector) {
        if (configFactory == null) {
            return fixedConfig;
        }

        Key<BytePluginConfig> configKey = Key.get(BytePluginConfig.class);
        if (injector.getExistingBinding(configKey) == null) {
            throw new IllegalStateException(
                    "SqliteModule requires BytePluginConfig when using configFactory. " +
                            "Ensure ConfigModule is included in the same injector."
            );
        }

        BytePluginConfig bytePluginConfig = injector.getInstance(BytePluginConfig.class);
        SqliteConfig resolved = configFactory.apply(bytePluginConfig);
        return Objects.requireNonNull(resolved, "configFactory returned null");
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    public SqliteDatabase sqliteDatabase(JavaPlugin plugin, Path dataDirectory, SqliteConfig config) {
        Path dbFile = dataDirectory.resolve(fileName);
        return new SqliteDatabase(plugin, dbFile, config);
    }
}
