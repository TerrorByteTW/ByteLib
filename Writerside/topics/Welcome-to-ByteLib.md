# Welcome to ByteLib

> This wiki is a work in progress! If you have questions or find issues, please report them by opening an issue on
> GitHub.
> {style="note"}

## About ByteLib

If you're here, that probably means you're interested in learning about ByteLib! Welcome aboard 😄

I wrote ByteLib because I found myself writing a ton of complex boilerplate and I wanted to centralize it into an
easy-to-use framework.
I also wanted to push my development limits and design something that I've never done before.

ByteLib was born from that drive to create the most useful possible framework without sacrificing the flexibility and
power of the language.

## What is ByteLib?

ByteLib is a shared library for building modern, maintainable _Paper-native_ plugins. This library is explicitly
designed with the experimental Paper Plugin API in mind and comes with the following features:

* Paper-native plugin loading, using Paper's [`PluginBoostrap` and
  `PluginLoader` APIs](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/)
* Guice-based dependency injection
* Modular lifecycle hooks (multiple `onLoad`, `onEnable`, and `onDisable` methods are now possible in your plugin)
* Config & translation abstractions around [BoostedYAML](https://github.com/dejvokep/boosted-yaml), an already amazing
  YAML library
* A powerful, opinionated SQLite wrapper with support for migrations, strong typings, and more.
* A wrapper around Brigadier which simplifies writing Brigadier commands while providing the same Guice-based dependency
  injection.

To reiterate, ByteLib is **not a plugin**, but rather a foundation that removes boilerplate and enforces safe
architecture across all plugins that use it. ByteLib currently powers
[DimensionPause](https://github.com/TerrorByteTW/DimensionPause) and
[Honeypot](https://github.com/TerrorByteTW/Honeypot)!

## What's next?
To get started with ByteLib, check out the [Getting Started guide](Getting-Started.md)!
