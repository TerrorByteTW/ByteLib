package org.reprogle.bytelib.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;

public interface Translator {
    Component tr(String key, TagResolver... resolvers);
    Title title(String keyBase, Title.Times times, TagResolver... resolvers);
}
