package org.reprogle.bytelib.config;

import dev.dejvokep.boostedyaml.YamlDocument;

public interface BytePluginConfig {
    YamlDocument config();
    YamlDocument lang();

    String locale();
    void reload();
}
