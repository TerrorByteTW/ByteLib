package org.reprogle.bytelib.db.api;

import org.jetbrains.annotations.NotNull;

public record Column<T>(Table table, String name, SqlType<T> type) {
    @Override
    public @NotNull String toString() {
        return table.name() + "." + name;
    }
}
