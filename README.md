# Java In-Memory Key–Value Store (v1)

A Redis-inspired **in-memory key–value store** built in Java as a **system-level learning project**.

This project is **not** a Redis reimplementation. The goal is to understand how fast in-memory systems work internally instead of treating them as a black box.

> **Motto:** Clarity. Correctness. Clear Abstraction.

---

## 1. Project Goals

* Understand internal design of in-memory data stores
* Learn how TTL (Time-To-Live) expiration works
* Practice clean abstractions and layered architecture
* Build confidence in system-level Java design
* Avoid feature explosion by evolving the system version by version

---

## 2. Versioning Strategy

The project is developed incrementally in **three explicit versions**:

| Version | Focus                                               |
| ------- | --------------------------------------------------- |
| v1      | Core store, TTL correctness, clean abstractions     |
| v2      | Concurrency, thread safety, background cleanup      |
| v3      | LRU eviction, memory limits, performance trade-offs |

This README documents **v1 only**.

---

## 3. v1 Scope

### Supported Commands

```
PUT key value
PUT key value ttlMillis
GET key
EXIT
```

### Features

* Fully in-memory storage
* Lazy TTL expiration
* Single-threaded execution
* Console-based interaction

### Explicit Non-Goals (v1)

* No eviction policy
* No concurrency
* No persistence
* No networking
* No advanced Redis data types

---

## 4. High-Level Architecture (v1)

```
CLI → KeyValueStore → StorageEngine
              ↓
        ExpirationPolicy
              ↓
            Clock
```

### Key Design Principle

> **Data, policy, and time are deliberately separated.**

This keeps the system testable, extensible, and easy to reason about.

---

## 5. Package Structure (v1)

```
com.adi.kvstore
├── api          # Public store interface
├── cli          # Console interface
├── core         # Core data and storage
├── expiration   # TTL expiration logic
├── impl         # Store implementation
└── time         # Time abstraction
```

---

## 6. Core Components

### 6.1 Clock

**Package:** `time`

Abstracts system time to avoid hard dependency on `System.currentTimeMillis()`.

```java
long now();
```

This design makes TTL logic deterministic and testable.

---

### 6.2 Entry

**Package:** `core`

Represents a single immutable key-value record.

Fields:

* `key`
* `value`
* `expiryTime` (epoch millis, `-1` means no expiration)

The `Entry` class contains **no TTL logic**.

---

### 6.3 ExpirationPolicy

**Package:** `expiration`

Responsible only for answering:

> *Is this entry expired at the given time?*

The default policy uses absolute expiry time and lazy expiration.

---

### 6.4 StorageEngine

**Package:** `core`

A policy-free abstraction over storage.

Responsibilities:

* Store entries
* Retrieve entries
* Remove entries

The storage layer does **not**:

* Check expiration
* Track time
* Apply eviction

---

### 6.5 KeyValueStore

**Package:** `api`

Public-facing behavior contract.

Responsibilities:

* Convert TTL to absolute expiry
* Enforce correct GET behavior
* Perform lazy expiration cleanup

Expired entries are **never visible** to clients.

---

### 6.6 ConsoleApp

**Package:** `cli`

Thin CLI layer responsible for:

* Parsing user input
* Calling `KeyValueStore`
* Printing output

Contains **zero business logic**.

---

## 7. TTL Behavior (v1)

| Scenario | Result                 |
| -------- | ---------------------- |
| No TTL   | Entry never expires    |
| TTL > 0  | Expires at `now + TTL` |
| TTL = 0  | Expires immediately    |
| TTL < 0  | Treated as expired     |

Expiration is handled lazily during `GET` operations.

---

## 8. Example Session

```
PUT a hello
GET a
hello

PUT b world 1000
GET b
world

(wait 1s)
GET b
(nil)
```

---

## 9. Design Invariants (v1)

* Expired data is never returned
* Storage does not enforce policy
* Time is injectable, not global
* Each class has a single responsibility
* Correctness is prioritized over performance

---

## 10. What Comes Next (v2 Preview)

v2 will introduce:

* Thread-safe access
* Background expiration cleanup
* Locking strategies
* Safe concurrent reads and writes

All v1 abstractions are designed to support this evolution **without breaking changes**.

---

## 11. Author Notes

This project is intentionally built **slowly and explicitly** to strengthen system design fundamentals.

Every abstraction exists for a reason.
Every feature is added only when justified.

---

**End of v1 Documentation**
