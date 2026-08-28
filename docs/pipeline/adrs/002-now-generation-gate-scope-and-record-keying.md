# ADR-002: NOW generation-gate scope, event-type matching, and record keying

## Status

Proposed

## Date

2026-08-28

## Context

ADR-001 settled ingestion transport and read-serving shape, and deferred one item: persisting a
meaningful record depends on deciding which NOW document type(s) apply to a resulted hearing. This
ADR settles that decision — the generation gate.

A hearing can produce several distinct NOW document types (warrants, notices, orders), each
routed to a different audience, unlike a single-document-per-defendant capability. The set of NOW
types this service may ever need to report on is fixed by
`service-cp-crime-hearing-results-document-subscription`'s registered `event_type` table — 40
entries today, each a string like `WEE_CustodialSentence` or `NEE_FootballBanning`. A NOW type
with no matching row there has no subscriber wired to receive it and must never be reported.

## Decision

**Scope: generation-gate only.** This service decides which registered NOW event type(s) would be
generated for a hearing/defendant. Recipient resolution, document assembly, and submission are out
of scope.

**Output is a set of eligible event types, not a boolean.** A single hearing can yield zero, one,
or several eligible NOW types simultaneously.

**The event-type universe is a fixed, static allow-list in this service — never fetched from HRDS
at runtime.** Sourced once from the registered `event_type` table and kept as a named constant.
Consequence: a new type registered there later needs a manual update here; this is an accepted,
infrequent maintenance cost, not treated as auto-syncing.

**Two independent reference-data lookups are required, not one:**
1. A NOW-definition catalogue mapping a judicial result's type identifier to a NOW template name,
   via a nested requirement tree. This resolves *which* event type a judicial result could trigger.
2. A subscription catalogue of vocabulary-based eligibility rules (custody location, custodial
   outcome, CPS-prosecution, age group, court language, attendance, major-creditor status). This
   resolves *whether* a matched event type is actually eligible.

Neither lookup can substitute for the other — the first answers "which document," the second
answers "generate it or not."

**Filtering order: prune to the allow-list before requirement-matching.** The fetched NOW-definition
catalogue is filtered down to only the entries whose template name is in the fixed 40-item
allow-list *before* any requirement-tree matching runs. This avoids computing eligibility for
document types this service can never report, and guarantees by construction that nothing outside
the registered set is ever surfaced.

**Ingestion is scoped to `Hearing_Resulted` only.** A separately-published `SJP_Hearing_Resulted`
event exists for Single Justice Procedure hearings; SJP hearings never produce NOW documents, so
this gate has no SJP-specific path.

**A record is keyed by `(hearingId, masterDefendantId)`, not a raw per-case `defendantId`.**
Vocabulary computation and NOW-definition matching are both evaluated against the merged defendant
view — one physical defendant can span multiple prosecution-case `defendantId`s and court
applications on the same hearing, and a NOW record represents that merged identity directly, not
one raw case-defendant pairing. This is a deliberate keying decision for this service's own store
(ADR-001), made explicitly here rather than left implicit.

**Vocabulary dimensions computed per defendant:** custody location, custodial-outcome, CPS-prosecution,
age group, court language, attendance (matched against real per-day attendance records, not a
stand-in), and major-creditor status (via a compliance-enforcement/major-creditor lookup). Computed
by merging across every prosecution case and court application sharing the defendant's master
identity on the hearing.

**Explicitly deferred:** audience-specific document variant cloning, the EDT sibling channel,
per-variant recipient/user-group whitelisting, document assembly, and reshare/amendment handling.
These are recipient- and delivery-shaped concerns, not generation-eligibility ones.

## Consequences

- Two new reference-data clients and domain models are needed — a NOW-definition/requirement
  catalogue client, and a subscription/vocabulary catalogue client. Neither is a drop-in reuse of a
  single existing client.
- The fixed event-type allow-list is a manual-sync point against
  `service-cp-crime-hearing-results-document-subscription`'s registered types — no automated
  drift check exists yet.
- The store's schema (ADR-001) must key its NOW record on `(hearingId, masterDefendantId)`, not a
  raw `defendantId` — this has schema and ingestion-mapping implications beyond this ADR's own
  scope.
- A record is only ever persisted once both the definition match and the subscription/vocabulary
  match succeed for a given event type.

## Compliance notes

No new PII-handling posture beyond ADR-001 — vocabulary computation reads existing hearing/defendant
data already in scope there. Reference-data responses used for matching carry no PII of their own.
