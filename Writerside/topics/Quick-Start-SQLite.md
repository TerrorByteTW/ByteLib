# Quick Start

To get started, in your plugin's [wiring class](Bootstrapping.md#bootstrapping-in-detail), add `SqliteModule()` to your
`modules()` function.

```java
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
```

`new SqliteModule()` takes between 1 and 2 parameters. The first is the name of your DB file, and the second is any
options you want to provide. The second parameter is a `SqliteConfig` record. Check
the [configuration](Configuring-SQLite-API.md) for more info on this.
