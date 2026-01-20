package org.reprogle.bytelib.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ConfigModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BytePluginConfig.class).to(BoostedYamlPluginConfig.class).in(Scopes.SINGLETON);
        bind(Translator.class).to(MiniMessageTranslator.class).in(Scopes.SINGLETON);
    }
}
