# Worked example — single defendant, single eligible event type

**Illustrative only — not part of the formal design.** This walks the pipeline described in
[`../designs/2026-09-10-nows-generation-gate-technical-design.md`](../../docs/designs/2026-09-10-nows-generation-gate-technical-design.md)
(§3–§6) end to end for one concrete hearing, with full example payloads at every stage, so the
mechanics are easy to follow without cross-referencing the design doc's own (more abstract)
descriptions. See [`02-multiple-events-flow.md`](02-multiple-events-flow.md) for the same flow
where one defendant matches more than one event type.

**Scenario:** a first hearing where a defendant is convicted of theft and remanded in custody —
exactly one judicial result, matching exactly one NOW event type, `WEE_CustodialSentence`.

---

## Stage 0 — raw payload from Redis / `ResultsClient`

This is the `hearingDetails/internal` payload this service actually ingests — `HearingDetailsResponse`
(`domain/HearingDetailsResponse.java`), Redis-first with a REST fallback (design doc §4a). Not to be
confused with `api-cp-crime-results-pcr`'s own published `PcrHearingResult` — that's a different,
already-flattened, public-facing shape.

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
            "defendantCaseJudicialResults": [],
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

Note: this example includes `judicialResultTypeId` on the judicial result as if the design doc's §2
fix (this field is currently missing from this repo's `JudicialResult` model) has already landed —
every later stage depends on it.

---

## Stage 1 — defendant merge (design doc §3a)

One prosecution case, one defendant, no court applications, no hearing-wide
`defendantJudicialResults` — only one source contributes, so the fold is a straight pass-through:

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
        { "promptReference": "prisonOrganisationName", "value": "HMP/YOI Durham" },
        { "promptReference": "prisonEmailAddress1", "value": "OMU.Durham@justice.gov.uk" }
      ]
    }
  ]
}
```

(See [`02-multiple-events-flow.md`](02-multiple-events-flow.md) for a case where this step actually
folds two distinct sources together — here there's only one to begin with.)

---

## Stage 2 — vocabulary computation (design doc §3b)

Computed once from the merged defendant + hearing:

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

Sourced: custody ← `personDefendant.custodialEstablishment.custody`; custodial-outcome ← the
`prisonOrganisationName` prompt; CPS ← `prosecutor.isCps`; age group ← `defendant.isYouth`; court
language ← `courtCentre.welshCourtCentre`; attendance ← `defendantAttendance[].attendanceDays[]`
matched to the result's `orderedDate`. Major-creditor stays empty — there's no financial result here
for that dimension to find.

---

## Stage 3 — `nows-metadata` lookup and match (design doc §3c)

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
        {
          "resultDefinitionId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
          "primary": true,
          "parentNowRequirementId": null,
          "rootResultDefinitionId": null,
          "nowRequirements": []
        }
      ]
    }
  ]
}
```

Match: the merged defendant's one result has `judicialResultTypeId == 3f8e2a10-…`, equal to this
requirement's `resultDefinitionId`, and it's `primary` → `WEE_CustodialSentence` becomes a
**candidate**, with `matched_result_type_ids = ["3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11"]` (since
`includeAllResults: false`, only matching results are kept).

---

## Stage 4 — `now-subscriptions` lookup and match (design doc §3d)

```
GET .../referencedata/now-subscriptions?on=2026-09-02
```

```json
{
  "nowSubscriptions": [
    {
      "isNowSubscription": true,
      "isEDTSubscription": false,
      "applySubscriptionRules": true,
      "subscriptionVocabulary": {
        "anyAppearance": true,
        "anyMajorCreditor": true,
        "anyCourtHearing": true,
        "adultOrYouthDefendant": true,
        "inCustody": true,
        "custodyLocationIsPrison": true,
        "atleastOneCustodialResult": true,
        "includedPrompts": [],
        "excludedPrompts": [],
        "includedResults": [],
        "excludedResults": []
      }
    }
  ]
}
```

Every rule this subscription configures is satisfied by Stage 2's vocabulary (adult, in custody at a
prison, at least one custodial result, attended in person, English-language court, no include/exclude
list to check) → the candidate survives → **eligible**.

---

## Stage 5 — persisted (design doc §5)

| Table | Row |
|---|---|
| `now_hearing` | `hearing_id = 6988027f-e786-49f4-a00f-7c35ab459464`, `hearing_day = 2026-09-02` |
| `now_defendant` | `master_defendant_id = d2151771-…` |
| `now_defendant_case` | `case_urn = RC363968376`, `defendant_id = d2151771-…` |
| `now_defendant_snapshot` | `content` = the full resolved defendant/hearing/offences JSON (§6b below, minus the `eligibleEventTypes` wrapper) |
| `now_eligible_event` | `event_type = WEE_CustodialSentence`, `matched_result_type_ids = ["3f8e2a10-…"]`, `matched_at = 2026-09-02T18:05:00Z` |

---

## Stage 6a — thin discovery (design doc §6b) — subscriber only holds `hearingId`

```
GET /hearings/6988027f-e786-49f4-a00f-7c35ab459464
```

```json
[
  {
    "caseURN": "RC363968376",
    "defendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "eligibleEventTypes": [
      { "eventType": "WEE_CustodialSentence", "matchedAt": "2026-09-02T18:05:00Z" }
    ]
  }
]
```

No content here — deliberately (design doc §6b). This is how the subscriber *discovers* which
`caseURN`/`defendantId` to ask about next.

---

## Stage 6b — rich content (design doc §6a) — subscriber now calls the specific defendant

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
          "wording": "Attempt theft to vehicle",
          "convictionDate": "2026-09-02",
          "results": [
            {
              "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
              "cjsCode": "RIBA48",
              "label": "Remanded in custody",
              "orderedDate": "2026-09-02",
              "prompts": [
                { "promptReference": "prisonOrganisationName", "label": "Prison organisation name", "value": "HMP/YOI Durham" }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

This is built entirely from Stage 5's two stored rows — `now_defendant_snapshot.content` provides
the defendant/hearing/offences, filtered by `now_eligible_event.matched_result_type_ids` to decide
which offence/result entries appear under `WEE_CustodialSentence` — no second call to Redis,
`ResultsClient`, or `nows-metadata` needed at read time. `orderName` comes along for free too, since
it was already known at Stage 3 and is worth persisting on `now_eligible_event` alongside
`matched_result_type_ids` rather than re-deriving at read time.