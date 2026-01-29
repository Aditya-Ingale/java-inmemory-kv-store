package com.adi.kvstore.expiration;

import com.adi.kvstore.core.Entry;

/*
* Policy to determine whether a given ectry is expired.
*/

public interface ExpirationPolicy {

    /*
    * Determine whether the entry is expired at the given time.
    * @param entry the entry to check
    * @param currentTime current time in milliseconds
    * @return true if expired, false otherwise
    */

    boolean isExpired(Entry entry, long currentTime);
}