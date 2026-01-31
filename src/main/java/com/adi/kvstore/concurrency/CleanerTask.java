package com.adi.kvstore.concurrency;

import com.adi.kvstore.core.Entry;
import com.adi.kvstore.expiration.ExpirationPolicy;
import com.adi.kvstore.time.Clock;

import java.util.Map;

/*
* Background task that removes expired entries from the store.
* This task is best-effort and non-blocking.
*/

public class CleanerTask implements Runnable {

    private final ConcurrentStorageEngine storageEngine;
    private final ExpirationPolicy expirationPolicy;
    private final Clock clock;

    public CleanerTask(ConcurrentStorageEngine storageEngine, ExpirationPolicy expirationPolicy, Clock clock){
        this.storageEngine = storageEngine;
        this.expirationPolicy = expirationPolicy;
        this.clock = clock;
    }

    @Override
    public void run(){
        long now = clock.now();

        for(Map.Entry<String, Entry> mapEntry : storageEngine.entrySet()){
            Entry entry = mapEntry.getValue();

            if (expirationPolicy.isExpired(entry, now)) {
                //Safe conditional removal
                storageEngine.remove(mapEntry.getKey(), entry);
            }
        }
    }
}