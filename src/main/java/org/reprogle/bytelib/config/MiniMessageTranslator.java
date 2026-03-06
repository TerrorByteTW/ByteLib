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

package org.reprogle.bytelib.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public final class MiniMessageTranslator implements Translator {

    private final BytePluginConfig cfg;
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    public MiniMessageTranslator(BytePluginConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public Component tr(String key, TagResolver... resolvers) {
        String raw = cfg.lang().getString(key);
        if (raw == null) return Component.empty();

        List<TagResolver> all = new ArrayList<>(resolvers.length + 1);

        String prefixRaw = cfg.lang().getString("prefix");
        if (prefixRaw != null) {
            all.add(Placeholder.component("prefix", mm.deserialize(prefixRaw)));
        } else {
            all.add(Placeholder.component("prefix", Component.empty()));
        }

        all.addAll(Arrays.asList(resolvers));

        return mm.deserialize(raw, TagResolver.resolver(all));
    }

    @Override
    public Title title(String keyBase, Title.Times times, TagResolver... resolvers) {
        Component title = tr(keyBase + ".title", resolvers);
        Component subtitle = tr(keyBase + ".subtitle", resolvers);
        return Title.title(title, subtitle, times);
    }
}

