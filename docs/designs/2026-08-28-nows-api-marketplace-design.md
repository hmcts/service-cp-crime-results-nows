# NOWs API Marketplace Service — Design

Implements [`ADR-003`](../pipeline/adrs/003-nows-api-marketplace-service-design.md) (AMP-907).
Incorporates [`ADR-001`](../pipeline/adrs/001-hearing-resulted-queue-ingestion-for-nows-read-api.md)
(ingestion/store shape) and [`ADR-002`](../pipeline/adrs/002-now-generation-gate-scope-and-record-keying.md)
(generation-gate matching) as already-decided detail — not restated here beyond what a diagram
needs.

## 1. Component architecture

```mermaid
flowchart LR
    EG["Event Grid<br/>Hearing_Resulted"] --> Q["Service Bus queue<br/>nows.hearing-resulted"]
    Q --> C["service-cp-crime-results-nows<br/>(peek-lock consumer)"]
    C --> HD["Hearing detail resolution<br/>(cache-then-fallback + completeness retry)"]
    HD --> VG["Vocabulary computation<br/>per masterDefendantId"]
    VG --> ND["NOW-definition match<br/>allow-list → requirement tree"]
    ND --> SM["Subscription / vocabulary match"]
    SM -->|eligible| P["Persist record<br/>(hearingId, masterDefendantId, eventType)"]
    SM -->|none eligible| NoOp["No record persisted"]
    P --> DB[("Postgres store")]
    DB --> API["GET read API"]
    API --> Sub["API Marketplace subscriber"]
```

## 2. Ingestion sequence

```mermaid
sequenceDiagram
    participant EG as Event Grid
    participant Q as Queue (nows.hearing-resulted)
    participant C as service-cp-crime-results-nows
    participant HD as Hearing detail source
    participant RD as Reference data<br/>(NOW-definitions, subscriptions)
    participant DB as Postgres store

    EG->>Q: Hearing_Resulted
    Q->>C: peek-lock message
    C->>HD: resolve hearing/defendant detail
    alt incomplete
        HD-->>C: incomplete
        C->>Q: complete + schedule follow-up (ScheduledEnqueueTimeUtc)
    else complete
        HD-->>C: full detail
        C->>RD: fetch NOW-definition catalogue + subscriptions
        RD-->>C: candidates + subscription rules
        Note over C: prune to allow-list, match requirement tree,<br/>match vocabulary (ADR-002)
        alt eligible event type(s)
            C->>DB: persist record(s) per (hearingId, masterDefendantId, eventType)
        else none eligible
            Note over C: no-op — nothing persisted
        end
        C->>Q: complete message
    end
```

## 3. Read sequence

```mermaid
sequenceDiagram
    participant Sub as API Marketplace subscriber
    participant API as service-cp-crime-results-nows
    participant DB as Postgres store

    Sub->>API: GET /cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}
    API->>DB: resolve defendantId → masterDefendantId
    DB-->>API: masterDefendantId
    API->>DB: read record(s) for (hearingId, masterDefendantId)
    DB-->>API: record(s) or none
    API-->>Sub: 200 with eligible NOW event type(s)
```

The subscriber addresses a resource by `caseURN`/`defendantId` — the identifiers they already
hold — never by `masterDefendantId`, which is this service's own internal merge key (ADR-002).
Resolving one to the other is this service's job, not the caller's. Endpoint path above is
illustrative — the OpenAPI contract in `api-cp-crime-results-nows` is not yet authored.

## 4. Open items

- Empty-result response shape (200 with empty array vs. 404) — not yet decided.
- Retry durations/max-tries for this service's own failure mode (ADR-001) — not yet sized.
- Store schema for the `(hearingId, masterDefendantId, eventType)` key — not yet migrated.
- `defendantId → masterDefendantId` resolution mechanism — likely a mapping persisted at
  ingestion time (the merge is already computed there), but not yet decided as its own schema.
