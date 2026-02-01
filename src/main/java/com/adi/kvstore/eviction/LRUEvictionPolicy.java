package com.adi.kvstore.eviction;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Approximation LRU eviction policy.
 * 
 * This implementation is concurrency-safe and intentionally simple:
 * - Tracks access order using a ConcurrentLinkedDeque of keys.
 * - Most-recently-used keys are kept at the front
 * - Least-recently-used keys are kept at the front
 *  
 * Trade-off:
 * - Ordering is approximate under concurrency
 * - Duplicate keys may exist in the deque
 * - Storage is the source of the truth, not this structure
 */
public class LRUEvictionPolicy implements EvictionPolicy {

    private final ConcurrentLinkedDeque<String> accessOrder = new ConcurrentLinkedDeque<>();

    @Override
    public void onAccess(String key) {
        if (key == null) {
            return;
        }
        // Best-effort LRU update: remove then add to front
        accessOrder.remove(key);
        accessOrder.addFirst(key);
    }

    @Override 
    public String selectEvictionCandidate(){
        // Least-recently-used key (best-effort)
        return accessOrder.pollLast();
    }
}