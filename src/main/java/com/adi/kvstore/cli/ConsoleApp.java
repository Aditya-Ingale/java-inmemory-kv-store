package com.adi.kvstore.cli;

import com.adi.kvstore.api.KeyValueStore;
import com.adi.kvstore.concurrency.ConcurrentStorageEngine;
import com.adi.kvstore.core.InMemoryStorageEngine;
import com.adi.kvstore.eviction.LRUEvictionPolicy;
import com.adi.kvstore.eviction.SimpleMemoryTracker;
import com.adi.kvstore.expiration.DefaultExpirationPolicy;
import com.adi.kvstore.impl.ConcurrentKVStore;
import com.adi.kvstore.impl.EvictingKVStore;
import com.adi.kvstore.impl.SimpleKVStore;
import com.adi.kvstore.time.SystemClock;

import java.util.Scanner;

/**
 * Console-based interface for the key-value store.
 */

public class ConsoleApp {

    public static void main(String[] args) {

        // v1: 
        // KeyValueStore store = new SimpleKVStore(new InMemoryStorageEngine(), new DefaultExpirationPolicy(), new SystemClock());

        // v2: 
        // ConcurrentKVStore store = new ConcurrentKVStore(new ConcurrentStorageEngine(), new DefaultExpirationPolicy(), new SystemClock());
        
        // v3: Evicitng Key-Value Store with LRU
        KeyValueStore store = new EvictingKVStore(new ConcurrentStorageEngine(), new DefaultExpirationPolicy(), new LRUEvictionPolicy(), new SimpleMemoryTracker(3), new SystemClock());

        Scanner scanner = new Scanner(System.in);
        System.out.println("In-Memory Key-Value Store started.");
        System.out.println("Available commands: PUT, GET, EXIT");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] tokens = input.split("\\s+");
            String command = tokens[0].toUpperCase();

            try {
                switch (command) {
                    case "PUT":
                        handlePut(tokens, store);
                        break;
                    
                    case "GET":
                        handleGet(tokens, store);
                        break;
                    
                    case "EXIT":
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Unknown command");;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e){
                System.out.println("Unexcepted error: " + e.getMessage());
            }
        }
    }

    private static void handlePut(String[] tokens, KeyValueStore store) {
        if (tokens.length < 3 || tokens.length > 4) {
            throw new IllegalArgumentException("Usage: PUT key value [TTL}");
        }

        String key =tokens[1];
        String value = tokens[2];

        if (tokens.length == 4) {
            long ttl;
            try {
                ttl = Long.parseLong(tokens[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("TTL must be a number (milliseconds)");
            }
            store.put(key, value, ttl);
        }else {
            store.put(key, value);
        }

        System.out.println("OK");
    }

    private static void handleGet(String[] tokens, KeyValueStore store){
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Usage: GET key");
        }

        String key = tokens[1];
        String value = store.get(key);

        if (value == null) {
            System.out.println("(nil)");
        } else {
            System.out.println(value);
        }
    }

}