package com.adi.kvstore.concurrency;

import com.adi.kvstore.core.Entry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
* Thread-safe storage engine using ConcurrentHashMap.
* This class is policy-free and TTL-agnostic.
*/

public class ConcurrentStorageEngine {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /*
    * Stores or replaces an entry for the given key.
    */
    public void put(String key, Entry entry){
        store.put(key, entry);
    }

    /*
    * Retrieves the entry for the given key.
    */
    public Entry get(String key){
        return store.get(key);
    }

    /*
    * Removes the entry for the given key unconditionally.
    */
    public void remove(String key){
        store.remove(key);
    }

    /*
    * Removes the entry only if it matches the expected value.
    * This is critical for safe concurrent expiration.
    * 
    * @return true if the entry was removed, false otherwise
    */
    public boolean remove(String key, Entry expectedEntry){
        return store.remove(key, expectedEntry);
    }

    /*
    * Return a snapshot view of keys for iteration.
    */
    public Set<Map.Entry<String, Entry>> entrySet(){
        return store.entrySet();
    }
}