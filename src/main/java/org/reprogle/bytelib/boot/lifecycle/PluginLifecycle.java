package org.reprogle.bytelib.boot.lifecycle;

public interface PluginLifecycle {
    default void onLoad() {}
    default void onEnable() {}
    default void onDisable() {}
}
