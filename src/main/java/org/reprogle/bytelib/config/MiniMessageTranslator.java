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

