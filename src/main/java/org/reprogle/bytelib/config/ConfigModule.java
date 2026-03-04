package org.reprogle.bytelib.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Guice module that configures bindings for BytePluginConfig and Translator.
 * This is an internal module to ByteLib, you should not instantiate
 * this module directly.
 */
public class ConfigModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BytePluginConfig.class).to(BoostedYamlPluginConfig.class).in(Scopes.SINGLETON);
        bind(Translator.class).to(MiniMessageTranslator.class).in(Scopes.SINGLETON);
    }
}
