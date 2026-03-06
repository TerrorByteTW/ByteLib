# Lifecycle Prioritization

If you want to call your lifecycle classes in a particular order, you may set the priority by annotating your Lifecycle
class with `@LifecyclePriority(PluginLifecycle.Priority)`. Priorities are `LOWEST`, `LOW`, `NORMAL`, `HIGH`,
and `HIGHEST`. 

All lifecycle methods (`onLoad`, `onEnable`, and `onDisable`) are gathered and sorted at runtime and
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