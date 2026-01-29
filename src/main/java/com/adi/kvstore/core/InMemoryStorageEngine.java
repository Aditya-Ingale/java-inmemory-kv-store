package com.adi.kvstore.core;

import java.util.HashMap;
import java.util.Map;

/*
* In-memory implementation of StorageEngine using HashMap.
*/

public class InMemoryStorageEngine implements StorageEngine {

    private final Map<String, Entry> store = new HashMap<>();

    @Override
    public void put(String key, Entry entry){
        store.put(key, entry);
    }

    @Override
    public Entry get(String key){
        return store.get(key);
    }

    @Override
    public void remove(String key){
        store.remove(key);
    }

    @Override
    public boolean containsKey(String key){
        return store.containsKey(key);
    }
}