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

package org.reprogle.bytelib.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.reprogle.bytelib.boot.lifecycle.InternalLifecycles;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

/**
 * Guice module that configures command system bindings.
 * This is an internal module to ByteLib, you should not instantiate
 * this module directly.
 * 
 * <p>
 * Sets up the following:
 * <ul>
 * <li>CommandFactory binding to GuiceCommandFactory for DI-based callback
 * instantiation</li>
 * <li>Multibinder for CommandRegistration implementations</li>
 * <li>CommandsLifecycle registration for plugin enable hook</li>
 * </ul>
 */
public final class CommandsModule extends AbstractModule {
    /**
     * Configures the Guice bindings for the command system.
     */
    @Override
    protected void configure() {
        // Existing extension point remains.
        Multibinder.newSetBinder(binder(), CommandRegistration.class);

        // New: default DI-backed command callback factory.
        bind(CommandFactory.class).to(GuiceCommandFactory.class);

        // Existing lifecycle hook remains.
        Multibinder.newSetBinder(binder(), PluginLifecycle.class, InternalLifecycles.class)
                .addBinding()
                .to(CommandsLifecycle.class);
    }
}