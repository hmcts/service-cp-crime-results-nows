# ADR-001: Queue-based Hearing_Resulted ingestion for Nows read API

## Status

Proposed

## Date

2026-08-27

## Context

This service exposes NOW (Notice/Order/Warrant) document data — generated when a court hearing is
resulted — to API Marketplace subscribers. A `Hearing_Resulted` event already fires on every
resulted hearing.

This ADR settles whether this service serves reads from its own store or proxies upstream.

## Decision

**Ingestion is asynchronous, via a Service Bus queue.** This service consumes
`nows.hearing-resulted`, a Terraform-provisioned queue on the platform's shared Service Bus
namespace. The queue name is a stable constant, not `@Value`-injected. The application fails to
start if the queue is unreachable — no in-process fallback for a missing infrastructure resource.

**Reads are served from this service's own Postgres store, not proxied live.** On message
receipt, this service resolves the full hearing detail, persists NOW document metadata, and
serves reads from that store directly.

Hearing-detail resolution needs a completeness retry with backoff, since the event and the
query-side view it depends on are updated by different processes and can race. The exact
resolution strategy is a follow-up implementation detail.

## Options considered

| Option | Pros | Cons |
|---|---|---|
| **Service Bus queue (chosen)** | Decouples from producer availability; native redelivery/dead-lettering | Needs a queue provisioned and something upstream to route events onto it |
| Synchronous webhook | No queue to provision | This service owns inbound auth and the producer's retry policy |
| Live proxy reads (no store) | No store/pipeline to build | Read availability tied to upstream uptime; no history |

## Consequences

- Needs its own Postgres schema/migrations and a Service Bus consumer — new build-out.
- Dead-letter/redelivery backoff strategy for the consumer is a follow-up design item.
- `Hearing_Resulted` is published onto `nows.hearing-resulted` via Event Grid.
- Persisting a meaningful record depends on a generation-gate decision (which NOW variant
  applies) — needs its own design.
- Local store enables full version history per record.

## Compliance notes

NOW data is expected to carry defendant PII. Treat as OFFICIAL-SENSITIVE from day one — no PII in
logs, error responses, or test fixtures.
