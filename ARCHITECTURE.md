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

## 5. v3 Architecture – Eviction & Memory Limits

v3 introduces **bounded memory** through eviction while preserving all v1 and v2 guarantees.

The central problem addressed in v3 is:

> *How do we enforce memory limits in an in-memory system without breaking correctness, TTL semantics, or concurrency safety?*

---

### 5.1 v3 Design Goals

v3 is designed with the following explicit goals:

- Enforce a **fixed memory limit**
- Evict entries using **Least Recently Used (LRU)** strategy
- Preserve all TTL and concurrency invariants from v1 and v2
- Avoid rewriting or mutating existing components
- Prefer correctness and clarity over perfect eviction accuracy

---

### 5.2 v3 Flow

```text
CLI → EvictingKVStore
        ├── ConcurrentStorageEngine
        ├── ExpirationPolicy
        ├── EvictionPolicy (LRU)
        ├── MemoryTracker
        └── Clock
```

Eviction is added as a **composition layer**, not a modification.

---

### 5.3 EvictingKVStore (Composition Layer)

Responsibilities:
- Orchestrates storage, expiration, eviction, and memory tracking
- Triggers eviction synchronously on `PUT`
- Ensures expiration always has priority

Non-responsibilities:
- Does not implement eviction logic
- Does not track LRU ordering directly
- Does not manage background threads

---

### 5.4 Eviction Policy Abstraction

Eviction logic is isolated behind an `EvictionPolicy` interface.

Key rule:

> Policy decides *what* to evict; the store decides *when* to evict.

This enables easy replacement (LRU → LFU → FIFO).

---

### 5.5 LRU Eviction Strategy

Characteristics:
- Tracks access order using a concurrent data structure
- Updates recency on every successful `GET` and `PUT`
- Selects eviction candidates from least-recently-used end

Intentional trade-offs:
- Ordering is approximate under concurrency
- Duplicate metadata entries may exist
- Storage remains the source of truth

This mirrors real-world systems like Redis.

---

### 5.6 Memory Tracking

Memory is defined as:

> **Number of entries**, not bytes

Rationale:
- JVM object sizes are difficult to measure accurately
- Entry-count tracking keeps reasoning simple
- Limits are enforced *eventually*, not instantaneously

---

### 5.7 Eviction vs Expiration

Priority rules:

1. **Expiration always wins**
2. Expired entries are removed lazily or via cleanup
3. Eviction removes only valid entries under pressure

This prevents eviction of valid data while expired data exists.

---

### 5.8 Concurrency Safety in v3

All removals use **conditional removal**:

```
remove(key, expectedEntry)
```


This guarantees:
- Newer entries are never deleted by stale threads
- Eviction and expiration races are safe
- Metadata corruption does not break correctness

---

### 5.9 v3 Invariants

- Expired entries are never returned
- Memory usage is bounded
- Eviction never deletes newer entries accidentally
- LRU metadata corruption does not affect correctness
- All v2 concurrency guarantees are preserved

---

### 5.10 v3 Trade-offs

Accepted:
- Approximate LRU ordering
- Temporary memory overshoot
- Metadata redundancy

Rejected:
- Global locks
- Perfect eviction ordering
- Byte-accurate memory tracking

---

## 6. Key Takeaways

- Correctness precedes optimization
- Time, data, policy, and eviction are cleanly separated
- Systems evolve safely through composition
- Approximation is acceptable when correctness is preserved

This architecture reflects how real-world in-memory systems are designed.

---

**End of Architecture Document**
