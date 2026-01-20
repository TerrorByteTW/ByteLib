package org.reprogle.bytelib.db.api;

public record Table(String name) {
    public static Table of(String name) {
        return new Table(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public <T> Column<T> col(String colName, SqlType<T> type) {
        return new Column<>(this, colName, type);
    }

    public record Column<T>(Table table, String name, SqlType<T> type) {
        public Param<T> param(T value) {
            return new Param<>(type, value);
        }

        @Override
        public String toString() {
            return table.name() + "." + name;
        }
    }
}
