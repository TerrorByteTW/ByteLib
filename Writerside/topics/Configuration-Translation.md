# Config API
<primary-label ref="subject-to-change"/>

The Config API is a mandatory API in ByteLib. It's a lightweight wrapper
around [BoostedYAML](https://github.com/dejvokep/boosted-yaml). It supports reading and writing YAML files in a highly
opinionated manner and also provides utilities for handling i18n.

Because the Config API only wraps BoostedYAML, its primary purpose is to make it easier to set up and use
BoostedYAML configurations. It is **not** its own Config framework.

> You may see the phrases "language file", "translation file" and/or "translation" used interchangeably. They all refer
> to the same thing: a file that contains translations for a plugin's messages.
> {style="tip"}

See [](Creating-Configuration-Files.md)to get started.

## Plans for Future Updates {collapsible="true"}

BoostedYAML is a great library that I've used for years, and I've even contributed to it! However, I want ByteLib to
need less manual setup for Config API, and that means using something more modern.

**I've decided that, in ByteLib v2, BoostedYAML will be replaced with [ConfigLib](https://github.com/Exlll/ConfigLib).**

Note that ConfigLib uses code to generate config files, while BoostedYAML uses Bukkit's `resources` folder. As a result,
the Config API _will_ need to be reworked entirely to use ConfigLib's code generation system. The benefit of this will
be that it is more flexible and customizable, and less manual setup will be needed to use Config API.

ByteLib will, of course, make it as easy as possible for developers to use the new Config API if they so choose.