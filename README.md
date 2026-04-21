# Sonatype Lifecycle Demo App

A small Spring Boot e-commerce API purpose-built to demonstrate three Sonatype Lifecycle capabilities in a live session.

<!-- trigger scan -->

| Capability | What it shows |
|---|---|
| **Reachability Analysis** | Vulnerable code paths that are genuinely called at runtime → policy action **FAIL** |
| **Automated Waivers** | Vulnerable components whose risky code path is never invoked → auto-suppressed, zero developer toil |
| **Golden PRs** | Lifecycle surfaces fix-version upgrade PRs automatically for all 5 vulnerable deps |

---

## Vulnerable Dependencies & Demo Scenarios

| # | Dependency | Version | CVE | CVSS | Reachable? | Demo Scenario |
|---|---|---|---|---|---|---|
| 1 | `log4j-core` | 2.14.1 | CVE-2021-44228 (Log4Shell) | **10.0** | ✅ REACHABLE | Reachability → FAIL build |
| 2 | `commons-text` | 1.9 | CVE-2022-42889 (Text4Shell) | **9.8** | ✅ REACHABLE | Reachability → FAIL build |
| 3 | `h2` | 2.1.212 | CVE-2022-23221 | **9.8** | ❌ NOT REACHABLE | Automated Waiver |
| 4 | `commons-collections` | 3.2.1 | CVE-2015-6420 | **7.5** | ❌ NOT REACHABLE | Automated Waiver |
| 5 | `snakeyaml` | 1.30 | CVE-2022-25857 | **7.5** | — | Golden PR only |

---

## Architecture

```
HTTP Request
    │
    ├─ GET  /api/search?q=          → SearchService      → log4j logger.info(userQuery)
    │                                                        ↑ Log4Shell REACHABLE SINK
    │
    ├─ POST /api/orders             → OrderService
    │                               → NotificationService → StringSubstitutor.replace()
    │                                                        ↑ Text4Shell REACHABLE SINK
    │
    ├─ GET  /api/inventory          → InventoryService   → StringValueTransformer (safe)
    │                                                        ↑ commons-collections NOT REACHABLE
    │
    └─ GET  /api/products           → ProductService     (no vulnerable call path)
```

---

## Running Locally

```bash
# Requires Java 17 and Maven 3.8+
mvn clean spring-boot:run
# App starts on http://localhost:8080
```

### Sample Requests

```bash
# Triggers Log4Shell reachable path (SearchService → logger.info)
curl "http://localhost:8080/api/search?q=laptop"

# Triggers Text4Shell reachable path (OrderService → NotificationService → StringSubstitutor)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2, "customerEmail": "user@example.com"}'

# Triggers commons-collections NOT REACHABLE path (InventoryService → StringValueTransformer)
curl "http://localhost:8080/api/inventory/category/electronics"

# Standard product list
curl "http://localhost:8080/api/products"
```

---

## GitHub Actions CI

The workflow at `.github/workflows/sonatype-lifecycle.yml` runs a Lifecycle policy evaluation on every push and pull request.

### Required Secrets

Add these under **Settings → Secrets and variables → Actions**:

| Secret | Value |
|---|---|
| `IQ_SERVER_URL` | Your Lifecycle server URL |
| `IQ_USERNAME` | Service account username |
| `IQ_PASSWORD` | Service account password |

The `application-id` in the workflow is set to `sonatype-lifecycle-demo` — make sure an application with that ID exists in your Lifecycle instance.

---

## Live Demo Script

### Setup (before the session)
1. Push a commit to `main` to trigger the first scan and let Lifecycle surface violations.
2. In Lifecycle, confirm the application `sonatype-lifecycle-demo` has violations.
3. Pre-configure an **Automated Waiver** policy rule: *"If reachability = NOT REACHABLE → auto-waive"*.

---

### Scene 1 — Reachability Analysis (≈ 5 min)

> **Talking point:** *"Not every vulnerability in your dependency tree is actually exploitable. Lifecycle traces the real call graph."*

1. Open the Lifecycle scan results for `sonatype-lifecycle-demo`.
2. Show **CVE-2021-44228** on `log4j-core 2.14.1` — Lifecycle marks it **REACHABLE**.
   - Walk through: `SearchController.search()` → `SearchService.search()` → `logger.info(userQuery)`.
   - The user-supplied query string hits the Log4j sink directly.
3. Show **CVE-2022-42889** on `commons-text 1.9` — also **REACHABLE**.
   - Walk through: `OrderController.createOrder()` → `OrderService` → `NotificationService.buildOrderConfirmation()` → `StringSubstitutor.replace()`.
4. Both are flagged as **FAIL** by the build policy — the CI pipeline is blocked. ✅

---

### Scene 2 — Automated Waivers (≈ 5 min)

> **Talking point:** *"For vulnerabilities that can't be reached, we eliminate the toil entirely — no ticket, no manual review."*

1. Show **CVE-2015-6420** on `commons-collections 3.2.1` — Lifecycle marks it **NOT REACHABLE**.
   - Explain: `InventoryService` imports commons-collections but only calls `StringValueTransformer` (a safe `toString()` wrapper). `InvokerTransformer` — the gadget chain entry point — is never instantiated.
2. Show **CVE-2022-23221** on `h2 2.1.212` — also **NOT REACHABLE**.
   - Explain: The H2 web console (the attack surface) is explicitly disabled in `application.properties`.
3. The **Automated Waiver** rule fires on both → violations suppressed automatically, zero developer action required.
4. **Contrast:** developers only need to focus on the 2 REACHABLE findings, not all 4+. Developer toil cut in half. ✅

---

### Scene 3 — Golden PRs (≈ 5 min)

> **Talking point:** *"Lifecycle doesn't just tell you what's wrong — it opens the fix PR for you."*

1. Navigate to the **GitHub repository** → **Pull Requests**.
2. Show the auto-generated Golden PRs (one per vulnerable dependency with a known fix version):
   - `log4j-core` 2.14.1 → 2.17.2
   - `commons-text` 1.9 → 1.10.0
   - `h2` 2.1.212 → 2.1.214
   - `commons-collections` 3.2.1 → 3.2.2
   - `snakeyaml` 1.30 → 1.33
3. Merge one Golden PR live — the CI scan re-runs and the violation disappears. ✅

---

## Project Structure

```
sonatype-lifecycle-demo/
├── .github/
│   └── workflows/
│       └── sonatype-lifecycle.yml   # CI policy evaluation
├── src/main/java/com/sonatype/demo/
│   ├── DemoStoreApplication.java    # Spring Boot entry point
│   ├── DataInitializer.java         # Seeds 15 demo products
│   ├── controller/
│   │   ├── SearchController.java    # → Log4Shell REACHABLE
│   │   ├── OrderController.java     # → Text4Shell REACHABLE
│   │   ├── InventoryController.java # → commons-collections NOT REACHABLE
│   │   └── ProductController.java   # Standard CRUD
│   ├── service/
│   │   ├── SearchService.java       # logger.info(userQuery) ← REACHABLE SINK
│   │   ├── NotificationService.java # StringSubstitutor.replace() ← REACHABLE SINK
│   │   ├── InventoryService.java    # StringValueTransformer only (safe)
│   │   ├── OrderService.java        # Chains to NotificationService
│   │   └── ProductService.java      # No vulnerable calls
│   ├── model/
│   │   ├── Product.java
│   │   └── Order.java
│   └── repository/
│       ├── ProductRepository.java
│       └── OrderRepository.java
├── src/main/resources/
│   ├── application.properties       # H2 console disabled, log4j2 config
│   └── log4j2-spring.xml            # Log4j2 (not Logback) — keeps CVE-2021-44228 in graph
└── pom.xml                          # All 5 vulnerable deps with explanatory comments
```
