# Architecture – Java In-Memory Key–Value Store

This document describes the **internal architecture, design decisions, invariants, and evolution** of the Java In-Memory Key–Value Store.

> Motto: **Clarity. Correctness. Clear Abstraction.**

---

## 1. Architectural Philosophy

This project is **not** a Redis clone.

The goal is to understand:

* How in-memory systems behave internally
* How correctness is preserved under time and concurrency
* How systems evolve without rewrites

Key principles:

* Separate *data* from *policy*
* Make time an explicit dependency
* Prefer correctness over performance
* Evolve by composition, not mutation

---

## 2. Versioned System Evolution

The system is intentionally built in **explicit versions**.

| Version | Focus                                                 |
| ------- | ----------------------------------------------------- |
| v1      | Correct single-threaded behavior, TTL                 |
| v2      | Concurrency, thread safety, background cleanup        |
| v3      | Eviction (LRU), memory limits, performance trade-offs |

Each version preserves the previous one.

---

## 3. v1 Architecture – Core Correctness

### 3.1 v1 Scope

* In-memory key–value store
* `PUT` / `GET` operations
* TTL (Time-To-Live) support
* Lazy expiration
* Single-threaded execution

Explicit non-goals:

* Concurrency
* Eviction
* Persistence
* Networking

---

### 3.2 v1 High-Level Flow

```
CLI → KeyValueStore → StorageEngine
              ↓
        ExpirationPolicy
              ↓
            Clock
```

Each layer has a single responsibility.

---

### 3.3 Core v1 Components

#### Clock

* Abstracts system time
* Prevents hard dependency on `System.currentTimeMillis()`
* Makes TTL logic testable and deterministic

---

#### Entry

* Immutable data holder
* Fields: `key`, `value`, `expiryTime`
* No expiration logic inside the data model

---

#### ExpirationPolicy

* Answers one question: *Is this entry expired at time T?*
* Stateless and reusable
* No side effects

---

#### StorageEngine

* Policy-free storage abstraction
* Stores and retrieves entries
* Knows nothing about TTL or time

---

#### SimpleKVStore (v1)

* Orchestrates storage, expiration, and time
* Implements lazy expiration on `GET`
* Enforces v1 correctness invariants

---

### 3.4 v1 Invariants

* Expired data is never returned
* Storage does not enforce expiration
* Time is injected, not global
* Each class has a single reason to change

---

## 4. v2 Architecture – Concurrency & Cleanup

v2 extends v1 to support **safe concurrent access**.

The core insight:

> Concurrency must be added *around* correct logic, not *inside* it.

---

### 4.1 Concurrency Challenges

Without protection, the following races occur:

* `GET` vs `PUT`
* `GET` vs expiration cleanup
* Cleanup vs `PUT`

v2 addresses these without global locking.

---

### 4.2 v2 High-Level Flow

```
CLI → ConcurrentKVStore
        ├── ConcurrentStorageEngine
        ├── ExpirationPolicy
        ├── Clock
        └── BackgroundScheduler
              └── CleanerTask
```

---

### 4.3 v2 Core Components

#### ConcurrentStorageEngine

* Wraps `ConcurrentHashMap`
* Provides atomic operations
* Supports **compare-and-remove**

This method is critical:

```
remove(key, expectedEntry)
```

It prevents deletion of newer entries by stale threads.

---

#### ConcurrentKVStore

* Thread-safe implementation of `KeyValueStore`
* Uses the same TTL logic as v1
* Applies conditional removal on expiration
* Starts background cleanup automatically

v1 logic is preserved; only safety is added.

---

#### CleanerTask

* Background expiration cleanup task
* Iterates over storage entries
* Removes expired entries using compare-and-remove
* Best-effort and non-blocking

Cleanup is an optimization, not a correctness requirement.

---

#### BackgroundScheduler

* Manages lifecycle of background threads
* Uses `ScheduledExecutorService`
* Provides clean startup and shutdown

Thread management is centralized and explicit.

---

### 4.4 v2 Invariants

* Expired data is never returned
* Newer entries are never deleted by older threads
* Reads see consistent entries
* Cleanup must not block `GET` / `PUT`
* Failure of cleanup does not break correctness

---

### 4.5 v2 Trade-offs

Accepted:

* Expired entries may exist briefly in memory
* Cleanup timing is approximate
* Slight overhead from concurrent structures

Rejected:

* Global locks
* Blocking cleanup
* Aggressive synchronous expiration

---

## 5. v3 Preview – Eviction & Memory Limits

Planned v3 additions:

* LRU eviction policy
* Configurable memory limits
* Eviction + expiration interaction
* Performance vs correctness trade-offs

v3 will reuse:

* Concurrent storage
* Background scheduling
* Expiration policies

No v2 code will be rewritten.

---

## 6. Key Takeaways

* Correctness is established before optimization
* Time, data, and policy are strictly separated
* Concurrency is introduced via composition
* The system evolves without breaking abstractions

This architecture mirrors how real in-memory systems are designed.

---

**End of Architecture Document**
