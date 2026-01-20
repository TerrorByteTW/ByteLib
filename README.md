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

Check out the [Wiki](https://github.com/TerrorByteTW/ByteLib/wiki) to get started