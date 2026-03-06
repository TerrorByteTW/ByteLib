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

package org.reprogle.bytelib.commands.dsl;

import org.reprogle.bytelib.commands.CommandRegistration;

import com.google.inject.Inject;
import io.papermc.paper.command.brigadier.Commands;

/**
 * CommandRegistration adapter for registering DSL-built command trees.
 * 
 * <p>
 * Bridges the gap between the command DSL and the registration infrastructure
 * by implementing
 * CommandRegistration. Allows DSL-built LiteralNode trees to be registered via
 * the standard
 * lifecycle and Guice module system.
 * 
 * <p>
 * <strong>Note:</strong> Paper Brigadier uses the LiteralCommandNode as the
 * command identity;
 * no separate label argument is passed to Commands#register(...).
 */
public final class DslCommandRegistration implements CommandRegistration {
    private final LiteralNode root;

    /**
     * Creates a DslCommandRegistration with the root command node.
     * 
     * @param root the root LiteralNode of the command tree to register
     */
    @Inject
    public DslCommandRegistration(LiteralNode root) {
        this.root = root;
    }

    /**
     * Registers the DSL-built command tree with Paper Brigadier.
     * 
     * @param commands the Paper Brigadier Commands registrar
     */
    @Override
    public void register(Commands commands) {
        commands.register(root.build().build());
    }
}