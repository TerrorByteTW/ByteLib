package org.reprogle.bytelib.db.cache;

@FunctionalInterface
public interface CacheWriter<K, V> {
    void write(K key, V value) throws Exception;

    static <K, V> CacheWriter<K, V> noop() {
        return (k, v) -> {
        };
    }
}
