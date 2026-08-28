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

Hearing-detail resolution needs a completeness retry, since the event and the query-side view it
depends on are updated by different processes and can race. Retry is queue-level, not in-process:
on an incomplete result the consumer completes the message and schedules a follow-up
(`ScheduledEnqueueTimeUtc`), the same pattern `service-cp-crime-results-pcr` settled on
(ADR-009) once its own queue-based ingestion went live — a dedicated retry-durations component
plus a retry-computation component, clamped to the last configured delay once attempts exceed the
list. This service's own retry durations/max-tries are a follow-up implementation detail; the
pattern itself is not.

## Consequences

- Needs its own Postgres schema/migrations and a Service Bus consumer — new build-out.
- Retry durations/max-tries for this service's own failure mode are a follow-up design item.
- `Hearing_Resulted` is published onto `nows.hearing-resulted` via Event Grid.
- Persisting a meaningful record depends on a generation-gate decision (which NOW variant
  applies) — needs its own design.
- Local store enables full version history per record.

## Compliance notes

NOW data is expected to carry defendant PII. Treat as OFFICIAL-SENSITIVE from day one — no PII in
logs, error responses, or test fixtures.
