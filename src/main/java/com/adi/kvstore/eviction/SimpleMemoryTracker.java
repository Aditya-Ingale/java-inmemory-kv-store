package com.adi.kvstore.eviction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple entry-count based memory tracker.
 * 
 * In v3, "memory" is defined as number of entries stored.
 * This tracker is thread-safe and intentionally simple.
 */
public class SimpleMemoryTracker implements MemoryTracker {

    private final int maxEntries;
    private final AtomicInteger currentEntries = new AtomicInteger(0);

    public SimpleMemoryTracker(int maxEntries){
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
    }

    @Override
    public void increment(){
        currentEntries.incrementAndGet();
    }

    @Override
    public void decrement() {
        currentEntries.decrementAndGet();
    }

    @Override
    public boolean isLimitExceeded(){
        return currentEntries.get() > maxEntries;
    }

    /**
     * Exposed for observability/debugging only.
     */
    public int getCurrentEntries(){
        return currentEntries.get();
    }
}