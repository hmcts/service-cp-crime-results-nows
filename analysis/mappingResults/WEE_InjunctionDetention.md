# WEE InjunctionDetention

- **Template ID:** `WEE_InjunctionDetention`
- **Source file:** `analysis/templates/WEE_InjunctionDetentionTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 24

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `applicants.name` | field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `caseApplicationReferences[0]` | field | Hearing-resulted event | prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) / courtApplication.applicationReference | Yes | prosecutionCase.caseURN (or courtApplications[].reference) |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.pncId` | field | Hearing-resulted event | defendant.pncId | No | no pncId field on Defendant in this schema |
| `defendantToBeDetainedInYouthDetentionAccommodationForAPeriodOf` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "defendantToBeDetainedInYouthDetentionAccommodationForAPeriodOf"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "defendantToBeDetainedInYouthDetentionAccommodationForAPeriodOf", then read .value |
| `dr.resultPromptValue(‘courtContactEmailAddressSPOC’,’6d65a5b5-46c3-4c79-a217-d338f4c8d223’)` | helper:dr | Judicial result prompt | the judicial result whose judicialResultTypeId = ? — one of its judicialResultPrompts, matched by promptReference | No | resultTexts[] has no judicialResultTypeId/resultDefinitionId to key against — the UUID this call needs isn't in the schema |
| `noOtherPowerIsAvailableToTheCourtBecause` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "noOtherPowerIsAvailableToTheCourtBecause"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "noOtherPowerIsAvailableToTheCourtBecause", then read .value |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `respondents.address.line1` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.address.line2` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.address.line3` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.address.line4` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.address.line5` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.address.postCode` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents.name` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
