# WEE ExtraditionRemandAfterBailAppealByProsecutor

- **Template ID:** `WEE_ExtraditionRemandAfterBailAppealByProsecutor`
- **Source file:** `analysis/templates/WEE_ExtraditionRemandAfterBailAppealByProsecutorTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 22

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `caseApplicationReferences[0]` | field | Hearing-resulted event | prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) / courtApplication.applicationReference | Yes | prosecutionCase.caseURN (or courtApplications[].reference) |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `defendant.address.line1` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line2` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line3` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line4` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line5` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.postCode` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `defendant.nationality` | field | Hearing-resulted event | personDefendant.personDetails.nationalityDescription | Yes | defendant.nationality (present, but null in the sample payload) |
| `defendant.pncId` | field | Hearing-resulted event | defendant.pncId | No | no pncId field on Defendant in this schema |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `prisonOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "prisonOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "prisonOrganisationName", then read .value |
| `prosecutorsNoticeOfAppealReceivedAt` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "prosecutorsNoticeOfAppealReceivedAt"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "prosecutorsNoticeOfAppealReceivedAt", then read .value |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
