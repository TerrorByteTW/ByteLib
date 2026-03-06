# Create a Command

The Commands API uses ByteLib's own `PluginLifecycle` API surface internally. It creates a lifecycle class called
`CommandsLifecycle` which registers each command during `onEnable()`.

For the Commands API to find and register your Command, you must implement the `CommandRegistration` interface from
ByteLib. Implementing that class requires you to override the `register(Commands commands)` method, which serves as your
Command's registration entry point.

Use the `CommandDsl` class to create a command. `CommandDsl` is the main class used to create Commands API
commands.

```java
public final class MyCommandRegistration implements CommandRegistration {
    @Override
    public void register(Commands commands) {
        LiteralNode root = CommandDsl.literal("myCommand");
        commands.register(root.toCommandNode());
    }
}
```

`toCommandNode()` is used to convert the `LiteralNode` that ByteLib provides to a
`LiteralCommandNode<CommandSourceStack>` required by Paper.

## Using Dependency Injection

You can use Dependency Injection in `CommandRegistration` classes. To inject dependencies into your command, you may
create a constructor and annotate it with `@Inject` like normal

```java
// ...
public final class MyCommandRegistration implements CommandRegistration {
    private final MyService myService;
    private final CommandFactory factory;

    @Inject
    public MyCommandRegistration(MyService myService, CommandFactory factory) {
        this.myService = myService;
        this.factory = factory;
    }

    @Override
    public void register(Commands commands) {
        LiteralNode root = CommandDsl.literal("myCommand");
        commands.register(root.toCommandNode());
    }
}
```

`CommandFactory` is a special dependency provided by the Commands API that can be used to create complex command
executors. You will read about it a later topic.