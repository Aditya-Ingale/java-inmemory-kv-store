package com.adi.kvstore.time;

/*
* System Clock implementation using JVM system time.
*/

public class SystemClock implements Clock{
    @Override
    public long now(){
        return System.currentTimeMillis();
    }
}