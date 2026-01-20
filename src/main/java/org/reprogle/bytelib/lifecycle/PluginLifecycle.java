package org.reprogle.bytelib.lifecycle;

public interface PluginLifecycle {
    default void onLoad() {}
    default void onEnable() {}
    default void onDisable() {}
}
