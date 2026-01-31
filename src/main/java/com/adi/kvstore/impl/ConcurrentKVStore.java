package com.adi.kvstore.impl;

import com.adi.kvstore.api.KeyValueStore;
import com.adi.kvstore.concurrency.ConcurrentStorageEngine;
import com.adi.kvstore.core.Entry;
import com.adi.kvstore.expiration.ExpirationPolicy;
import com.adi.kvstore.time.Clock;

/*
* Thread-safe key-value store implementation
* Supports concurrent access with correct TTL semantics.
*/

public class ConcurrentKVStore implements KeyValueStore{

    private final ConcurrentStorageEngine storageEngine;
    private final ExpirationPolicy expirationPolicy;
    private final Clock clock;

    public ConcurrentKVStore(ConcurrentStorageEngine storageEngine, ExpirationPolicy expirationPolicy, Clock clock){
        this.storageEngine = storageEngine;
        this.expirationPolicy = expirationPolicy;
        this.clock = clock;
    }

    @Override
    public void put(String key, String value){
        Entry entry = new Entry(key, value, -1);
        storageEngine.put(key, entry);  
    }

    @Override
    public void put(String key, String value, long ttlMillis){
        long expiryTime;

        if (ttlMillis <= 0) {
            expiryTime = clock.now(); // immediate expiration
        } else {
            expiryTime = clock.now() + ttlMillis;
        }

        Entry entry = new Entry(key, value, expiryTime);
        storageEngine.put(key, entry);
    }

    @Override
    public String get(String key){
        Entry entry = storageEngine.get(key);

        if (entry == null) {
            return null;
        }

        long now = clock.now();
        if (expirationPolicy.isExpired(entry, now)) {
            // Safe conditional removal to avoid deleting newer entry
            storageEngine.remove(key, entry);
            return null;
        }

        return entry.getValue();
    }
}