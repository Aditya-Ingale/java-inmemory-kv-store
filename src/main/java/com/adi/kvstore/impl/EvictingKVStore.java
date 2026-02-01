package com.adi.kvstore.impl;

import com.adi.kvstore.api.KeyValueStore;
import com.adi.kvstore.concurrency.ConcurrentStorageEngine;
import com.adi.kvstore.core.Entry;
import com.adi.kvstore.eviction.EvictionPolicy;
import com.adi.kvstore.eviction.MemoryTracker;
import com.adi.kvstore.expiration.ExpirationPolicy;
import com.adi.kvstore.time.Clock;

/**
 * v3 KeyValueStore implementation that adds memory-bounded eviction
 * on top of concurrency-safe storage and TTL handling.
 * 
 * This class is a pure composition layer:
 * - It does NOT implement eviction policy logic
 * - It does NOT manage background threads
 * - It doed NOT store data directly
 */
public class EvictingKVStore implements KeyValueStore {

    private final ConcurrentStorageEngine storageEngine;
    private final ExpirationPolicy expirationPolicy;
    private final EvictionPolicy evictionPolicy;
    private final MemoryTracker memoryTracker;
    private final Clock clock;

    public EvictingKVStore(ConcurrentStorageEngine storageEngine, ExpirationPolicy expirationPolicy, EvictionPolicy evictionPolicy, MemoryTracker memoryTracker, Clock clock){
        this.storageEngine = storageEngine;
        this.expirationPolicy = expirationPolicy;
        this.evictionPolicy = evictionPolicy;
        this.memoryTracker = memoryTracker;
        this.clock = clock;
    }

    @Override
    public void put(String key, String value){
        put(key, value, -1);
    }

    @Override
    public void put(String key, String value, long ttlMillis) {
        long expiryTime = (ttlMillis <= 0)
                ? clock.now()
                : clock.now() + ttlMillis;

        put(key, value, expiryTime);
    }

    private void putInternal(String key, String value, long expiryTime) {
        Entry newEntry = new Entry(key, value, expiryTime);

        Entry existing = storageEngine.get(key);
        storageEngine.put(key, newEntry);

        // Update memory tracking only on new keys
        if (existing == null) {
            memoryTracker.increment();
        }

        // Mark as recently used
        evictionPolicy.onAccess(key);

        // Enforce memory limits
        evictIfNeeded();
    }

    @Override
    public String get(String key) {
        Entry entry = storageEngine.get(key);
        if (entry == null) {
            return null;
        }

        long now = clock.now();
        if (expirationPolicy.isExpired(entry, now)) {
            // Expiration always wins over eviciton
            boolean removed = storageEngine.remove(key, entry);
            if (removed) {
                memoryTracker.decrement();
            }
            return null;
        }

        // Update LRU metadata on successfull accesss
        evictionPolicy.onAccess(key);
        return entry.getValue();
    }

    private void evictIfNeeded(){
        while (memoryTracker.isLimitExceeded()){
            String candidateKey = evictionPolicy.selectEvictionCandidate();
            if (candidateKey == null) {
                return;
            }

            Entry candidate = storageEngine.get(candidateKey);
            if (candidate == null) {
                continue;
            }

            // Do not evict expired entries here; expiration handles them
            boolean removed = storageEngine.remove(candidateKey, candidate);
            if(removed) {
                memoryTracker.decrement();
            }
        }
    }
}