# NOW document generation: can a NOWs API consumer correlate the data to the PDF?

*Technical evidence and analysis. For a plain-English summary, see the companion document,
[2026-09-11-now-document-generation-correlation-analysis-business-summary.md](2026-09-11-now-document-generation-correlation-analysis-business-summary.md).*

## Status

Investigation — not a decision record. No code in this service is changed by this document. Written
to answer one question before the NOWs API Marketplace channel is published: if a subscriber reads
structured judicial-result data from this API, can they work out what the corresponding NOW PDF
actually says, and in what order? The answer, evidenced below, is no — not reliably, and the gap is
structural, not a one-off template bug.

## Scope

- **In scope:** how `cpp-context-system-doc-generator` turns a `NowDocumentContent` payload into a
  rendered PDF, across all 40 NOW event types HRDS's `event_type` table registers (ids 2–41); what
  `cpp-context-hearing-nows` actually sends onward to HRDS today.
- **Out of scope:** this service's own generation-gate/data-store design (covered by the other design
  docs in this folder) and any proposed fix. This document is evidence-gathering only.

## 1. Summary

A NOW document is assembled from three JSON shapes with no declared relationship to each other:

1. **Structured result data** — `defendant.defendantResults[]` and
   `cases[].defendantCaseOffences[].results[]`, each with its own `prompts[]`.
2. **`distinctPrompts[]`** — a flat, hearing-wide, deduplicated-by-value array the Docmosis template
   reads by `promptReference` string lookup.
3. **`nowText[]`** — a flat array of label/value pairs holding static legal boilerplate, read by
   `label` string lookup.

The Docmosis `.docx` template is the *only* place that encodes which of these values appear in which
sentence, in which order, and under which condition. That wiring lives in binary template field codes
— it is never represented in the JSON payload. A consumer with the full JSON cannot reconstruct the
PDF's sentence structure, and in several templates cannot even know which *result* the PDF renders at
all, because the template selects it by a hardcoded reference-data GUID.

This is systemic: 39 of the 40 registered templates use these lookup-by-string helpers, and 12 of the
40 hardcode at least one literal result-identifier GUID directly in the template body.

## 2. Lifecycle — from Common Platform result to rendered PDF

Traced directly from `cpp-context-azure-legalaidagency`'s durable-function orchestration
(`HearingResultedNowsHandler`), `cpp-context-hearing-nows`, and `cpp-context-system-doc-generator`
source. Included to show where the payload this document analyses actually comes from, and where this
service's own proposed API would sit in the same chain.

```mermaid
sequenceDiagram
    participant CP as Common Platform (cpp-context-results)
    participant EG as Event Grid
    participant LAA as Azure Legal Aid Agency
    participant RD as Reference Data
    participant HN as Hearing NOWs
    participant SDG as System Doc Generator
    participant HRDS as HRDS

    CP->>EG: Hearing_Resulted
    EG->>LAA: HearingResultedNowsEventGridTrigger starts HearingResultedNowsHandler
    LAA->>LAA: HearingResultedCacheQuery (Redis), SetComplianceEnforcement
    LAA->>RD: GET nows-metadata?on=&lt;orderDate&gt;
    RD-->>LAA: NOW-definition catalogue (nowRequirements per type)
    LAA->>RD: GET now-subscriptions?on=&lt;orderDate&gt;
    RD-->>LAA: active subscribers per NOW type
    LAA->>LAA: SetNowVariants — matches judicial results against the catalogue
    LAA->>LAA: CloneNowsForUserGroupVariants, SetMDEVariants, NowsVariantsSubscriptions
    LAA->>RD: GET organisation-units/{id}, enforcement-area?..., prisons-custody-suites
    RD-->>LAA: org/court/prison contact detail
    LAA->>LAA: OutboundNowsVariants — NowDocumentRequestMapper builds the nowContent payload
    LAA->>HN: POST .../hearingnows-command-api/.../add-now-document
    HN->>HN: persist hearingnows.nows_document_request row (file-service)
    HN->>SDG: systemdocgenerator.generate-document command
    SDG->>SDG: Docmosis render — dp./nt./dr. field lookups against the payload (§3)
    SDG-->>HN: rendered PDF
    HN->>HRDS: POST /notifications (identity/case-URN projection only — §6)
```

Reference-data calls made during enrichment (all `GET`, against
`REFERENCE_DATA_CONTEXT_API_BASE_URI/referencedata-query-api/query/api/rest/referencedata/...`,
`cpp-context-azure-legalaidagency/.../NowsHelper/service/ReferenceDataService.js`):

| Call | Narration |
|---|---|
| `nows-metadata?on=<orderDate>` | The NOW-definition catalogue for that date — every document type's requirement/prompt matching rules. `SetNowVariants` matches a hearing's primary judicial results against this to decide which NOW variant(s), if any, apply. |
| `now-subscriptions?on=<orderDate>` | Which parties/organisations are actively subscribed to receive which NOW type on that date. Decides *who* gets a copy, not *whether* one is generated — a separate concern from the metadata match above. |
| `organisation-units/{id}` | Resolves an organisation-unit id (e.g. the ordering court) to its full name/address/contact detail. Populates `orderingCourt`-style fields in the NOW content. |
| `major-creditors` | Resolves financial-penalty creditor organisation detail — e.g. which court/body a fine is payable to — for financial NOW variants only. |
| `enforcement-area?postcode=...` | Resolves which enforcement area administers a given postcode, for financial-penalty/enforcement-routing content. |
| `enforcement-area?localJusticeAreaNationalCourtCode=...` | Same enforcement-area lookup, keyed by LJA code instead of postcode, for hearings where no postcode is available. |
| `prisons-custody-suites` | The reference list of prison/custody-suite organisations, including contact email addresses — the likely source of the `prisonOrganisationName`/`prisonEmailAddress1`/`prisonEmailAddress2` prompt values seen in the worked example (HMP/YOI Durham). |

**Where the proposed NOWs API sits in this chain**: this service's proposed read API would expose data
shaped almost identically to the `nowContent` payload `OutboundNowsVariants`/`NowDocumentRequestMapper`
builds and `hearingnows.nows_document_request` persists — the same `defendantResults[]`/
`cases[].defendantCaseOffences[].results[]`/`prompts[]`/`nowText[]`/`distinctPrompts[]` shape this
document analyses throughout §3–§5. That is precisely why the correlation gap matters here: a consumer
of this service's API would be reading the *same* payload shape Docmosis renders from, carrying the
*same* missing linkage to what actually prints on the page.

## 3. How a NOW document is actually assembled

### 3a. `distinctPrompts` is a curated allow-list, not "every prompt"

`NowContentMapper.getDistinctPrompts()` (`cpp-context-azure-legalaidagency`,
`Mapper/NowContent/NowContentMapper.js:224`) only includes prompt *types* the NOW-definition reference
data separately flags `distinctPromptTypes: true` for. Everything else a consumer sees under
`defendantResults[].prompts[]` or `results[].prompts[]` is invisible to `distinctPrompts`, and
therefore invisible to the template's `dp.*` helpers entirely.

### 3b. Deduplication is by value, discarding which result/case/offence it came from

`getDistinctPromptsValues()` (same file, line 367) dedups using `new Set(prompt.value)` — same
`promptReference` + same value collapses to one entry; same `promptReference` + *different* values
(e.g. two offences remanded to two different prisons) survive as two entries, neither carrying a
`caseId`/`offenceId`/`resultIdentifier` back-reference. A consumer cannot tell which case a given
`distinctPrompts` entry belongs to.

### 3c. `resultIdentifier` and `promptIdentifier` share one GUID pool, with no type tag

`resultPrompt.promptIdentifier = distinctPrompt.judicialResultPromptTypeId` (same file, line 251) —
`distinctPrompts[].promptIdentifier` is a **prompt-type** reference-data id, not a unique instance id.
It is drawn from the same GUID shape as `resultIdentifier` (a **result-type** reference-data id), with
nothing in either field distinguishing which kind of thing the GUID names.

This is not theoretical — it reproduces directly in the example payload given for materialId
`cdc70bcf-548a-4a8e-ab7a-4b534e42d7f5`: the defendant-level "Risk or vulnerability factors" result
carries `resultIdentifier: 66105417-41c8-420d-820f-40b61b507442`. The same GUID reappears, unrelated,
as a `promptIdentifier` inside a completely different case-level custody result's `prompts[]` array —
with a synthesised `promptReference` that is itself a raw UUID
(`3dd9db0e-4bbe-4743-b257-1842123ea947`) rather than a semantic name like every genuine CP prompt
reference (`prisonOrganisationName`, `timeWhenWrittenNoticeOfAppealWasReceived`, etc.), and a value
string (`"Risk or vulnerability factors:test"`) shaped differently from every sibling prompt's plain
value. A consumer building a lookup table keyed by these GUIDs cannot assume uniqueness across entity
kinds.

### 3d. Sentence-selection logic is baked into the template, not the data

`NowTextHelper.valueBasedOnPromptReference()` (`cpp-context-system-doc-generator`,
`service/NowTextHelper.java:64`) lets a template field choose between two different `nowText` entries
based on comparing a `distinctPrompts` value against a literal string, entirely inside the `.docx`
field code. 9 of the 40 templates use this or an equivalent conditional helper
(`valueMatches`/`labelIfValueMatches`/`valueIfValueMatches`) — see the census in §5. A consumer has no
way to know which branch fired without also holding and correctly interpreting the binary template.

### 3e. First-match-wins lookups silently drop repeats

`DistinctPromptsHelper.value()`/`.label()` and `NowTextHelper.value()`
(`cpp-context-system-doc-generator/service/DistinctPromptsHelper.java:48`,
`NowTextHelper.java:28`) all resolve with `.findFirst()`. If two results in the same NOW document
carry the same `promptReference` with *different* values that survived §3b's dedup, only the first one
the array happens to contain is ever rendered — the rest are structurally unreachable from the
template, but still visible (and misleading) to a consumer reading the full result data.

### 3f. Nested templating inside stored text

`DistinctResultsHelper.getNowRequirementTextValue()`
(`cpp-context-system-doc-generator/service/DistinctResultsHelper.java`) regex-scans a stored
`nowRequirementText` string for `<promptReference>` tokens and substitutes them at render time against
a specific `resultDefinitionId`. This is a second, independent templating layer living inside a text
*value*, invisible to anything reading the JSON structurally rather than parsing its string content.

### 3g. Literal reference-data GUIDs hardcoded directly in the template body

The clearest evidence. `WEE_RemandAfterBailAppealByProsecutorTemplate.docx`'s final section reads
(decoded from the raw template XML):

```
<<rr_distinctResults>>
  <<cr_{equalsIgnoreCase(resultIdentifier, '23d744a1-d71c-4849-b6a3-b736d71ea438')}>>
    <<label>>
    <<rs_prompts>><<value>><<es>>
  <<es_>>
<<er_>>
```

This iterates `distinctResults` and renders a result's label and raw prompt values **only if its
`resultIdentifier` exactly equals `23d744a1-d71c-4849-b6a3-b736d71ea438`**, a literal GUID hardcoded at
template-authoring time. Every other result a consumer sees in the structured payload — however many
`defendantResults`/case-offence `results` are present — is silently invisible to this section unless
it happens to carry that one specific GUID. Nothing in the JSON payload, nor in any reference-data
endpoint this API would expose, documents which GUID "matters" for which template.

This is not a one-off: `WEE_RemandTemplate.docx` hardcodes four literal result-identifier GUIDs in its
own body (one of them the same `23d744a1-...` GUID, implying it names a result type reused across
templates). See §5 for the full count across all 40.

## 4. Worked example — `WEE_RemandAfterBailAppealByProsecutor` (materialId `cdc70bcf-548a-4a8e-ab7a-4b534e42d7f5`)

The template's field-code sequence, extracted in document order from the raw `.docx` XML:

| # | Field code | Resolves against (this example) |
|---|---|---|
| 1 | `dp.value('conveyorcustodiannameOrganisationName')` | `Cardiff Magistrates' Court: PECS` |
| 2 | `nt.value('toText1')` | `and the Governor of` |
| 3 | `dp.value('prisonOrganisationName')` | `HMP/YOI Durham` |
| 4 | `nt.value('orderDate')` | `Date of warrant` *(a caption string, not a date — see below)* |
| 5 | `nt.value('warrantText1')` | `Written notice of appeal lodged at:` |
| 6 | `dp.value('timeWhenWrittenNoticeOfAppealWasReceived')` | `21:00` |
| 7 | `nt.value('warrantText2')` | `on` |
| 8 | `nt.value('warrantText3')` | `The appeal must be commenced at the Crown Court within 48 hours...` |
| 9 | `nt.value('warrantText4')` | `Basis of remand:` |
| 10 | `nt.value('warrantText5')` | `Pending appeal by prosecutor against grant of bail.` |
| 11 | `nt.value('orderText1')` | `The defendant is to be taken to the nominated prison establishment...` |
| 12 | `dp.value('crownCourtName')` | `Blackfriars Crown Court` |
| 13 | `nt.value('orderText2')` | `at a date and time to be notified.` |

Reconstructing the "To:" line alone: `To: Cardiff Magistrates' Court: PECS and the Governor of HMP/YOI
Durham`. That sentence glues two *different* prompts (`conveyorcustodiannameOrganisationName`,
`prisonOrganisationName`) with one static fragment (`toText1`) in a specific order — nothing in the
JSON says these two values belong in the same sentence, let alone in this order.

**A separate, genuine naming collision**: the `nowText` entry with `label: "orderDate"` holds the
*caption* `"Date of warrant"`, not a date. The real date is a different, top-level `orderDate` field
(`"2026-09-02"`), reformatted at render time via `dateFormat(...)`. A consumer reading
`nowText: [{label: "orderDate", value: "Date of warrant"}]` in isolation would reasonably assume
`value` is itself a date string; it is a caption.

**The `distinctResults` section renders nothing for this example** — the given payload has no
`distinctResults` array at all, so §3g's hardcoded-GUID section is simply empty for this hearing. This
is expected per the template's own logic, not a defect in the example payload.

**Case/offence content is the one part that does correlate properly**: `<<rr_cases>>` genuinely
iterates the payload's `cases[]` array, printing each `reference`, comma-joined `caseMarkers`, and each
linked offence's `title`/`wording` — worth noting for balance; not everything in every template is
broken in the same way as §3c–§3g.

## 5. Census across all 40 HRDS-registered event types

Counted directly from each template's `.docx` body (field-code occurrences), matched against HRDS's
`event_type` table (`V1.009__restore_event_type_data.sql`, ids 2–41):

- **39 of 40** templates call `dp.*`/`nt.*`/`dr.*` lookup-by-string helpers at all. The one exception,
  `WEE_Layout5` (id 2, "Warrant Supplement"), appears to be a static continuation/cover-sheet layout
  with no dynamic content.
- **9 of 40** templates use conditional branching logic
  (`valueBasedOnPromptReference`/`valueMatches`/`labelIfValueMatches`/`valueIfValueMatches`) embedded
  in the template — §3d's problem.
- **12 of 40** templates hardcode at least one literal result-identifier GUID directly in the template
  body — §3g's problem. `WEE_Remand` hardcodes four.
- Field-code usage per template ranges from 2 calls (`NEE_DetentionOnRecommendationForDeportation`) to
  50 (`WXE_RemandWarrantYouthDetentionAccommodation`) — this is not a small-template-only phenomenon.

One naming-drift finding, unrelated to the correlation problem but worth flagging: HRDS registers
event id 23 as `WEE_CustodyWarrantSendingToSecretaryOfStateOnConsentCategory2Territory`, but the actual
template file is `WEE_GenerateCustodyWarrantSendingToSecretaryOfStateOnConsentCategory2TerritoryTemplate.docx`
— an extra `Generate` in the filename the registered `event_name` doesn't have. Whatever resolves
`templateName` to a file today evidently copes with this (or the mismatch is dormant), but it means a
literal `event_name`-to-filename string match would fail for this one event type.

## 6. What NOWs actually sends to HRDS today

### 6a. HRDS's own contract only types a small identity/routing projection

`EventPayload` (`api-cp-crime-hearing-results-document-subscription/openapi-spec.yml:764`) only
requires and types: `eventId`, `materialId`, `hearingId`, `eventType`, `timestamp`, and
`defendant.{masterDefendantId, name, dateOfBirth, custodyEstablishmentDetails.emailAddress, cases[].urn}`.
Every other field — results, prompts, `nowText`, `distinctPrompts`, order/court metadata, prosecutor
detail, case-level PII — has no declared field at all. It can only travel through a single optional,
untyped `payload: {additionalProperties: true}` blob whose shape the spec does not document.

### 6b. The current mapper doesn't populate even that optional blob

`HRDSNotificationMapper.mapNowsForHRDSNotification()`
(`cpp-context-hearing-nows/hearingnows-event-processor/.../hrds/mappers/HRDSNotificationMapper.java:32`)
builds the outbound `HRDSEventPayload` from exactly: `eventType` (← `templateName`), `eventId` (←
`requestId`, random if absent), `hearingId`, `materialId`, `timestamp` (← message metadata), and
`defendant.{masterDefendantId, name, dateOfBirth, custodyEstablishmentDetails.emailAddress (← nowContent.orderAddressee.address.emailAddress1), cases[].urn (← nowContent.cases[].reference)}`.

**It never sets the optional `payload` field.** Today, zero result/prompt/order/court content reaches
HRDS — only a routing envelope plus defendant identity, one email address, and case URNs.

### 6c. The concrete payload for materialId `cdc70bcf-548a-4a8e-ab7a-4b534e42d7f5`

Applying the mapper above to the given `now.json` (`caseURN RC363968376`,
`hearingId 6988027f-e786-49f4-a00f-7c35ab459464`):

```json
{
  "eventType": "WEE_RemandAfterBailAppealByProsecutor",
  "eventId": "<random UUID — not derivable from this payload; sourced from requestId if present on the originating NowDocumentReq>",
  "hearingId": "6988027f-e786-49f4-a00f-7c35ab459464",
  "materialId": "cdc70bcf-548a-4a8e-ab7a-4b534e42d7f5",
  "timestamp": "<sourced from separate message metadata.createdAt, not present in this payload>",
  "defendant": {
    "masterDefendantId": "d2151771-41a1-42e1-af36-a99d9b39c0b2",
    "name": "Lacy Braun",
    "dateOfBirth": "1998-09-02",
    "custodyEstablishmentDetails": {
      "emailAddress": "OMU.Durham@justice.gov.uk"
    },
    "cases": [
      { "urn": "RC363968376" }
    ]
  }
}
```

Note `defendant.masterDefendantId` is sourced from the top-level `NowDocumentReq.masterDefendantId`
(`d2151771-...`), not any field inside `nowContent.defendant` — `nowContent.defendant` has no id field
of its own. Note also `defendant.cases[].urn` is sourced from `nowContent.cases[].reference`
(`"RC363968376"`), not the top-level `cases` array of internal case UUIDs
(`["e7e3f7b6-4b03-4364-b6e5-29817151af42"]`) — the payload carries two different "case identifier"
shapes and the mapper picks the human-readable one.

Everything else in the ~250-line source payload — the defendant's title/address/`defendantResults[]`,
every case's prosecutor/offence/judicial-result/prompt detail, `nowText[]`, `distinctPrompts[]`,
`orderName`/`orderDate`/`courtClerkName`, `orderingCourt` — is dropped before it reaches HRDS.

## 7. Consequence for the NOWs API Marketplace channel

Bringing NOW content and PII into this service's scope — a confirmed direction, not covered by this
document — would let a subscriber read the same content categories the PDF renders from
(`defendantResults`, case/offence/result/prompt detail, `nowText`, etc.), not just an eligibility flag.
§3–§6 show that reading that content does not let a
consumer reconstruct *what the PDF actually displays*: which prompt values appear in which sentence,
in what order, under what condition, or — for 12 of 40 templates — which single result out of several
the document renders at all.

Two distinct claims this API can and cannot support once content is added:

- **Can support**: "here are the judicial results, prompts, and case/order facts CP recorded for this
  hearing" — the raw structured data, as recorded.
- **Cannot currently support**: "here is what the generated NOW document says, in the order it says
  it" — that mapping exists only inside binary Docmosis templates this API has no reason to expose,
  and in several cases depends on hardcoded reference-data GUIDs with no documented meaning outside the
  template that hardcodes them.

A consumer who assumes API field presence implies PDF presence (or vice versa) will build logic that
silently drifts from what the document generator actually renders — the same class of risk
golden-master drift-detection is meant to catch, but here it applies to the shape of individual
fields, not just whether a document was generated at all.

The generation gate this service already scopes to answers "would a NOW have been generated." Nothing
in this service's current or planned scope answers "what would it have said" — and per §3, that
question may not be answerable from structured data alone without also exposing (or re-deriving) the
template's own field-selection and sentence-assembly logic.

## 8. Open questions

Not decisions — flagging for whoever scopes the content-exposure work:

- Should the NOWs API response carry an explicit disclaimer that field presence/order in the response
  does not represent the rendered document's presence/order? Silence here is what lets a consumer
  assume a correlation that doesn't exist.
- Is there an appetite to add the missing linkage (a documented result→prompt→rendered-text mapping,
  or at minimum a scoped, versioned list of the hardcoded GUIDs each template depends on) as a
  separate piece of reference data this API could expose? Out of scope to answer here — the finding is
  that today, nothing does this.
- If a future phase extends the HRDS callback's optional `payload` blob to carry more than the current
  identity/routing projection (§6), the same ID-collision (§3c) and ordering-invisibility (§3d–§3g)
  risks carry over unchanged — worth flagging to whoever picks that up, not just to this service's own
  read API.
