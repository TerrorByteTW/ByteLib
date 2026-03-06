# Getting Started

To better understand the design of ByteLib and how it works, a good place to start is to review
the [core design principles](Core-Design-Principles.md) of the library. If you're not interested in the technical
nitty-gritty, you can skip this.

ByteLib is built based off of the
experimental [Paper Plugin API](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/). This API requires
some manual configuration on the part of every developer, so ByteLib has this process well-documented.

- Review the [Bootstrapping](Bootstrapping.md) topic to learn how to <tooltip term="bootstrap">bootstrap</tooltip> your
  plugin to use ByteLib.
- Review the [Plugin Loader](Plugin-Loader.md) documentation to learn how the Paper `PluginLoader` works in ByteLib.

## ByteLib APIs

ByteLib provides a number of APIs that make plugin development easier. All of ByteLib's APIs keep Dependency Injection
at their center, making it extremely easy to integrate them anywhere in your plugin.

To learn about these APIs, head to their respective topics.

### Config API

The [Config API](Configuration-Translation.md) is an opinionated, lightweight wrapper around the
BoostedYAML library for more easily interacting with configuration files.

This API includes utility classes for handling translations as well and natively supports MiniMessage.

### Lifecycles API

The [Lifecycles API](Lifecycle-API.md) allows ByteLib plugins to contain any number of `onLoad`, `onEnable`, and
`onDisable` lifecycle hooks, allowing for thinner main classes and separating responsibilities.

ByteLib lifecycles are able to be executed in a defined order if developers need.

### Commands API

The [Commands API](Commands-API.md) allows developers to easily create and register Brigadier commands for their plugin.

While similar to Brigadier, the ByteLib Commands API provides a more opinionated API for creating commands, as well as
some handy features. Of course, with all ByteLib APIs, Google Guice Dependency Injection is first-class.

### SQLite API

One of ByteLib's best APIs, the [SQLite API](SQLite-API.md) allows developers to easily create and interact with SQLite
databases in a type-safe, object-oriented manner, all without removing the freedom of raw SQL.