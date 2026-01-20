package org.reprogle.bytelib;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.reprogle.bytelib.commands.CommandsModule;
import org.reprogle.bytelib.boot.inject.CoreModule;
import org.reprogle.bytelib.boot.inject.PluginInstanceModule;
import org.reprogle.bytelib.boot.lifecycle.LifecycleModule;
import org.reprogle.bytelib.boot.wiring.PluginWiring;
import org.reprogle.bytelib.boot.wiring.WiringResolver;
import org.reprogle.bytelib.config.ConfigModule;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "UnstableApiUsage", "unused" })
public final class BytePluginBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLogger().debug("Bootstrapping {}", context.getConfiguration().getName());
    }

    @Override
    public @NonNull JavaPlugin createPlugin(PluginProviderContext context) {
        PluginMeta meta = context.getConfiguration();
        Path dataDir = context.getDataDirectory();
        ComponentLogger logger = context.getLogger();

        String mainClassName = meta.getMainClass();
        ClassLoader cl = this.getClass().getClassLoader();

        PluginWiring wiring = WiringResolver.resolve(cl, mainClassName)
                .orElseThrow(() -> new IllegalStateException("No PluginWiring found for " + mainClassName));

        Injector bootstrapInjector = Guice.createInjector(new CoreModule(meta, dataDir, logger));

        Class<? extends JavaPlugin> pluginClass = loadPluginMain(cl, mainClassName);
        JavaPlugin rawPlugin = constructPlugin(pluginClass, bootstrapInjector, meta, dataDir, logger);

        if (!(rawPlugin instanceof ByteLibPlugin plugin)) {
            throw new IllegalStateException("Main class must extend ByteLibPlugin: " + mainClassName);
        }

        List<Module> childModules = new ArrayList<>();
        childModules.add(new PluginInstanceModule(plugin));
        childModules.add(new ConfigModule());
        childModules.add(new LifecycleModule());
        childModules.add(new CommandsModule());
        childModules.addAll(wiring.modules(meta, dataDir, logger));

        Injector pluginInjector = bootstrapInjector.createChildInjector(childModules);

        plugin.attachInjector(pluginInjector);
        pluginInjector.injectMembers(plugin);

        return plugin;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends JavaPlugin> loadPluginMain(ClassLoader cl, String mainClassName) {
        try {
            Class<?> raw = Class.forName(mainClassName, true, cl);
            if (!JavaPlugin.class.isAssignableFrom(raw)) {
                throw new IllegalStateException("Main class does not extend JavaPlugin: " + mainClassName);
            }
            return (Class<? extends JavaPlugin>) raw;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load main class: " + mainClassName, e);
        }
    }

    private JavaPlugin constructPlugin(
            Class<? extends JavaPlugin> pluginClass,
            Injector injector,
            PluginMeta meta,
            Path dataDir,
            ComponentLogger logger) {
        try {
            Constructor<? extends JavaPlugin> ctor = pluginClass.getDeclaredConstructor(
                    com.google.inject.Injector.class,
                    io.papermc.paper.plugin.configuration.PluginMeta.class,
                    java.nio.file.Path.class,
                    net.kyori.adventure.text.logger.slf4j.ComponentLogger.class);
            ctor.setAccessible(true);
            return ctor.newInstance(injector, meta, dataDir, logger);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Plugin main class must declare a constructor: " +
                            pluginClass.getName() +
                            "(Injector, PluginMeta, Path, ComponentLogger)",
                    e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate plugin: " + pluginClass.getName(), e);
        }
    }
}
