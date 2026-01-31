package com.adi.kvstore.eviction;

/**
 * EvictionPolicy decides which key should be evicted
 * when the memory limit is exceeded
 * 
 * This interface is intentionally policy-only:
 * - It does NOT remove entries
 * - It does NOT know about TTL
 * - It does NOT know about concurrency primitives
 */
public interface EvictionPolicy {

    /**
     * Records access to a key (GET or PUT).
     * Used to update eviction metadata.
     */
    void onAccess(String key);

    /**
     * Selects a candidate key for eviction
     * 
     * @return key to evict, or null if none available
     */
    String selectEvictionCandidate();
}