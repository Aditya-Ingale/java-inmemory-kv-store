package com.adi.kvstore.impl;

import com.adi.kvstore.api.KeyValueStore;
import com.adi.kvstore.core.Entry;
import com.adi.kvstore.core.StorageEngine;
import com.adi.kvstore.expiration.ExpirationPolicy;
import com.adi.kvstore.time.Clock;

/*
* Single-threaded key-value store implementation
*/

public class SimpleKVStore implements KeyValueStore {

    private final StorageEngine storageEngine;
    private final ExpirationPolicy expirationPolicy;
    private final Clock clock;

    public SimpleKVStore(StorageEngine storageEngine, ExpirationPolicy expirationPolicy, Clock clock){
        this.storageEngine = storageEngine;
        this.expirationPolicy = expirationPolicy;
        this.clock = clock;
    }

    @Override
    public void put(String key, String value){
        Entry entry = new Entry(key, value , -1);
        storageEngine.put(key, entry);
    }

    @Override
    public void put(String key, String value, long ttlMillies){
        long expiryTime;

        if(ttlMillies <= 0){
            // Expire immediately
            expiryTime = clock.now();
        } else{
            expiryTime = clock.now() + ttlMillies;
        }

        Entry entry = new Entry(key, value, expiryTime);
        storageEngine.put(key, entry);
    }

    @Override
    public String get(String key){
        Entry entry = storageEngine.get(key);

        if(entry == null){
            return null;
        }

        long now = clock.now();
        if(expirationPolicy.isExpired(entry, now)){
            storageEngine.remove(key); // lazy cleanup
            return null;
        }
        
        return entry.getValue();
    }
}