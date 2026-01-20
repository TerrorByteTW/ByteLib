package org.reprogle.bytelib.boot.inject;

import com.google.inject.AbstractModule;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.ByteLibPlugin;

public class PluginInstanceModule extends AbstractModule {
    private final ByteLibPlugin plugin;

    public PluginInstanceModule(ByteLibPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void configure() {
        bind(ByteLibPlugin.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);
        bind(Plugin.class).toInstance(plugin);

        Class concrete = plugin.getClass();
        bind(concrete).toInstance(plugin);

    }
}
