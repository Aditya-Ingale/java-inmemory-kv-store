package com.adi.kvstore.api;

/*
* Public API for the key-value store.
*/

public interface KeyValueStore {

    /*
    * Store a ekey-value pair without exception.
    */
    void put(String key, String Value);

    /*
    * Store a key-value pair with a TTL in Milliseconds.
    */
    void put(String key, String value, long ttlMillies);

    /*
    * Retrieve the value for a key.
    *
    * @return value if present and not ecpired, otherwise null
    */
    String get(String key);
}