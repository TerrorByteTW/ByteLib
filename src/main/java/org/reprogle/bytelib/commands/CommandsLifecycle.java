package org.reprogle.bytelib.commands;

import com.google.inject.Inject;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

import java.util.Set;

public final class CommandsLifecycle implements PluginLifecycle {
    private final JavaPlugin plugin;
    private final Set<CommandRegistration> registrations;

    @Inject
    public CommandsLifecycle(JavaPlugin plugin, Set<CommandRegistration> registrations) {
        this.plugin = plugin;
        this.registrations = registrations;
    }

    @Override
    public void onEnable() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var commands = event.registrar();
            for (CommandRegistration reg : registrations) {
                reg.register(commands);
            }
        });
    }
}
