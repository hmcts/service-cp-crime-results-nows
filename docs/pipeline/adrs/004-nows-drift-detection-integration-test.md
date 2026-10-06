# ADR-004: Drift detection as a parameterized integration test

## Status

Proposed

## Date

2026-09-28

## Context

Design doc §9 recommends a golden-master check: this service reimplements legacy NOW eligibility
logic rather than calling it, so real hearings with a known legacy NOW outcome should be replayed
through the real code path and the output compared. PCR already built this mechanism
(`service-cp-crime-results-pcr` ADR 008, AMP-898); this ADR adopts it and records only where NOWS
differs.

## Decision

- **Same mechanism as PCR ADR 008.** `NowsDriftDetectionIntegrationTest` (package
  `integration.e2e.driftdetection`) is one `@ParameterizedTest` over every folder under
  `src/test/resources/drift-detection/`. Each run seeds Redis with the hearing payload, publishes
  `Hearing_Resulted` to the Service Bus emulator, and diffs the `GET` response per defendant with
  `JSONAssert` `NON_EXTENSIBLE`. Adding a hearing means adding a folder, not Java.
- **Fixture layout:**
  ```
  drift-detection/<hearing-name>/
    event.json              # results hearingDetails/internal payload (Redis-seed input)
    nows-metadata.json      # reference-data nows-metadata response for the sitting day
    now-subscriptions.json  # reference-data now-subscriptions response for the sitting day
    expected/<defendantId>.json
    reference/              # rendered NOW PDF + Docmosis template, provenance only
  ```
- **Both reference-data catalogues are stubbed, not just subscriptions.** The generation gate
  (ADR-002) needs the NOW-definition requirement tree as well as subscriptions, and the real
  definition is what makes the fixture a drift check rather than a restatement of test data.
- **`matchedAt` is made deterministic with a fixed `ClockService`, not an ignore-list.** It is the
  one volatile field in the response (set from `ClockService` on insert). Every `expected/*.json`
  carries the test's fixed instant.
- **Full response body is asserted**, not just event-type names — the filtered
  offences/results/prompts are what the NOW template renders, so content drift is caught too.
- **`expected/*.json` is bootstrapped from a real run, then checked against the rendered PDF and
  its template** field by field. A defendant who got no NOW is expected with an empty
  `eventTypes`.

## Consequences

- A mapping regression present before a fixture is first captured would be captured as correct;
  the PDF/template cross-check at capture time is the mitigation, as in PCR.
- Fixtures are real hearing data and must be redacted before commit — fabricated names, dates of
  birth, addresses, phone numbers and case URNs, no real government email domains.
- Runs in the normal `./gradlew test` suite and needs `docker compose up -d postgres redis
  servicebus`.