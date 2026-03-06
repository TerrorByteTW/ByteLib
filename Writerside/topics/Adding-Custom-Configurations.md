# Custom Configurations

ByteLib supports registering custom configurations alongside the standard plugin configuration and language files.
Custom configurations can be registered as optional, external-only, and do not require versioning but _do_ support
being versioned, if desired.

## Registering New Configurations

Just like accessing config, to register custom configurations, you must inject `BytePluginConfig` and call `register()`
that is used to register custom configurations.

```java
public void register(String name, YamlSpec spec);
```

`name` is an identifier for your configuration. `spec` is a `BoostedYamlPluginConfig.YamlSpec` record, which contains
some very basic configuration about the configuration file itself.

> The default configuration file and the language file are registered as `config` and `lang`. Because these
> are reserved names, attempting to register a custom configuration with these names will result in an error.
> {style="tip"}

> The name of the configuration does not have to be the same as its path. The name is just an internal reference
> for accessing the configuration object when calling `yaml(String name)`, `reload(String name)`, or
`require(String name)`.

### Required Configs

```java
public static YamlSpec of(Path outFile, String resourcePath);

public static YamlSpec of(Path outFile, String resourcePath, String versionKey);
```

`BoostedYamlPluginConfig.YamlSpec.of` defines a _required_ configuration file. These files exist in the `resources`
folder in the plugin jar and will be copied to the server.

- `outFile` is the path to the file on the server. This is **not** relative to the plugin's data folder, so be cautious!
- `resourcePath` parameter is the path to the file in the plugin jar (relative to the `resources` folder).
- `versionKey` is an optional third parameter, which defines the value in config that defines the config's version, and
  what BoostedYAML will use to determine if the config needs to be updated. This is `file-version` in the default
  config.yaml

### Optional Configs

If you find a need for a config that is not bundled with your plugin and is instead provided by the server
administrator,
you can register it as an external-only configuration file using `externalOnly`.

```java
public static YamlSpec externalOnly(Path outFile);

// Not really useful since an external file means there is no source of truth.
// Versioning externalOnly files is is generally pointless.
public static YamlSpec externalOnly(Path outFile, String versionKey);

```

Unlike `of()`, `externalOnly()` does not take a resource path since there is no resource to look up.

> External-only configurations technically support versioning, but versioning requires a source-of-truth to compare to.
> Since external only files are their own source of truth, versioning is generally pointless.

## Reading Custom Configurations

To read custom configurations, you must know the key of the configuration you want to read. Two methods are provided
to do so: `require(String name)` and `yaml(String name)`.

Both of these methods return the registered YamlDocument if available. However, the primary difference is
`require(String name)` will throw an `IllegalStateException` if the configuration is not found, while
`yaml(String name)` will return `null` if the configuration is not found. Use `require()` if you need to fail loudly.

## Example

Below is an example of how to register a custom configuration file. `ByteLibPlugin` is injected to get the path of the
plugin's data folder. The below example uses a [`PluginLifecycle`](Lifecycle-API.md) to register the configuration during the plugin's
`onEnable` lifecycle stage.

```java
public class MyLifecycleClass implements PluginLifecycle {

    private final BytePluginConfig config;
    private final ByteLibPlugin plugin;

    @Inject
    public MyLifecycleClass(
        BytePluginConfig config,
        ByteLibPlugin plugin
    ) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        config.register(
            "customConfig",
            BoostedYamlPluginConfig.YamlSpec.of(
                plugin.getDataFolder()
                    .toPath()
                    .resolve("customConfig.yml"),
                "customConfig.yml"
            )
        );
    }
}
```