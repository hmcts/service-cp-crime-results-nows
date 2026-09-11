# Worked example — single defendant, multiple eligible event types

**Illustrative only — not part of the formal design.** Same pipeline as
[`01-single-event-flow.md`](01-single-event-flow.md), described in
[`../designs/2026-09-10-nows-generation-gate-technical-design.md`](../../docs/designs/2026-09-10-nows-generation-gate-technical-design.md)
(§3–§6), but this time one defendant has **two** judicial results from two different sources in the
raw payload, each independently triggering a different NOW event type — `WEE_CustodialSentence` and
`NEE_FootballBanning`. This is the case that actually exercises the merge fold (Stage 1) and the
per-event-type content filtering (Stage 5's `matched_result_type_ids`), neither of which do
interesting work in the single-event example.

**Scenario:** same hearing and defendant as `01-single-event-flow.md` — convicted of theft, remanded
in custody — but this defendant is *also* given a football banning order at the same hearing, as a
case-level result (not tied to either offence).

---

## Stage 0 — complete raw payload from Redis / `ResultsClient`

```json
{
  "hearing": {
    "courtCentre": {
      "id": "f8254db1-1683-483e-afb3-b87fde5a0a26",
      "code": "B01LY00",
      "name": "Lavender Hill Magistrates' Court",
      "welshCourtCentre": false,
      "lja": { "ljaName": "South West London Magistrates' Court" },
      "address": {
        "address1": "176A Lavender Hill",
        "address2": "London",
        "address3": "",
        "address4": "",
        "address5": "",
        "postcode": "SW11 1JU"
      }
    },
    "hearingDays": [
      { "sittingDay": "2026-09-02" }
    ],
    "type": { "id": "1", "description": "First hearing" },
    "jurisdictionType": "MAGISTRATES",
    "defendantAttendance": [
      {
        "defendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
        "attendanceDays": [
          { "day": "2026-09-02", "attendanceType": "IN_PERSON" }
        ]
      }
    ],
    "defendantJudicialResults": [],
    "prosecutionCases": [
      {
        "id": "8f2f1b3a-6c3d-4a1b-9d7e-1234567890ab",
        "prosecutionCaseIdentifier": { "caseURN": "RC363968376" },
        "caseMarkers": [],
        "prosecutor": { "isCps": false },
        "defendants": [
          {
            "id": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
            "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
            "isYouth": false,
            "personDefendant": {
              "custodialEstablishment": {
                "id": "estab-durham",
                "name": "HMP/YOI Durham",
                "custody": "Prison"
              },
              "personDetails": {
                "title": "Mr",
                "firstName": "Lacy",
                "middleName": null,
                "lastName": "Braun",
                "dateOfBirth": "1998-09-02",
                "address": {
                  "address1": "221B Baker Street",
                  "address2": "Baker Street",
                  "address3": "Marylebone",
                  "address4": null,
                  "address5": null,
                  "postcode": "NW1 5BR"
                },
                "gender": "MALE",
                "nationalityDescription": null
              }
            },
            "defendantCaseJudicialResults": [
              {
                "cjsCode": "FB01",
                "judicialResultTypeId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9",
                "label": "Football banning order made",
                "resultText": "Football banning order made under section 14A Football Spectators Act 1989",
                "category": "FINAL",
                "postHearingCustodyStatus": "Not Applicable",
                "isFinancialResult": false,
                "isConvictedResult": true,
                "publishedForNows": false,
                "orderedDate": "2026-09-02",
                "nextHearing": null,
                "judicialResultPrompts": [
                  { "promptReference": "durationOfOrder", "label": "Duration of order", "value": "5 years", "type": "TEXT" },
                  { "promptReference": "additionalRequirements", "label": "Additional requirements", "value": "Report to police station within 5 days", "type": "TEXT" }
                ]
              }
            ],
            "offences": [
              {
                "id": "407e4865-3a51-4ede-90ef-ce92c6426cd5",
                "offenceCode": "TH68013A",
                "offenceTitle": "Attempt theft of motor vehicle",
                "wording": "Attempt theft to vehicle",
                "listingNumber": 1,
                "startDate": "2026-07-04",
                "endDate": null,
                "convictionDate": "2026-09-02",
                "plea": { "pleaValue": "GUILTY", "pleaDate": "2026-09-02" },
                "offenceLegislation": "Contrary to section 1(1) of the Criminal Attempts Act 1981.",
                "verdict": null,
                "allocationDecision": null,
                "indicatedPlea": null,
                "judicialResults": [
                  {
                    "cjsCode": "RIBA48",
                    "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
                    "label": "Remanded in custody",
                    "resultText": "Prosecutor's notice of appeal received. Remanded in custody",
                    "category": "FINAL",
                    "postHearingCustodyStatus": "Remanded in custody",
                    "isFinancialResult": false,
                    "isConvictedResult": true,
                    "publishedForNows": false,
                    "orderedDate": "2026-09-02",
                    "nextHearing": null,
                    "judicialResultPrompts": [
                      { "promptReference": "prisonOrganisationName", "label": "Prison organisation name", "value": "HMP/YOI Durham", "type": "TEXT" },
                      { "promptReference": "prisonEmailAddress1", "label": "Prison email address 1", "value": "OMU.Durham@justice.gov.uk", "type": "TEXT" }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "courtApplications": []
  },
  "sharedTime": "2026-09-02T18:00:35.800Z"
}
```

Two independent judicial results on this one defendant, from two different sources in the payload —
exactly what makes this a genuine "multiple events" case rather than a trivial merge:

- **`offences[0].judicialResults[0]`** (offence-level) — the custodial result,
  `judicialResultTypeId = 3f8e2a10-…` → will drive `WEE_CustodialSentence`.
- **`defendants[0].defendantCaseJudicialResults[0]`** (case-level, not tied to any offence) — the
  football banning result, `judicialResultTypeId = 9a1b2c3d-…` → will drive `NEE_FootballBanning`.

---

## Stage 1 — defendant merge (design doc §3a) — now genuinely folds two sources together

This is the case where the merge fold actually does work for a *single* defendant, not just across
cases: one result comes from `offences[].judicialResults[]`, the other from
`defendantCaseJudicialResults[]` — both land in the same `MergedDefendant.results[]`:

```json
{
  "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
  "isYouth": false,
  "defendantIds": ["d2151771-41a1-42e1-af36-a99d9b39c0b2"],
  "results": [
    {
      "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
      "orderedDate": "2026-09-02",
      "judicialResultPrompts": [
        { "promptReference": "prisonOrganisationName", "value": "HMP/YOI Durham" }
      ]
    },
    {
      "judicialResultTypeId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9",
      "orderedDate": "2026-09-02",
      "judicialResultPrompts": [
        { "promptReference": "durationOfOrder", "value": "5 years" },
        { "promptReference": "additionalRequirements", "value": "Report to police station within 5 days" }
      ]
    }
  ]
}
```

---

## Stage 2 — vocabulary computation (design doc §3b) — unchanged

Vocabulary is computed once, from the merged defendant as a whole — it doesn't change per result, so
it's identical to the single-event example (in custody, adult, English hearing, attended in person):

```json
{
  "custodyLocationIsPolice": false,
  "custodyLocationIsPrison": true,
  "inCustody": true,
  "atleastOneCustodialResult": true,
  "allNonCustodialResults": false,
  "atleastOneNonCustodialResult": false,
  "cpsProsecuted": false,
  "youthDefendant": false,
  "adultDefendant": true,
  "welshCourtHearing": false,
  "englishCourtHearing": true,
  "appearedInPerson": true,
  "appearedByVideoLink": false,
  "anyAppearance": true,
  "prosecutorMajorCreditor": [],
  "nonProsecutorMajorCreditor": []
}
```

---

## Stage 3 — `nows-metadata` lookup and match (design doc §3c) — two candidates now

```
GET .../referencedata/nows-metadata?on=2026-09-02
```

```json
{
  "nows": [
    {
      "id": "now-def-006",
      "name": "WEE_CustodialSentence",
      "includeAllResults": false,
      "nowRequirements": [
        { "resultDefinitionId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11", "primary": true, "nowRequirements": [] }
      ]
    },
    {
      "id": "now-def-032",
      "name": "NEE_FootballBanning",
      "includeAllResults": false,
      "nowRequirements": [
        { "resultDefinitionId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9", "primary": true, "nowRequirements": [] }
      ]
    }
  ]
}
```

Each of the merged defendant's two results matches a **different** definition's requirement tree —
two independent candidates, each with its own filtered result set:

- `WEE_CustodialSentence` → `matched_result_type_ids = ["3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11"]`
- `NEE_FootballBanning` → `matched_result_type_ids = ["9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9"]`

---

## Stage 4 — `now-subscriptions` lookup and match (design doc §3d) — different rules per candidate

```
GET .../referencedata/now-subscriptions?on=2026-09-02
```

```json
{
  "nowSubscriptions": [
    {
      "isNowSubscription": true, "applySubscriptionRules": true,
      "subscriptionVocabulary": {
        "anyAppearance": true, "anyMajorCreditor": true, "anyCourtHearing": true,
        "adultOrYouthDefendant": true, "inCustody": true, "custodyLocationIsPrison": true,
        "atleastOneCustodialResult": true
      }
    },
    {
      "isNowSubscription": true, "applySubscriptionRules": true,
      "subscriptionVocabulary": {
        "anyAppearance": true, "anyMajorCreditor": true, "anyCourtHearing": true,
        "adultOrYouthDefendant": true, "ignoreCustody": true, "ignoreResults": true
      }
    }
  ]
}
```

The second subscription (football banning) deliberately doesn't gate on custody or custodial outcome
at all — a banning order applies whether or not the defendant is in custody — so it's satisfied
regardless of Stage 2's custody flags. Both candidates survive → **two eligible event types**.

### Why this stage matters — and what happens when a candidate fails it

Stage 3 only asks a *structural* question: does this defendant have a result whose
`judicialResultTypeId` appears in this NOW definition's requirement tree? It has no idea whether this
particular defendant, in these particular circumstances, should actually get the document — two
defendants with the exact same custodial result could reasonably need different treatment (one in
police custody, one in prison custody; one CPS-prosecuted, one not), and Stage 3's tree-matching can't
express any of that. ADR-002 states this split explicitly: *"the first answers 'which document,' the
second answers 'generate it or not.'"* Stage 4 is the second — it's the only stage that can make a
Stage-3 candidate disappear entirely.

For **each** Stage-3 candidate independently, Stage 4 fetches the active subscriptions, filters to
`isNowSubscription: true`, and for each one runs the AND-chain (`matchVocabularyRules`): attendance,
major-creditor, court language, age group, custody, custodial outcome, then
`includedPrompts`/`excludedPrompts`/`includedResults`/`excludedResults` checked against *that
candidate's own* `matched_result_type_ids` — not the defendant's whole result set. A candidate
survives if **any** subscription's rules pass.

**Counterfactual — suppose `NEE_FootballBanning`'s subscription had instead been configured for
youth defendants only:**

```json
{ "isNowSubscription": true, "applySubscriptionRules": true,
  "subscriptionVocabulary": { "anyAppearance": true, "anyCourtHearing": true, "youthDefendant": true, "ignoreCustody": true, "ignoreResults": true } }
```

Our defendant's vocabulary has `youthDefendant: false`, `adultDefendant: true` (Stage 2).
`ageGroupMatches` requires `subVoc.youthDefendant && vocabulary.youthDefendant` — false && false → the
rule fails, and since no other subscription matches `NEE_FootballBanning` either, **the whole
candidate is dropped**. Effects, traced all the way through:

| | With the original subscription | With this counterfactual one |
|---|---|---|
| Stage 3 candidates | `WEE_CustodialSentence`, `NEE_FootballBanning` | `WEE_CustodialSentence`, `NEE_FootballBanning` — **unchanged**, Stage 3 already ran and found both |
| Stage 4 eligible | both | **only `WEE_CustodialSentence`** |
| `now_defendant_snapshot.content` | both results present | **unchanged** — still holds both results |
| `now_eligible_event` rows | 2 | **1** — no `NEE_FootballBanning` row is ever written |
| Stage 6a thin list | 2 entries | **1 entry** |
| Stage 6b rich response | 2 entries in `eligibleEventTypes[]` | **1 entry** — the football banning result exists in the snapshot but is never surfaced by either endpoint |

Two things worth taking from that trace:

1. **The snapshot doesn't know or care about Stage 4.** `now_defendant_snapshot` is built once from
   the raw payload (§5b), independent of subscription matching — it's `now_eligible_event` that
   Stage 4 controls. If circumstances or the subscription catalogue later changed such that
   `NEE_FootballBanning` *would* now match, the content is already sitting there ready — only a new
   eligibility decision (a new `now_eligible_event` row) would be needed, not a re-ingestion of the
   hearing.
2. **A candidate that fails Stage 4 doesn't show up as "ineligible" anywhere — it just doesn't
   appear.** There's no `{ "eventType": "NEE_FootballBanning", "eligible": false }` entry; §6d's "200
   with empty array" semantics apply per-candidate too, not just per-hearing — a subscriber has no way
   to distinguish "this NOW type was never structurally relevant" from "it was relevant but no
   subscription wanted it" purely from the API response. That's a deliberate simplification already
   noted in §3d, but worth knowing when debugging why something didn't show up.

The rest of this document continues with the **original** (both-eligible) subscriptions, not the
counterfactual.

---

## Stage 5 — persisted (design doc §5) — two `now_eligible_event` rows sharing one snapshot

| Table | Row(s) |
|---|---|
| `now_hearing` | 1 row |
| `now_defendant` | 1 row |
| `now_defendant_case` | 1 row |
| `now_defendant_snapshot` | **1 row** — `content` holds *both* results (offence-level custodial + case-level banning), written once |
| `now_eligible_event` | **2 rows**: `WEE_CustodialSentence` / `matched_result_type_ids=["3f8e2a10-…"]`, and `NEE_FootballBanning` / `matched_result_type_ids=["9a1b2c3d-…"]` |

This is exactly the case §5c's design exists for: one shared snapshot, two independently-filtered
views into it — no duplicated `content`.

---

## Stage 6a — thin discovery (design doc §6b)

```
GET /hearings/6988027f-e786-49f4-a00f-7c35ab459464
```

```json
[
  {
    "caseURN": "RC363968376",
    "defendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "eligibleEventTypes": [
      { "eventType": "WEE_CustodialSentence", "matchedAt": "2026-09-02T18:05:00Z" },
      { "eventType": "NEE_FootballBanning", "matchedAt": "2026-09-02T18:05:00Z" }
    ]
  }
]
```

Or, narrowed with `?eventType=NEE_FootballBanning` (design doc §6c) → just that one entry.

---

## Stage 6b — rich content (design doc §6a) — each event type sees a different slice of the same snapshot

```
GET /cases/RC363968376/hearings/6988027f-e786-49f4-a00f-7c35ab459464/defendants/d2151771-41a1-42e1-af36-a99d9b39c0b2
```

```json
{
  "caseURN": "RC363968376",
  "defendant": {
    "id": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "title": "Mr", "firstName": "Lacy", "middleName": null, "lastName": "Braun",
    "dateOfBirth": "1998-09-02",
    "address": {
      "address1": "221B Baker Street",
      "address2": "Baker Street",
      "address3": "Marylebone",
      "address4": null,
      "address5": null,
      "postCode": "NW1 5BR"
    },
    "gender": "MALE", "nationality": null
  },
  "hearing": {
    "id": "6988027f-e786-49f4-a00f-7c35ab459464",
    "courtDetails": {
      "court": { "courtHouseName": "Lavender Hill Magistrates' Court" },
      "ljaName": "South West London Magistrates' Court"
    },
    "hearingDate": "2026-09-02",
    "jurisdiction": "MAGISTRATES"
  },
  "eligibleEventTypes": [
    {
      "eventType": "WEE_CustodialSentence",
      "orderName": "Warrant for Custodial Sentence",
      "matchedAt": "2026-09-02T18:05:00Z",
      "caseResults": [],
      "offences": [
        {
          "code": "TH68013A",
          "title": "Attempt theft of motor vehicle",
          "results": [
            {
              "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
              "cjsCode": "RIBA48",
              "label": "Remanded in custody",
              "prompts": [
                { "promptReference": "prisonOrganisationName", "value": "HMP/YOI Durham" }
              ]
            }
          ]
        }
      ]
    },
    {
      "eventType": "NEE_FootballBanning",
      "orderName": "Notice of Football Banning Order",
      "matchedAt": "2026-09-02T18:05:00Z",
      "caseResults": [
        {
          "judicialResultTypeId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9",
          "cjsCode": "FB01",
          "label": "Football banning order made",
          "prompts": [
            { "promptReference": "durationOfOrder", "value": "5 years" },
            { "promptReference": "additionalRequirements", "value": "Report to police station within 5 days" }
          ]
        }
      ],
      "offences": []
    }
  ]
}
```

Notice `caseResults`/`offences` swap which one is populated between the two entries — that's
`matched_result_type_ids` doing its job: `WEE_CustodialSentence` only ever sees the offence-level
custodial result, `NEE_FootballBanning` only ever sees the case-level banning result, even though
both come out of the exact same `now_defendant_snapshot.content` row. Neither entry leaks the other
event type's unrelated result — which is exactly what a real NOW document's own template would need
(a football-banning notice has no business printing the theft offence's custodial detail, and vice
versa).