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

# Bootstrap Flow (Simplified)
```text
Paper loads plugin
 └─ BytePluginBootstrap.createPlugin()
     ├─ Create bootstrap injector (CoreModule only)
     ├─ Construct plugin instance (Paper-managed)
     ├─ Create child injector:
     │    - PluginInstanceModule
     │    - LifecycleModule
     │    - CommandsModule
     │    - Plugin wiring modules
     ├─ Inject members using child injector
     └─ Return plugin
```
