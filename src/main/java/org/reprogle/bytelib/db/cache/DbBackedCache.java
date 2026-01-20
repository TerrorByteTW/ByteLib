package org.reprogle.bytelib.db.cache;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class DbBackedCache<K, V> implements AutoCloseable {
    private final DbCachePolicy policy;
    private final CacheLoader<K, V> loader;
    private final CacheWriter<K, V> writer;
    private final CacheDeleter<K> deleter;

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final Executor refreshExecutor;

    // basic stats (useful for debugging)
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong timeouts = new AtomicLong();
    private final AtomicLong refreshes = new AtomicLong();

    public DbBackedCache(
            DbCachePolicy policy,
            CacheLoader<K, V> loader,
            CacheWriter<K, V> writer,
            CacheDeleter<K> deleter,
            Executor refreshExecutor
    ) {
        this.policy = Objects.requireNonNull(policy);
        this.loader = Objects.requireNonNull(loader);
        this.writer = Objects.requireNonNull(writer);
        this.deleter = Objects.requireNonNull(deleter);
        this.refreshExecutor = Objects.requireNonNull(refreshExecutor);
    }

    /**
     * Transparent read: returns cached value if present.
     * If missing, blocks up to policy.blockingTimeout() attempting to load.
     * If it can’t, returns defaultValue and refreshes async.
     */
    public V get(K key, V defaultValue) {
        Optional<V> opt = getOptional(key);
        return opt.orElse(defaultValue);
    }

    public Optional<V> getOptional(K key) {
        Objects.requireNonNull(key);

        Entry<V> entry = map.get(key);
        long now = System.nanoTime();

        if (entry != null) {
            if (!entry.isExpired(now, policy.ttl())) {
                hits.incrementAndGet();
                maybeRefresh(key, entry, now);
                return Optional.ofNullable(entry.value);
            }

            // expired
            if (policy.serveStaleWhileRefreshing()) {
                // serve stale and refresh
                maybeRefresh(key, entry, now, true);
                hits.incrementAndGet();
                return Optional.ofNullable(entry.value);
            }

            // don’t serve stale: treat as miss
        }

        misses.incrementAndGet();
        return loadOnMiss(key, entry, now);
    }

    /**
     * Write-through: updates cache immediately, then writes to DB (blocking off-main-thread in your DB executor).
     * If you call this on the main thread, you should wrap with your own policy or do putAsync.
     */
    public void put(K key, V value) {
        Objects.requireNonNull(key);
        long now = System.nanoTime();

        // update cache first (write-through behavior)
        map.put(key, Entry.fresh(value, now));

        // persist
        try {
            writer.write(key, value);
        } catch (Exception e) {
            // cache already updated; decide if you want to revert or log higher up
            throw wrap(e);
        }

        evictIfNeeded();
    }

    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value), refreshExecutor);
    }

    public void invalidate(K key) {
        if (key == null) return;
        map.remove(key);
    }

    public void delete(K key) {
        Objects.requireNonNull(key);

        // remove from cache first
        map.remove(key);

        try {
            deleter.delete(key);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public CompletableFuture<Void> deleteAsync(K key) {
        return CompletableFuture.runAsync(() -> delete(key), refreshExecutor);
    }

    public long hitCount() {
        return hits.get();
    }

    public long missCount() {
        return misses.get();
    }

    public long timeoutCount() {
        return timeouts.get();
    }

    public long refreshCount() {
        return refreshes.get();
    }

    @Override
    public void close() {
        // nothing mandatory; caller owns executors typically
        map.clear();
    }

    // ---------------- internals ----------------

    private Optional<V> loadOnMiss(K key, Entry<V> existingEntry, long now) {
        // coalesce loads: if an entry exists and is currently loading, wait briefly
        Entry<V> entry = map.compute(key, (k, old) -> {
            if (old == null) return Entry.loading(null, now);
            if (old.loadingFuture != null && !old.loadingFuture.isDone()) return old; // already loading
            // if old exists but expired: either keep stale while loading or not
            return Entry.loading(oldValueIfAny(old), now);
        });

        // If we’re not the one who initiated loading, we still may wait for the existing future.
        CompletableFuture<Optional<V>> fut = ensureLoadingFuture(key, entry);

        // Block up to blockingTimeout (blocking-first)
        Duration timeout = policy.blockingTimeout();
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            // no blocking allowed -> return default empty and refresh async
            return Optional.ofNullable(entry.value);
        }

        try {
            Optional<V> loaded = fut.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            // Update cache with loaded result (fresh or absent)
            applyLoaded(key, loaded, now);

            return loaded;
        } catch (TimeoutException te) {
            timeouts.incrementAndGet();
            // keep whatever we have (maybe null), but ensure background refresh continues
            return Optional.ofNullable(entry.value);
        } catch (Exception e) {
            // loader failed: keep whatever we have, or remove entry if none
            if (existingEntry == null && entry.value == null) map.remove(key);
            throw wrap(unwrap(e));
        }
    }

    private CompletableFuture<Optional<V>> ensureLoadingFuture(K key, Entry<V> entry) {
        CompletableFuture<Optional<V>> existing = entry.loadingFuture;
        if (existing != null) return existing;

        // install a future atomically by recomputing
        Entry<V> updated = map.compute(key, (k, old) -> {
            if (old == null) return Entry.loading(null, System.nanoTime());
            if (old.loadingFuture != null) return old;
            CompletableFuture<Optional<V>> fut = CompletableFuture.supplyAsync(() -> {
                try {
                    return loader.load(key);
                } catch (Exception e) {
                    throw wrap(e);
                }
            }, refreshExecutor);
            return old.withLoadingFuture(fut);
        });

        return updated.loadingFuture;
    }

    private void maybeRefresh(K key, Entry<V> entry, long now) {
        maybeRefresh(key, entry, now, false);
    }

    private void maybeRefresh(K key, Entry<V> entry, long now, boolean forced) {
        Duration refreshAfter = policy.refreshAfter();
        if (!forced) {
            if (refreshAfter == null || refreshAfter.isZero() || refreshAfter.isNegative()) return;
            long ageNanos = now - entry.writtenAtNanos;
            if (ageNanos < refreshAfter.toNanos()) return;
        }

        // already loading? skip
        if (entry.loadingFuture != null && !entry.loadingFuture.isDone()) return;

        refreshes.incrementAndGet();
        CompletableFuture<Optional<V>> fut = CompletableFuture.supplyAsync(() -> {
            try {
                return loader.load(key);
            } catch (Exception e) {
                throw wrap(e);
            }
        }, refreshExecutor);

        map.computeIfPresent(key, (k, old) -> old.withLoadingFuture(fut));

        fut.whenComplete((opt, err) -> {
            if (err != null) return; // keep old value
            applyLoaded(key, opt, System.nanoTime());
        });
    }

    private void applyLoaded(K key, Optional<V> loaded, long now) {
        if (loaded.isEmpty()) {
            // if “not found”, remove entry entirely
            map.remove(key);
        } else {
            map.put(key, Entry.fresh(loaded.get(), now));
            evictIfNeeded();
        }
    }

    private void evictIfNeeded() {
        int maxSize = policy.maxSize();
        if (maxSize <= 0) return;
        int size = map.size();
        if (size <= maxSize) return;

        // cheap eviction: remove random-ish keys until under limit
        int toRemove = size - maxSize;
        Iterator<K> it = map.keySet().iterator();
        while (toRemove > 0 && it.hasNext()) {
            it.next();
            it.remove();
            toRemove--;
        }
    }

    private static <V> V oldValueIfAny(Entry<V> old) {
        return old == null ? null : old.value;
    }

    private static RuntimeException wrap(Throwable t) {
        return (t instanceof RuntimeException re) ? re : new RuntimeException(t);
    }

    private static Throwable unwrap(Throwable t) {
        if (t instanceof ExecutionException ee && ee.getCause() != null) return ee.getCause();
        return t;
    }

    // ---------------- entry ----------------

    private record Entry<V>(V value, long writtenAtNanos, CompletableFuture<Optional<V>> loadingFuture) {

        static <V> Entry<V> fresh(V value, long now) {
            return new Entry<>(value, now, null);
        }

        static <V> Entry<V> loading(V maybeStale, long now) {
            return new Entry<>(maybeStale, now, null);
        }

        Entry<V> withLoadingFuture(CompletableFuture<Optional<V>> future) {
            return new Entry<>(this.value, this.writtenAtNanos, future);
        }

        boolean isExpired(long now, Duration ttl) {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) return false; // never expires
            return (now - writtenAtNanos) >= ttl.toNanos();
        }
    }
}