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

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class providing common permission and sender type checks for
 * commands.
 * 
 * <p>
 * These checks can be used with the {@code requires()} method on command nodes
 * to
 * restrict command access based on permissions, sender type, or custom
 * predicates.
 */
public final class PermissionChecks {
    /**
     * Utility class, not instantiable.
     */
    private PermissionChecks() {
    }

    /**
     * Creates a predicate that returns true if the sender has the specified
     * permission.
     * 
     * @param node the permission node to check
     * @return a predicate for permission checking
     */
    public static Predicate<CommandSourceStack> permission(String node) {
        Objects.requireNonNull(node, "node");
        return stack -> stack.getSender().hasPermission(node);
    }

    /**
     * Creates a predicate that returns true if the sender is an operator.
     *
     * @return a predicate that checks if sender is an operator
     */
    public static Predicate<CommandSourceStack> isOp() { return stack -> stack.getSender().isOp(); }

    /**
     * Creates a predicate that returns true if the sender is a player.
     * 
     * @return a predicate that checks if sender is a player
     */
    public static Predicate<CommandSourceStack> playerOnly() {
        return stack -> stack.getSender() instanceof Player;
    }

    /**
     * Creates a predicate that returns true if the sender is not a player (e.g.,
     * console).
     * 
     * @return a predicate that checks if sender is not a player
     */
    public static Predicate<CommandSourceStack> consoleOnly() {
        return stack -> !(stack.getSender() instanceof Player);
    }

    /**
     * Creates a predicate that returns true if the sender is an instance of the
     * specified type.
     * 
     * @param type the CommandSender subclass to check for
     * @return a predicate for type checking
     */
    public static Predicate<CommandSourceStack> senderType(Class<? extends CommandSender> type) {
        Objects.requireNonNull(type, "type");
        return stack -> type.isInstance(stack.getSender());
    }

    /**
     * Creates a predicate that returns true if all provided checks pass.
     * 
     * <p>
     * Null checks are treated as always passing. Short-circuits on the first
     * failing check.
     * 
     * @param checks the predicates to combine
     * @return a combined predicate that requires all checks to pass
     */
    @SafeVarargs
    public static Predicate<CommandSourceStack> allOf(Predicate<CommandSourceStack>... checks) {
        return stack -> {
            for (Predicate<CommandSourceStack> check : checks) {
                if (check != null && !check.test(stack))
                    return false;
            }
            return true;
        };
    }

    /**
     * Creates a predicate that returns true if any of the provided checks pass.
     * 
     * <p>
     * Null checks are treated as always failing. Short-circuits on the first
     * passing check.
     * 
     * @param checks the predicates to combine
     * @return a combined predicate that requires at least one check to pass
     */
    @SafeVarargs
    public static Predicate<CommandSourceStack> anyOf(Predicate<CommandSourceStack>... checks) {
        return stack -> {
            for (Predicate<CommandSourceStack> check : checks) {
                if (check != null && check.test(stack))
                    return true;
            }
            return false;
        };
    }

    /**
     * Creates a predicate that returns the opposite of the provided check.
     * 
     * @param check the predicate to negate
     * @return a predicate that inverts the result of the original
     */
    public static Predicate<CommandSourceStack> not(Predicate<CommandSourceStack> check) {
        Objects.requireNonNull(check, "check");
        return stack -> !check.test(stack);
    }
}