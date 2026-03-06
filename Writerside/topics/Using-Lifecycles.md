# Using Lifecycles

ByteLib automatically discovers classes implementing the `PluginLifecycle` interface. This interface allows overriding
the standard Paper lifecycle events. Using `PluginLifecycle` enables you to use Dependency Injection within your lifecycle classes, meaning
you can inject dependencies for use in your `onEnable`, `onDisable`, and even `onLoad` methods!

As with Paper's lifecycle system, you only have to override lifecycle methods you intend on using.


<code-block lang="java">
public final class MyLifecycle implements PluginLifecycle {
    private final BytePluginConfig config;
    
    // You don't need to inject anything into your constructor, or even have one at all.
    // This example just demonstrates that you can.
    @Inject
    public MyLifecycle([[[BytePluginConfig|Configuration-Translation.md]]] config) {
        this.config = config;
    }
    
    @Override
    public void onEnable() {
        config.config().getString(&quot;some-string&quot;);
        // Do more work...
    }
}
</code-block>

ByteLib makes liberal use of the Lifecycles API. It even uses the Lifecycles API internally when registering Commands
via the [](Commands-API.md). Lifecycles are a powerful way to separate your plugin's code at build time.

[You can see an example of the Lifecycle API being used here](https://github.com/TerrorByteTW/ByteLib/blob/master/src/main/java/org/reprogle/bytelib/commands/CommandsLifecycle.java).