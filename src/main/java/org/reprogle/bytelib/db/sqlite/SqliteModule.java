package org.reprogle.bytelib.db.sqlite;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Objects;

public final class SqliteModule extends AbstractModule {
    private final String fileName;
    private final SqliteConfig config;

    public SqliteModule(String fileName) {
        this(fileName, SqliteConfig.defaults());
    }

    public SqliteModule(String fileName, SqliteConfig config) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.config = Objects.requireNonNull(config, "config");
    }

    @Provides
    @Singleton
    public SqliteConfig sqliteConfig() {
        return config;
    }

    @Provides
    @Singleton
    public SqliteDatabase sqliteDatabase(JavaPlugin plugin, Path dataDirectory, SqliteConfig config) {
        Path dbFile = dataDirectory.resolve(fileName);
        return new SqliteDatabase(plugin, dbFile, config);
    }
}
