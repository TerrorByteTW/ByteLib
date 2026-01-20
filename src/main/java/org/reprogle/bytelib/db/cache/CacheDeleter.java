package org.reprogle.bytelib.db.cache;

@FunctionalInterface
public interface CacheDeleter<K> {
    void delete(K key) throws Exception;

    static <K> CacheDeleter<K> noop() {
        return k -> {
        };
    }
}
