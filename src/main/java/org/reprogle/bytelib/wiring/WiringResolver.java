package org.reprogle.bytelib.wiring;

import java.util.Optional;
import java.util.ServiceLoader;

public final class WiringResolver {
    private WiringResolver() {
    }

    public static Optional<PluginWiring> resolve(ClassLoader classLoader, String mainClassName) {
        // Convention 1: <MainClassName>Wiring
        Optional<PluginWiring> wiring = tryInstantiate(classLoader, mainClassName + "Wiring");
        if (wiring.isPresent()) return wiring;

        // Convention 2: <MainClassName>$Wiring (nested)
        wiring = tryInstantiate(classLoader, mainClassName + "$Wiring");
        if (wiring.isPresent()) return wiring;

        // Optional fallback: ServiceLoader
        for (PluginWiring candidate : ServiceLoader.load(PluginWiring.class, classLoader)) {
            return Optional.of(candidate);
        }

        return Optional.empty();
    }

    private static Optional<PluginWiring> tryInstantiate(ClassLoader classLoader, String className) {
        try {
            Class<?> clazz = Class.forName(className, true, classLoader);
            if (!PluginWiring.class.isAssignableFrom(clazz)) return Optional.empty();
            return Optional.of((PluginWiring) clazz.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Found wiring class but couldn't instantiate: " + className, e);
        }
    }
}
