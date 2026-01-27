package org.reprogle.bytelib.config;

import dev.dejvokep.boostedyaml.YamlDocument;

public interface BytePluginConfig {
    YamlDocument config();

    YamlDocument lang();

    String locale();

    YamlDocument yaml(String name);

    YamlDocument require(String name);

    void register(String name, BoostedYamlPluginConfig.YamlSpec spec);

    void reload(String name);

    void reload();
}
