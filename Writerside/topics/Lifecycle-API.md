# Lifecycles API
<secondary-label ref="wip"/>

The Lifecycles API allows plugins to have more than one `onEnable()`, `onDisable()`, and `onLoad()` method, allowing
decoupling of your plugin's lifecycle code. This allows you to have different parts of your plugin initialized
separately without having one massive `onLoad()` or `onEnable()` method. Some developers may simply separate these into
separate functions or classes, but ByteLib goes a step further and separates them into separate classes entirely.

## Using the Lifecycles API

Using this API is extremely easy. Implement the `PluginLifecycle` interface from ByteLib and override any of the
standard Paper lifecycle events. The constructor for your lifecycle supports Dependency Injection, as do all ByteLib
APIs. As with Paper's lifecycle system, you only have to override lifecycle methods you intend on using.

## Prioritizing Lifecycles

If you want to call your lifecycle classes in a particular order, you may set the priority by annotating your Lifecycle
class with `@LifecyclePriority(PluginLifecycle.Priority.[PRIORITY])`. Priorities are `LOWEST`, `LOW`, `NORMAL`, `HIGH`,
and `HIGHEST`. All lifecycle methods (`onLoad`, `onEnable`, and `onDisable`) are gathered and sorted at runtime and
called in order of the highest priority first. Lifecycles with the same priority are not guaranteed to run in any
particular order. All lifecycles are the default `NORMAL` priority unless specified, and will therefore be run in
random order.

```java

@LifecyclePriority(PluginLifecycle.Priority.HIGHEST)
public final class MyLifecycle implements PluginLifecycle {
    @Override
    public void onEnable() {
        // ...
    }
}
```

ByteLib makes liberal use of the Lifecycles API. It even uses the Lifecycles API internally when registering Commands
via the Commands API. Lifecycles are a powerful way to separate your plugin's code at build time.

[You can see an example of the Lifecycle API being used here](https://github.com/TerrorByteTW/ByteLib/blob/master/src/main/java/org/reprogle/bytelib/commands/CommandsLifecycle.java).