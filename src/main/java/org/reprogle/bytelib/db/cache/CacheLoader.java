package org.reprogle.bytelib.db.cache;

import java.util.Optional;

@FunctionalInterface
public interface CacheLoader<K, V> {
    Optional<V> load(K key) throws Exception;
}
