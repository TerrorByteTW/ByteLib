/*
 * Copyright (c) 2026 Nate Reprogle and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package org.reprogle.bytelib.boot.wiring;

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
