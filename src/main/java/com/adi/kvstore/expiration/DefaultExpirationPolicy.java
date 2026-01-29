package com.adi.kvstore.expiration;

import com.adi.kvstore.core.Entry;

/*
* Default TTL-based expiration policy.
*/

public class DefaultExpirationPolicy implements ExpirationPolicy {

    @Override
    public boolean isExpired(Entry entry, long currentTime){
        if (!entry.hasExpiry()) {
            return false;
        }
        return currentTime >= entry.getExpiryTime();
    }
}