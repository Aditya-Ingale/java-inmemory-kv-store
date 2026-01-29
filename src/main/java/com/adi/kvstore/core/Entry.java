package com.adi.kvstore.core;

/*
*  Represents a single key-value entry in the store.
* Immutable by design.
*/

public final class Entry{

    private final String key;
    private final String value;
    private final long expiryTime; // -1 mean no expiration

    public Entry(String key, String value, long expiryTime){
        if(key == null || key.isBlank()){
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        if(value == null){
            throw new IllegalArgumentException("Value must not be null");
        }

        this.key = key;
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getKey(){
        return key;
    }

    public String getValue(){
        return value;
    }

    public long getExpiryTime(){
        return expiryTime;
    }

    /*
    * @return true if this entry has an expiration time set
    */

    public boolean hasExpiry(){
        return expiryTime >= 0;
    }
}