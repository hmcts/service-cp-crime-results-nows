# NOWs content payload — current vs enhanced

Comparison only — no design docs changed. "Current" is the `content` shape as already documented in
[`../../docs/designs/2026-09-10-nows-generation-gate-technical-design.md`](../../docs/designs/2026-09-10-nows-generation-gate-technical-design.md)
§5b/§6a and the worked examples in [`../../docs/e2eFlow/`](../../docs/e2eFlow/). "Enhanced" folds in
the gap-closing research from this session: what's addable now with confidence, what's addable
pending one contract confirmation, and what genuinely isn't closable. Sourced from
[`../gaps/api-response-gaps.md`](../gaps/api-response-gaps.md) and
[`../gaps/missing-fields-by-template.md`](../gaps/missing-fields-by-template.md).

All example values below are synthetic/placeholder — no real case, defendant, or reference data.

---

## 1. Current payload

### Schema

| Section | Field | Notes |
|---|---|---|
| Root | `caseURN` | |
| Defendant | `id`, `masterDefendantId` | |
| Defendant | `title`, `firstName`, `middleName`, `lastName`, `dateOfBirth` | |
| Defendant | `address.{address1-5, postCode}` | |
| Defendant | `gender`, `nationality` | |
| Hearing | `id` | |
| Hearing | `courtDetails.court.courtHouseName`, `courtDetails.ljaName` | |
| Hearing | `hearingDate`, `jurisdiction` | |
| Per eligible event type | `eventType`, `orderName`, `matchedAt` | |
| Per eligible event type | `caseResults[]` | case-level results (e.g. a football banning order) filtered to this event type |
| Per eligible event type | `offences[]` | offence-level results filtered to this event type |
| Offence | `code`, `title`, `wording`, `convictionDate` | |
| Result | `judicialResultTypeId`, `cjsCode`, `label`, `orderedDate` | |
| Result | `prompts[].{promptReference, label, value}` | |

### Example

```json
{
  "caseURN": "example-case-urn",
  "defendant": {
    "id": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "title": "Mr", "firstName": "Example", "middleName": null, "lastName": "Defendant",
    "dateOfBirth": "1998-09-02",
    "address": { "address1": "1 Example Street", "address2": "Example District", "address3": "London", "address4": null, "address5": null, "postCode": "SW1A 1AA" },
    "gender": "MALE", "nationality": null
  },
  "hearing": {
    "id": "6988027f-e786-49f4-a00f-7c35ab459464",
    "courtDetails": { "court": { "courtHouseName": "Example Magistrates' Court" }, "ljaName": "Example Local Justice Area" },
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
          "code": "TH68013A", "title": "Attempt theft of motor vehicle",
          "wording": "Attempt theft to vehicle", "convictionDate": "2026-09-02",
          "results": [
            { "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11", "cjsCode": "RIBA48", "label": "Remanded in custody", "orderedDate": "2026-09-02",
              "prompts": [ { "promptReference": "prisonOrganisationName", "label": "Prison organisation name", "value": "example prison name" } ] }
          ]
        }
      ]
    }
  ]
}
```

---

## 2. Enhanced payload

New/changed fields marked `// NEW`. Confidence tier noted per addition — see §4 for what each tier means.

### Schema

| Section | Field | Notes |
|---|---|---|
| Root | `caseURN` | unchanged |
| Defendant | `id`, `masterDefendantId` | unchanged |
| Defendant | `title`, `firstName`, `middleName`, `lastName`, `dateOfBirth` | unchanged |
| Defendant | `address.{address1-5, postCode}` | unchanged |
| Defendant | `gender`, `nationality` | unchanged |
| Defendant | **`pncId`** `// NEW (Tier 2)` | |
| Defendant | **`contact.{home, mobile}`** `// NEW (Tier 2)` | |
| Defendant | **`nationalInsuranceNumber`** `// NEW (Tier 2)` | |
| Defendant | **`occupation`** `// NEW (Tier 2)` | |
| Defendant | **`ethnicity.selfDefinedEthnicityDescription`** `// NEW (Tier 2)` | |
| Defendant | **`prosecutingAuthorityReference`** `// NEW (Tier 2)` | ASN, joined across the merged defendant's cases |
| Hearing | `id` | unchanged |
| Hearing | `courtDetails.court.courtHouseName`, `courtDetails.ljaName` | unchanged |
| Hearing | **`courtDetails.ljaCode`** `// NEW (Tier 1)` | |
| Hearing | `hearingDate`, `jurisdiction` | unchanged |
| Hearing | **`courtApplications[]`** `// NEW (Tier 2)` | see below — was reduced to nothing beyond a merge key; now full party detail |
| courtApplications[] | `applicationReference` | already sourced (unchanged), just not previously shown in this example |
| courtApplications[] | **`applicant.{title, firstName, middleName, lastName, dateOfBirth, address, contact, nationalInsuranceNumber, occupation, ethnicity}`** `// NEW (Tier 2)` | same extended party shape as Defendant |
| courtApplications[] | **`respondents[].{...same party shape...}`** `// NEW (Tier 2)` | |
| Per eligible event type | `eventType`, `orderName`, `matchedAt` | unchanged |
| Per eligible event type | **`nowText[].{label, value, welshValue}`** `// NEW (Tier 1)` | template-wide static text |
| Per eligible event type | **`nowRequirementText[].{label, value, welshValue}`** `// NEW (Tier 1)` | requirement-scoped static text |
| Per eligible event type | `caseResults[]`, `offences[]` | unchanged |
| Offence | `code`, `title`, `wording`, `convictionDate` | unchanged |
| Offence | **`count`, `orderIndex`** `// NEW (Tier 2)` | |
| Result | `judicialResultTypeId`, `cjsCode`, `label`, `orderedDate` | unchanged |
| Result | `prompts[].{promptReference, label, value}` | unchanged |

### Example

```json
{
  "caseURN": "example-case-urn",
  "defendant": {
    "id": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "title": "Mr", "firstName": "Example", "middleName": null, "lastName": "Defendant",
    "dateOfBirth": "1998-09-02",
    "address": { "address1": "1 Example Street", "address2": "Example District", "address3": "London", "address4": null, "address5": null, "postCode": "SW1A 1AA" },
    "gender": "MALE", "nationality": null,
    "pncId": "example-pnc-id",
    "contact": { "home": "example-landline", "mobile": "example-mobile" },
    "nationalInsuranceNumber": "example-ni-number",
    "occupation": "example-occupation",
    "ethnicity": { "selfDefinedEthnicityDescription": "example-ethnicity-description" },
    "prosecutingAuthorityReference": "example-asn"
  },
  "hearing": {
    "id": "6988027f-e786-49f4-a00f-7c35ab459464",
    "courtDetails": { "court": { "courtHouseName": "Example Magistrates' Court" }, "ljaName": "Example Local Justice Area", "ljaCode": "example-lja-code" },
    "hearingDate": "2026-09-02",
    "jurisdiction": "MAGISTRATES",
    "courtApplications": []
  },
  "eligibleEventTypes": [
    {
      "eventType": "WEE_CustodialSentence",
      "orderName": "Warrant for Custodial Sentence",
      "matchedAt": "2026-09-02T18:05:00Z",
      "nowText": [
        { "label": "orderText", "value": "example static template text for this NOW type", "welshValue": null }
      ],
      "nowRequirementText": [],
      "caseResults": [],
      "offences": [
        {
          "code": "TH68013A", "title": "Attempt theft of motor vehicle",
          "wording": "Attempt theft to vehicle", "convictionDate": "2026-09-02",
          "count": 1, "orderIndex": 1,
          "results": [
            { "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11", "cjsCode": "RIBA48", "label": "Remanded in custody", "orderedDate": "2026-09-02",
              "prompts": [ { "promptReference": "prisonOrganisationName", "label": "Prison organisation name", "value": "example prison name" } ] }
          ]
        }
      ]
    }
  ]
}
```

**Illustrative-only snippet — a court application with a full applicant/respondent (not part of the
worked scenario above, which has none):**

```json
{
  "applicationReference": "example-application-reference",
  "applicant": {
    "title": "Ms", "firstName": "Example", "lastName": "Applicant", "dateOfBirth": "1985-03-11",
    "address": { "address1": "1 Example Row", "postCode": "SW1A 2AA" },
    "contact": { "home": null, "mobile": "example-mobile" },
    "nationalInsuranceNumber": "example-ni-number", "occupation": "example-occupation",
    "ethnicity": { "selfDefinedEthnicityDescription": "example-ethnicity-description" }
  },
  "respondents": [
    {
      "title": "Mr", "firstName": "Example", "lastName": "Respondent", "dateOfBirth": "1998-09-02",
      "address": { "address1": "1 Example Street", "postCode": "SW1A 1AA" }
    }
  ]
}
```

---

## 3. Field-by-field diff

| Field | Current | Enhanced | Confidence |
|---|---|---|---|
| `hearing.courtDetails.ljaCode` | absent | **added** | Tier 1 — confirmed on the wire (`results-shared-v3` event fixture, LJA code sits right next to LJA name on the same object) |
| `eligibleEventTypes[].nowText[]` | absent | **added** | Tier 1 — same `nows-metadata` response already fetched for requirement-tree matching |
| `eligibleEventTypes[].nowRequirementText[]` | absent | **added** | Tier 1 — same source as above |
| `defendant.pncId` | absent | **added** | Tier 2 — confirmed in upstream domain; `hearingDetails/internal` contract itself not verified |
| `defendant.contact.{home,mobile}` | absent | **added** | Tier 2 |
| `defendant.nationalInsuranceNumber` | absent | **added** | Tier 2 |
| `defendant.occupation` | absent | **added** | Tier 2 |
| `defendant.ethnicity.selfDefinedEthnicityDescription` | absent | **added** | Tier 2 |
| `defendant.prosecutingAuthorityReference` | absent | **added** | Tier 2 — needs joining across a merged defendant's cases |
| `hearing.courtApplications[].applicant.*` | absent (only an internal merge key existed) | **added** | Tier 2 — same extended party shape as defendant |
| `hearing.courtApplications[].respondents[].*` | absent | **added** | Tier 2 |
| `offences[].count` | absent | **added** | Tier 2 |
| `offences[].orderIndex` | absent | **added** | Tier 2 |
| `amendmentDate` (anywhere) | absent | **still absent** | Not closable by this service alone — see §4c below |
| everything else | — | unchanged | — |

---

## 4. Fields deliberately NOT included, listed for reference

### 4a. Genuinely unused — no template's merge fields need them, and nothing in the decision gate (vocabulary computation) reads them either

| Field | Why it's excluded |
|---|---|
| `prosecutionCase.caseMarkers[]` | Zero references across all 40 templates' merge fields |
| `offence.offenceCode`, `.listingNumber`, `.startDate`, `.endDate` | Zero references |
| `offence.verdict` (`.verdictType.verdictCode`/`.description`) | Zero references |
| `offence.allocationDecision.motReasonDescription` | Zero references |
| `offence.indicatedPlea.indicatedPleaValue` | Zero references |
| `offence.plea.{pleaValue, pleaDate}` | Zero references |
| `judicialResult.isFinancialResult`, `.isConvictedResult`, `.publishedForNows` | Zero references |
| `judicialResult.nextHearing` (whole object) | Zero references — the one apparent hit was a false positive (a prompt literally named `tVLinkAtNextHearing`, not real use of the `NextHearing` object) |
| `courtApplication.courtApplicationCases[]`, `.courtOrder`/`.courtOrderOffences[]` | Offence-linkage plumbing — the linked offences are already reachable via the top-level `offences[]` this payload carries |
| `personDefendant.custodialEstablishment.id`/`.name` | Zero references (`.custody` is excluded for a different reason — see 4b) |
| `hearing.hearingDays[]`/`hearingDay.sittingDay`, `hearing.type` | Zero references — `hearingDate` is sourced from the matched judicial result's own date, not this |

### 4b. Not printed by any template, but required internally for the vocabulary computation (§3b of the design doc) — must stay in the *ingestion* model regardless; excluded from `content` only because there's nothing to display, not because they're unused

| Field | What it's used for internally |
|---|---|
| `prosecutionCase.prosecutor.isCps` | CPS-prosecution vocabulary dimension |
| `defendant.isYouth` | Age-group vocabulary dimension |
| `courtCentre.welshCourtCentre` | Court-language vocabulary dimension |
| `personDefendant.custodialEstablishment.custody` | Custody-location vocabulary dimension |
| `hearing.defendantAttendance[]`/`attendanceDay.{day,attendanceType}` | Attendance vocabulary dimension |
| `hearing.jurisdictionType` | Governs whether `ljaName`/`ljaCode` get populated at all (MAGISTRATES-only, per the legacy mapper's own rule) — a mapping-time input, not a display field |

### 4c. Confirmed real, but not addable by this service alone

| Field | Why |
|---|---|
| `amendmentDate` | Exists upstream (`ResultLine.amendmentDate`), but the actual cross-context event isn't confirmed to flatten it inline — it's a separate `newAmendmentResults[]` array correlated by result id. A cross-team ask, not a payload design choice. |

### 4d. Out of scope on purpose, not a gap

| Field | Why |
|---|---|
| `orderAddressee.*` (name/address) | Recipient resolution — explicitly owned elsewhere, not this service's concern |