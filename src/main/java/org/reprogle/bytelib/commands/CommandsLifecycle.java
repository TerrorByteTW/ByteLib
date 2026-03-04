package org.reprogle.bytelib.commands;

import com.google.inject.Inject;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

import java.util.Set;

/**
 * Plugin lifecycle handler that registers commands during plugin enable.
 * 
 * <p>
 * This lifecycle handler hooks into Paper's lifecycle event system to register
 * all
 * CommandRegistration instances when the COMMANDS lifecycle event fires.
 */
public final class CommandsLifecycle implements PluginLifecycle {
    private final JavaPlugin plugin;
    private final Set<CommandRegistration> registrations;

    /**
     * Creates a CommandsLifecycle with injected dependencies.
     * 
     * @param plugin        the plugin instance
     * @param registrations the set of CommandRegistration implementations to
     *                      register
     */
    @Inject
    public CommandsLifecycle(JavaPlugin plugin, Set<CommandRegistration> registrations) {
        this.plugin = plugin;
        this.registrations = registrations;
    }

    /**
     * Registers all command registrations when the plugin enables.
     */
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
