package org.reprogle.bytelib.boot.lifecycle;

public interface PluginLifecycle {
    default void onLoad() {}
    default void onEnable() {}
    default void onDisable() {}

    enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST
    }
}
