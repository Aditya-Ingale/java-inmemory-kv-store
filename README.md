# Java In-Memory Key–Value Store


A Redis-inspired **in-memory key–value store** built in Java as a **system-level learning project**.


This project is **not a Redis clone**. The goal is to understand how fast, in-memory systems are designed internally instead of using them as black boxes.



---


## Why This Project Exists


This project was created to:
- Understand how in-memory systems manage data
- Learn TTL (Time-To-Live) and expiration semantics deeply
- Reason about concurrency and race conditions
- Design eviction strategies (LRU) with real trade-offs
- Practice evolving a system **version by version without rewrites**


---


## Versioned Evolution


The system is intentionally built in **three explicit versions**.


| Version | Focus |
|------|------|
| v1 | Core correctness, TTL, single-threaded |
| v2 | Concurrency, thread safety, background cleanup |
| v3 | Memory limits, LRU eviction |


Each version **preserves the previous one**. Nothing is rewritten.


---


## Features by Version


### v1 – Core Store
- In-memory key–value storage
- `PUT` / `GET` commands
- TTL (Time-To-Live) support
- Lazy expiration
- Single-threaded correctness


### v2 – Concurrent Store
- Thread-safe `GET` / `PUT`
- `ConcurrentHashMap`-based storage
- Background expiration cleanup
- Safe concurrent expiration (compare-and-remove)


### v3 – Evicting Store (Default)
- Fixed memory limit (entry-count based)
- LRU (Least Recently Used) eviction
- Eviction + expiration interaction
- All v1 and v2 guarantees preserved


---


## How to Run the Project


### 1 Clone the Repository


```bash
git clone https://github.com/Aditya-Ingale/java-inmemory-kv-store.git
cd java-inmemory-kv-store
```


### 2 Compile & Run

From the project root:


```bash
javac -d out src/main/java/com/adi/kvstore/**/*.java
java -cp out com.adi.kvstore.cli.ConsoleApp
```


## Supported Console Commands


```
PUT key value
PUT key value ttlMillis
GET key
EXIT
```


### Example Session


```
PUT a hello
GET a
hello


PUT b world 1000
GET b
world
```

---


## Running Different Versions (Important)


The `ConsoleApp` is wired to **v3 by default**.


Inside `ConsoleApp.java`, you will find clearly commented sections for:
- v1 (`SimpleKVStore`)
- v2 (`ConcurrentKVStore`)
- v3 (`EvictingKVStore`)


To run a specific version:
1. Uncomment **only one** store initialization
2. Comment out the others


> ⚠️ **Only one version should be active at a time.**


This allows reviewers or learners to:
- Compare behaviors
- Understand evolution
- Run versions independently


---


## Project Structure (High-Level)


```
com.adi.kvstore
├── api # Public interfaces
├── cli # Console application
├── core # Core data & storage
├── expiration # TTL logic
├── concurrency # Thread safety & background cleanup
├── eviction # LRU eviction & memory limits
├── impl # Store implementations (v1, v2, v3)
└── time # Time abstraction
```

---
## Architecture & Design


This project follows a **layered and evolvable architecture**:
- Storage is policy-free
- Time is injected, not global
- Expiration and eviction are separate concerns
- Concurrency is added by composition


 **For full system design, invariants, trade-offs, and version-by-version evolution, see:**


 `ARCHITECTURE.md`


---


## Roadmap


- v3 (completed): LRU eviction & memory limits
- Possible future extensions:
- LFU eviction
- Metrics & observability
- Networked API


---


## Notes


- v1 and v2 implementations are intentionally preserved
- v3 is the default runtime store
- This project is designed for **learning**, not production deployment


---


**End of README**