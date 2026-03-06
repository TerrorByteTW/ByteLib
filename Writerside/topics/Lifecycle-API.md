# Lifecycles API

The Lifecycles API allows plugins to have more than one `onEnable()`, `onDisable()`, and `onLoad()` method, allowing
decoupling of your plugin's lifecycle code. This allows you to have different parts of your plugin initialized
separately without having one massive `onLoad()` or `onEnable()` method. Some developers may simply separate these into
separate functions methods, but ByteLib goes a step further and separates them into separate classes entirely.