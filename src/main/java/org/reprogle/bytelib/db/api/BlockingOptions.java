package org.reprogle.bytelib.db.api;

import java.time.Duration;

public record BlockingOptions(Duration timeout, MainThreadPolicy mainThreadPolicy, TimeoutBehavior timeoutBehavior,
                              Duration slowQueryWarnThreshold) {
    public static BlockingOptions mainThreadDefault() {
        return new BlockingOptions(
                Duration.ofMillis(15),        // cap impact on TPS
                MainThreadPolicy.WARN,        // you’ll see hotspots immediately
                TimeoutBehavior.THROW,        // forcing you to handle explicitly
                Duration.ofMillis(5)          // log if it took >5ms on main thread
        );
    }

    public BlockingOptions withTimeout(Duration timeout) {
        return new BlockingOptions(timeout, mainThreadPolicy, timeoutBehavior, slowQueryWarnThreshold);
    }
}
