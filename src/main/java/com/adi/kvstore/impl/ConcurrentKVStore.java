package com.adi.kvstore.impl;

import com.adi.kvstore.api.KeyValueStore;
import com.adi.kvstore.concurrency.BackgroundScheduler;
import com.adi.kvstore.concurrency.CleanerTask;
import com.adi.kvstore.concurrency.ConcurrentStorageEngine;
import com.adi.kvstore.core.Entry;
import com.adi.kvstore.expiration.ExpirationPolicy;
import com.adi.kvstore.time.Clock;

/*
* Thread-safe key-value store implementation with background expiration cleanup.
*/

public class ConcurrentKVStore implements KeyValueStore{

    private static final long CLEANUP_INTERVAL_MILLIS = 5000; // 5 seconds (v2 default)

    private final ConcurrentStorageEngine storageEngine;
    private final ExpirationPolicy expirationPolicy;
    private final Clock clock;
    private final BackgroundScheduler scheduler;

    public ConcurrentKVStore(ConcurrentStorageEngine storageEngine, ExpirationPolicy expirationPolicy, Clock clock){
        this.storageEngine = storageEngine;
        this.expirationPolicy = expirationPolicy;
        this.clock = clock;

        // Setup background expiration cleanup
        CleanerTask cleanerTask = new CleanerTask(storageEngine, expirationPolicy, clock);
        this.scheduler = new BackgroundScheduler(CLEANUP_INTERVAL_MILLIS);
        this.scheduler.start(cleanerTask);
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

    /*
    * Gracefully stop background cleanup.
    * Should be called during application shutdown.
    */
    public void shutdown(){
        scheduler.stop();
    }
}