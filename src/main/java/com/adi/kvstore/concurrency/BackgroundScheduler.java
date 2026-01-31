package com.adi.kvstore.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
* Manages bcakground task scheduling for the key-value store.
*/

public class BackgroundScheduler {

    private final ScheduledExecutorService scheduler;
    private final long intervalMillis;

    public BackgroundScheduler(long intervalMillis) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.intervalMillis = intervalMillis;
    }

    /*
    * Starts scheduling the given task at a fixed interval.
    */
    public void start(Runnable task){
        scheduler.scheduleAtFixedRate(task, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    /*
    * Stops all scheduled tasks gracefully.
    */
    public void stop(){
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}