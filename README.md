# ByteLib

ByteLib is a shared library for building modern, maintainable Paper plugins. This library is explicitly designed with
the experimental Paper Plugin API in mind and comes with the following features:

* Paper-native plugin loading, using Paper's [`PluginBoostrap` and
  `PluginLoader` APIs](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/)
* Guice-based dependency injection
* Modular lifecycle hooks (multiple `onLoad`, `onEnable`, and `onDisable` methods are now possible in your plugin)
* Brigadier-based command registration (no `plugin.yml` commands)
* Config & translation abstractions around [BoostedYAML](https://github.com/dejvokep/boosted-yaml), an already amazing
  YAML library
* A powerful, opinionated SQLite wrapper with support for migrations, strong typings, and more.

To reiterate, ByteLib is **not a plugin**, but rather a foundation that removes boilerplate and enforces safe
architecture across all plugins that use it. ByteLib currently powers
[DimensionPause](https://github.com/TerrorByteTW/DimensionPause), and plans to convert
[Honeypot](https://github.com/TerrorByteTW/Honeypot) to ByteLib are in the works

# Core Design Principles

## 1. Paper-native first

ByteLib uses Paper's modern plugin system, utilizing:

* `paper-plugin.yml`
* `PluginBootstrap`
* `PluginLoader`

Legacy Bukkit patterns have been avoided unless strictly necessary

## 2. Two Injectors (Critical Concept)

ByteLib intentionally splits dependency injection into two Guice injectors:

| Injector                    | Purpose                             |
|-----------------------------|-------------------------------------|
| **Bootstrap Injector**      | Exists *before* the plugin instance |
| **Plugin (Child) Injector** | Exists *after* the plugin instance  |

This avoids Guice accidentally constructing a second plugin instance, which can cause everything from subtle bugs to the
plugin flat-out refusing to enable

### Bootstrap Injector (DO NOT TOUCH THE PLUGIN)

Used only for:

* `PluginMeta`
* `ComponentLogger`
* Plugin Path (data folder)
* Wiring resolution
* _Pure_ services that do not depend on `Plugin`

Never bind or inject:

* `Plugin`
* `JavaPlugin`
* Your concrete plugin class
* Listeners
* Commands
* Schedulers
* Databases

### Plugin (Child) Injector

Created after Paper constructs the plugin instance.

This injector:

* Binds the real Paper-managed plugin instance
* Installs plugin-specific modules
* Runs lifecycle hooks
* Registers commands & listeners
* Owns SQLite, config, timers, etc.

## Bootstrap a Plugin

1. Create a `paper-plugin.yml` in your `src/main/resources` folder (Example from DimensionPause below)

  ```yaml
  name: DimensionPause
  version: "2.0.0"
  main: org.reprogle.dimensionpause.DimensionPausePlugin
  api-version: "1.21"

  bootstrapper: org.reprogle.bytelib.BytePluginBootstrap
  loader: org.reprogle.bytelib.BytePluginLoader
  ```

2. Extend `ByteLibPlugin` in your main class

```java
public final class DimensionPausePlugin extends ByteLibPlugin {
    // No onEnable / onDisable logic here
    // Everything goes in lifecycle hooks
}
```

3. Create a Module which is used to configure Guice bindings (Example from DimensionPause)

```java
public final class DimensionPauseModule extends AbstractModule {

    @Override
    protected void configure() {
        // Core services
        bind(CommandFeedback.class).in(Singleton.class);

        // Subcommands
        Multibinder<SubCommand> subcommandBinder = Multibinder.newSetBinder(binder(), SubCommand.class);
        subcommandBinder.addBinding().to(Reload.class);
        subcommandBinder.addBinding().to(State.class);
        subcommandBinder.addBinding().to(Toggle.class);

        // Lifecycle hooks
        Multibinder<PluginLifecycle> lifecycles = Multibinder.newSetBinder(binder(), PluginLifecycle.class);
        lifecycles.addBinding().to(DimensionPauseLifecycle.class);

        // Commands
        Multibinder.newSetBinder(binder(), CommandRegistration.class)
                .addBinding()
                .to(DPCommandRegistration.class);
    }
}
```

4. Create at least one Lifecycle class, which contains the standard `onEnable`, `onLoad`, and `onDisable` methods.
   Ensure this implements `PluginLifecycle`

```java
public class DimensionPauseLifecycle implements PluginLifecycle {
    private final ComponentLogger logger;

    @Inject
    public DimensionPauseLifecycle(ComponentLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onEnable() {
        logger.info("Dimension Pause has been loaded");
    }

    @Override
    public void onDisable() {
        logger.info("Dimension Pause is shutting down");
    }
}
```

5. Wire up Guice using one of three conventions:

```java
// Create a class named `[MainClass]Wiring.class` and implement PluginWiring
// Ensure it returns your Module you made in step 3
public static class DimensionPauseWiring implements PluginWiring {
    @Override
    public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
        return List.of(
                new DimensionPauseModule()
        );
    }
}

//
// OR
//
// Nest a `Wiring` class in your plugin's main class
public final class DimensionPausePlugin extends ByteLibPlugin {

    @Inject
    public DimensionPausePlugin(Injector injector, PluginMeta meta, Path dataDir, ComponentLogger logger) {
        super(injector, meta, dataDir, logger);
    }

    @SuppressWarnings("unused")
    public static class Wiring implements PluginWiring {
        @Override
        public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
            return List.of(
                    new DimensionPauseModule(),
                    new SqliteModule("dimensionpause.db")
            );
        }
    }
}

//
// OR
//
// Create a class that implements PluginWiring, and allow the `ServiceLoader` to attempt to locate it (Not recommended)
public static class PluginWiringSetup extends PluginWiring {
    @Override
    public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
        return List.of(
                new DimensionPauseModule(),
                new SqliteModule("dimensionpause.db")
        );
    }
}
```

If this seems complex, it's because it is. If DI wasn't involved, this would be much easier,
but DI provides the immediate benefit of decoupling your code, making it easier to develop.

## Using the SQLite API