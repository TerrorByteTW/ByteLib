# ByteLib Bootstrapper

The ByteLib bootstrapper is the heart of ByteLib. It handles configuring Google Guice for dependency injection, handling
lifecycle hooks, registering commands, and so much more. ByteLib lives and dies by its bootstrapper.

> ByteLib's bootstrapper is **_mandatory_** to use ByteLib, as all ByteLib APIs depend on it. ByteLib's bootstrapper
> cannot be substituted or configured. If you wish to use your own bootstrapper, you cannot use ByteLib.
> {style="warning"}

## Bootstrap a Plugin

"Bootstrapping a plugin" with ByteLib just means setting up your plugin to use ByteLib. Because Paper Plugins define the
bootstrapper and loader via config, you must manually set this up for your plugins that use ByteLib.

The process is as follows:

1. Create a `paper-plugin.yml` and list ByteLib's boostraper and loader classes
2. Extend `ByteLibPlugin` on your main class (It can remain empty, and is recommended)
3. Create a Google Guice module to register your plugin's dependencies and hook up DI
4. Create one or more Lifecycle classes to do work during the `onLoad`, `onEnable`, and `onDisable` Paper lifecycles
5. Create a wiring class to hook it all together

## Bootstrapping in Detail

1. Create a `paper-plugin.yml` in your `src/main/resources` folder (Example from DimensionPause below)

<include from="code_snippets.topic" element-id="paper-plugin" />

The <tooltip term="bootstrap">`bootstrapper`</tooltip> in a Paper Plugin allows you to change the way your plugin is
initialized, allowing you to pass
values into your plugin constructor. In ByteLib, the `bootstrapper` is responsible for configuring all the internal
Guice modules, injecting dependencies, and more. The ByteLib bootstrapper requires the main class of your plugin to
extend `ByteLibPlugin`, which will be done in the next step.

The <tooltip term="loader">`loader`</tooltip> in a Paper Plugin allows the creation of an expected/dynamic environment
for your plugin to load into.
According to Paper's docs, this only applies to creating the expected classpath for the plugin, e.g. supplying external
libraries to the plugin. As of now, ByteLib's loader only adds the BoostedYAML and Google Guice dependencies into the
plugin's classpath. It is _not_ configurable yet, but plans to make this configurable in the future are in the works.

ByteLib's loader _is not mandatory_ as long as you ensure your plugin's loader loads the necessary dependencies itself.
However, for most developers, it's recommended to use it anyway.

2. Extend `ByteLibPlugin` in your main class

<include from="code_snippets.topic" element-id="bytelib-plugin" />

> **DO NOT EXPECT** to be able to inject your plugin's main class! ByteLib binds `JavaPlugin`, `Plugin`, and your
> plugin's main class to `ByteLibPlugin`.
> While you _may_ inject those classes, it's recommended that you inject `ByteLibPlugin` instead.
> {style="warning"}

3. Create a Module which is used to configure Guice bindings (Example from DimensionPause)

<include from="code_snippets.topic" element-id="bytelib-module" />

4. Create at least one Lifecycle class, which contains the standard `onEnable`, `onLoad`, and `onDisable` methods.
   Ensure this implements `PluginLifecycle`

<include from="code_snippets.topic" element-id="bytelib-lifecycle" />

5. Wire up Guice using one of three conventions:

<include from="code_snippets.topic" element-id="bytelib-wiring" />

If this seems complex, it's because it is. If DI wasn't involved, this would be much easier,
but DI provides the immediate benefit of decoupling your code, making it easier to develop.

## Next Steps

After you've bootstrapped your plugin, be sure to configure the [loader](Plugin-Loader.md) and
the [Config API](Configuration-Translation.md). ByteLib requires the Config API to be configured
before it can properly bootstrap your plugin.
