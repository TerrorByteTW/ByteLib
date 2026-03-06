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

    public SqliteModule(String fileName) {
        this(fileName, SqliteConfig.defaults());
    }

    public SqliteModule(String fileName, SqliteConfig config) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.fixedConfig = Objects.requireNonNull(config, "config");
        this.configFactory = null;
    }

    public SqliteModule(String fileName, Function<BytePluginConfig, SqliteConfig> configFactory) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.fixedConfig = null;
        this.configFactory = Objects.requireNonNull(configFactory, "configFactory");
    }

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

    @Provides
    @Singleton
    public SqliteDatabase sqliteDatabase(JavaPlugin plugin, Path dataDirectory, SqliteConfig config) {
        Path dbFile = dataDirectory.resolve(fileName);
        return new SqliteDatabase(plugin, dbFile, config);
    }
}
