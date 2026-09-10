# ADR-003: Notice(s)/Order(s)/Warrant(s) (NOWs) API Marketplace Service — Design

## Status

Proposed

## Date

2026-08-28

## Jira

AMP-907 — parent epic for this NOWs API Marketplace capability. ADR-001 (ingestion transport and
read-serving shape) and ADR-002 (generation-gate scope, event-type matching, and record keying)
are decisions made under this same epic.

## Context

Notice(s)/Order(s)/Warrant(s) (NOWs) — court-issued documents raised against judicial results,
covering custodial warrants, detention/supervision orders, and statutory notices — are generated
and distributed today by an existing internal pipeline. No programmatic, subscriber-facing read
access to this data exists for API Marketplace consumers; only the internal generation/distribution
channel does.

Any new integration pattern and any new public repo pair requires an ADR before proceeding.

## Decision

Build a new read channel — `api-cp-crime-results-nows` (OpenAPI spec) +
`service-cp-crime-results-nows` (Spring Boot service) — exposing NOW generation-eligibility data
via a pull `GET` API:

- **Not a replacement for existing NOW generation or distribution.** Whichever system generates
  and routes the actual NOW documents today keeps doing so unchanged; this is an additional,
  independent read channel, not a rebuild of it.
- **Does not rebuild recipient/subscriber management.** Recipient resolution, document assembly,
  and submission stay owned elsewhere. This service answers "would this NOW event type have been
  generated for this hearing/defendant" — nothing about who receives it or how.
- **Event-driven ingestion, not synchronous generation on request** — triggered by the same
  `Hearing_Resulted` signal already published on every resulted hearing (ADR-001).
- **A record is keyed by `(hearingId, masterDefendantId)`** — the merged defendant identity
  across every prosecution case and court application sharing that identity on a hearing, not a
  raw per-case identifier (ADR-002).
- Standard template scaffold for a new API Marketplace capability — no deviation needing its own
  ADR.

## Consequences

- Two new public repos exist, each requiring standard repo-ownership and coding-in-the-open
  process.
- This service sits downstream of whichever system publishes `Hearing_Resulted`. No
  recipient/subscriber integration is wired from this service — that stays owned elsewhere.
- Every subsequent design/ADR in this repo is scoped within this decision, not a re-litigation of
  it.

## Alternatives considered

- **Extend the existing internal generation pipeline with a pull endpoint** — rejected; that
  pipeline is generation-and-distribution-shaped, not a fit for a versioned, queryable REST
  resource, and this capability is Modern-by-Default, not built on that pipeline's runtime.
- **Have subscribers consume the internal generation system's own output directly** — rejected;
  that system has no external contract, and coupling subscribers to its internal shape would make
  every internal change a breaking one for them.

## Compliance notes

- NOW data is expected to carry defendant PII (ADR-001) — OFFICIAL-SENSITIVE from day one.
- Both new repos are public by default per coding-in-the-open policy; no private-repo case
  identified.
