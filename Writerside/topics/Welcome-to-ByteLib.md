# Welcome to ByteLib

> This wiki is a work in progress! If you have questions or find issues, please report them by opening an issue on
> GitHub.
> {style="note"}

ByteLib is a shared library I originally wrote for centralizing boilerplate into a single location. However, it's grown
into a full-fledged framework for building modern, maintainable _Paper-native_ plugins.

ByteLib is _explicitly_ designed with the experimental Paper Plugin API in mind and comes with the following
features:

* Paper-native plugin loading, using Paper's [`PluginBoostrap` and
  `PluginLoader` APIs](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/)
* Guice-based dependency injection
* Modular lifecycle hooks (enables multiple `onLoad`, `onEnable`, and `onDisable` methods)
* Config & translation abstractions around [BoostedYAML](https://github.com/dejvokep/boosted-yaml)
* A powerful, opinionated SQLite API with support for migrations, strong typings, and more.
* A wrapper around Brigadier to simplify command authoring.

To reiterate, ByteLib is **not a plugin**, but rather a foundation that removes boilerplate and enforces safe
architecture across all plugins that use it. ByteLib currently powers
[DimensionPause](https://github.com/TerrorByteTW/DimensionPause) and
[Honeypot](https://github.com/TerrorByteTW/Honeypot)!

## Why ByteLib?

If you want:

- A paper-native plugin
- First-class support for Dependency Injection
- Clean, modular code
- Less boilerplate

Then ByteLib might be for you!

[Paper](https://papermc.io/) is _by far_ the most used Minecraft server software, but as far as I know, there are no
frameworks in existence
that target Paper specifically. So,
when [Paper forked from Spigot in December 2024](https://forums.papermc.io/threads/the-future-of-paper-hard-fork.1451/),
it made sense to create a framework that was designed to be Paper-native as well.

As plugins get larger and more complex, it's important to ensure that they are maintainable and scalable. Unfortunately,
most developers do not do this, and many plugins (including my own) become
a [spaghettified](https://en.wikipedia.org/wiki/Spaghettification) mess.

ByteLib provides a robust foundation for building plugins that can handle these challenges. The biggest benefit of
ByteLib is its forced use of Google's Guice library, an amazing dependency injection framework. This allows developers
to [turn over control](https://en.wikipedia.org/wiki/Inversion_of_control) of their plugin's
dependencies to the framework rather than handle it themselves, which greatly improves modularity, flexibility, and
maintainability.

ByteLib was designed to ensure DI is available **everywhere**, even in your plugin's main class! As well,
ByteLib includes powerful APIs for handling the most common tasks plugin developers face, like persistent
storage, configuration, i18n, command authoring, and more. All of this means that no matter how complex your plugin
is, you can maintain a clean, scalable codebase from `onLoad()` to `onDisable()` 😁

## What's next?

To get started with ByteLib, check out the [Getting Started guide](Getting-Started.md)!
