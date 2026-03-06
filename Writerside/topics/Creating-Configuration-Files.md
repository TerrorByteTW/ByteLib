# Creating Configuration Files

<secondary-label ref="wip"/>

Developers are free to do whatever they want with their configuration files, but are required to provide a couple of
values at minimum.

1. If you haven't already, in your `src/main/resources` folder create a `config.yml` file and a `lang/en_US.yml`
   folder/file. ByteLib will handle generating these on disk when the plugin first runs.
2. In your `config.yml` file, add three properties:
    1. A `language` string value, defaulting to `en_US`
    2. A `bypass-language-check` boolean value defaulting to false
    3. A `file-version` number value, defaulting to 1.
3. In your `lang/en_US.yml` file, add a single property:
    1. A `language-version` number value, defaulting to 1.

```yaml
# config.yml
file-version: 1
language: en_US
bypass-language-check: false

# lang/en_US.yml
language-version: 1
```

> ByteLib allows server owners to create their own translation files to crowdsource translations and customize the look
> of the plugin's chat messages in-game. However, to _use these_ custom translations (if the plugin doesn't natively
> support them), the `bypass-language-check` must be `true`. If a user sets this to `true`, protections against unknown
> language files will be removed, and they may experience unknown behavior.
>
> Once set to `true`, upload a `.yml` file with the name of the language you wish to use, then set `language` in config
> to that filename (minus the extension). This translation will be loaded automatically.

### Upgrading your config plugins

As you upgrade your plugin, you may find the need to add or modify values in your configuration. When you do, bump the
version number up by 1.

> It's recommended to add a comment in your config file regarding the version number and to not change it. Server admins
> changing these values can break their configs.

## Registering custom configuration files

To add custom configuration files alongside the standard plugin config and language files, inject `BytePluginConfig` and
call the `register()` method within in. `register()` takes two arguments, the first being the name of the configuration
file and the second being a `BoostedYamlPluginConfig.YamlSpec` record. `BoostedYamlPluginConfig.YamlSpec` defines the
actual path of the file, the resource path within the plugin, the version key (If the file is versioned), and if it's a
required file. Note that the name of the configuration is not necessary the file name/path. This allows for shorter
names if you plan on referencing them internally quite a bit.

Below is an example of how the Honeypot plugin registers its `honeypots.yml` file:

<code-block lang="java" noinject="true">
// `plugin` is a `JavaPlugin` injected via the constructor in the lifecycle class.
config.register("honeypots",BoostedYamlPluginConfig.YamlSpec.of(plugin.getDataFolder().toPath().resolve("honeypots.yml"), "honeypots.yml"));
</code-block>

Check the Javadoc to see how you can register non-mandatory config files.

## Accessing configuration files

To access the `config.yml` file, inject `BytePluginConfig` and call the `config()` method on it. To access the language
file, inject `BytePluginConfig` and call the `lang()` method on it.

If you need to a custom config file, you may call either `yaml(String name)` or `require(String name)`. `yaml()` will
return either the YamlDocument or null if it's not loaded, while `require()` will throw an exception if the config is
not loaded. `yaml()` should be used if the config is optional and may not exist, `require()` should be used if it's
necessary for the config to exist for the plugin to function.

A utility method, `locale()`, exists if you wish to obtain the current locale being used.

## Reloading config files

You can reload config files by calling `reload()`, which reloads all config files, or calling `reload(String name)` to
reload a specific registered config file.
