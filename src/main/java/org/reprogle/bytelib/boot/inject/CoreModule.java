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
