package com.adi.kvstore.eviction;

/**
 * MemoryTracker is responsible for tracking memory usage
 * and determining whether eviction is required
 * 
 * In v3, memory is tracked as number of entries.
 */
public interface MemoryTracker {

    /**
     * Called when a new entry is added
     */
    void increment();

    /**
     * Called when an entry is removed.
     */
    void decrement();

    /**
     * @return true if memory limit is exceeded
     */
    boolean isLimitExceeded();
}