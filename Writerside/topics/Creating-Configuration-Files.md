# Setting up Config API

<secondary-label ref="wip"/>

The Config API is extremely opinionated. Developers are free to do whatever they want with their configuration files,
_but_ they are required to at least provide two files for Config API to function:

- `config.yml` is the primary configuration file for your plugin
- `lang/en_US.yml` is the default language file for your plugin. ByteLib plugins support any number of languages the
  developer wants to add, but `en_US` is mandatory.

In addition to creating these files, three properties are mandatory in `config.yml` and one property is mandatory in
`lang/en_US.yml` (and every other language file, as well):

- `file-version`: A number that determines the "version" of the default config. This is used to auto-update config on
  the server.
- `language`: A string that tells the Config API which language file to use for translations. This should always default
  to `en_US`.
- `bypass-language-check`: A boolean that determines whether ByteLib will validate the language is supported or not at
  runtime. This allows for custom language files to be used and ensures protection against unsupported files. This
  should always default to `false`.
- `language-version`: A number that determines the "version" of the language/translation file. This is used to
  auto-update translations on the server.

## Setting up the files in your project

1. If you haven't already, in your `src/main/resources` folder create a `config.yml` file and a folder called `lang`.
2. Inside the `lang` folder, create a file called `en_US.yml` folder/file.
3. In your `config.yml` file, add the three properties listed above.
4. In your `lang/en_US.yml` file, add the single property listed above.

Your files should look like this.

```yaml
# config.yml
file-version: 1
language: en_US
bypass-language-check: false

# lang/en_US.yml
language-version: 1
```

To create additional translations, add a new `.yml` file to the `resources/lang` folder in your project. ByteLib handles
automatic discovery of language files, so you don't need to worry about marking a language as "supported" anywhere. If
the file exists, it's supported.

### Allowing custom translations

ByteLib allows server owners to create their own translation files to crowdsource translations and customize the look
of the plugin's chat messages in-game. However, to _use these_ custom translations (if the plugin doesn't natively
support them), the `bypass-language-check` must be `true`. If a user sets this to `true`, protections against unknown
language files will be removed, and they may experience unknown behavior.

Once set to `true`, upload a `.yml` file with the name of the language you wish to use to the server's
`plugins/[plugin]/lang` folder, then set `language` in `config.yml` to that filename (minus the extension). This
translation will be loaded automatically.

### Upgrading your config

As you upgrade your plugin, you will eventually find the need to add or modify values in your configuration.

1. Add the updated properties, comments, etc. to your config file.
2. Increment the `file-version` number by 1 (you can really increment it however much you want, but 1 is a sane default)

> It's recommended to add a comment in your config file regarding the version number and to not change it. Server admins
> changing these values can break their configs.

## Using config

When you need to access config or your language file, you need to inject `BytePluginConfig` into your class.

`BytePluginConfig` has two methods on it: `config()` and `lang()`. `config()` returns the default config file, and
`lang()` returns the currently active language file. The return of these objects is a BoostedYAML `YamlDocument` object.
From there, you'll want to reference BoostedYAML's documentation for how to set and get values and properties.

If you need to access a [Custom Config File](Adding-Custom-Configurations.md), you may call either `yaml(String name)`
or `require(String name)`. `yaml()` will return either the YamlDocument or null if it's not loaded, while `require()`
will throw an exception if the config is not loaded. `yaml()` should be used if the config is optional and may not
exist, `require()` should be used if it's necessary for the config to exist for the plugin to function.

A utility method, `locale()`, exists if you wish to get the *name* of the current locale being used, such as `en_US`.

> The default configuration file is registered under the name `config`, and the default language file is registered as
> `lang`. The `config()` and `lang()` functions are simply convenience wrappers for `yaml("config")` and `yaml("lang")`.
> You are not required to use these helpers, they are provided only as utility methods.
>
> For more details on what it means for a file to be "registered" under a specific name, see
> the [Custom Config Files](Adding-Custom-Configurations.md) documentation
> {style="note"}

## Reloading config files

To reload a config file, call `reload(String name)`, where `name` is the key of the config file you wish to reload.

If you wish to reload all config files at once, you can call `reload()`. This method will reload the default config
file, the translation file (loading the new langauge file if the `language` was changed), and any registered custom
configuration files.
