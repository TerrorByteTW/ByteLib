package org.reprogle.bytelib.db.api;

public enum MainThreadPolicy {
    ALLOW,          // no warning
    WARN,           // log stacktrace / slow query warning
    DISALLOW        // throw exception if on main thread
}
