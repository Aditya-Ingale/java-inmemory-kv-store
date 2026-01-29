package com.adi.kvstore.time;

/*
* Clock abstraction to provide current time.
* Allows decoupling time-dependent logic from system time.
*/

public interface Clock {

    /*
    * @return current time in milliseconds since epoch
    */
    long now();
}