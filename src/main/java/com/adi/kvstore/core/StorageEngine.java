package com.adi.kvstore.core;

/*
* Storage engine abstraction for key-value entries.
* This layer is TTL-agnostic and policy-free
*/

public interface StorageEngine {

    void put(String key, Entry entry);

    Entry get(String key);

    void remove(String key);

    boolean containsKey(String key);
}