# WEE CustodialSentence

- **Template ID:** `WEE_CustodialSentence`
- **Source file:** `analysis/templates/WEE_CustodialSentenceTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 44

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `bailRemandDaysToCountTaggedDays` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "bailRemandDaysToCountTaggedDays"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "bailRemandDaysToCountTaggedDays", then read .value |
| `caseApplicationReferences[0]` | field | Hearing-resulted event | prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) / courtApplication.applicationReference | Yes | prosecutionCase.caseURN (or courtApplications[].reference) |
| `consecutiveToAllOtherTermsBeingServed` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "consecutiveToAllOtherTermsBeingServed"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "consecutiveToAllOtherTermsBeingServed", then read .value |
| `consecutiveToSentenceImposedOn` | prompt (dateValue) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "consecutiveToSentenceImposedOn"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "consecutiveToSentenceImposedOn", then read .value |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `convictionDate` | date field | Hearing-resulted event | offence.convictionDate | Yes | offences[].convictionDate |
| `count` | field | Hearing-resulted event | offence.count | No | no offence count/sequence field in this schema |
| `defendant.address.line1` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line2` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line3` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line4` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line5` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.postCode` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.landlineNumber` | field | Hearing-resulted event | personDefendant.personDetails.contact.home / .mobile | No | no contact numbers on Defendant in this schema |
| `defendant.mobileNumber` | field | Hearing-resulted event | personDefendant.personDetails.contact.home / .mobile | No | no contact numbers on Defendant in this schema |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `defendant.nationalInsuranceNumber` | field | Hearing-resulted event | personDefendant.personDetails.nationalInsuranceNumber | No | no NINO field on Defendant in this schema |
| `defendant.nationality` | field | Hearing-resulted event | personDefendant.personDetails.nationalityDescription | Yes | defendant.nationality (present, but null in the sample payload) |
| `defendant.occupation` | field | Hearing-resulted event | personDefendant.personDetails.occupation | No | no occupation field on Defendant in this schema |
| `defendant.pncId` | field | Hearing-resulted event | defendant.pncId | No | no pncId field on Defendant in this schema |
| `defendant.prosecutingAuthorityReference` | field | Hearing-resulted event | personDefendant.arrestSummonsNumber (joined across cases) | No | no arrest/summons reference on Defendant in this schema |
| `defendant.selfDefinedEthnicity` | field | Hearing-resulted event | personDefendant.personDetails.ethnicity.selfDefinedEthnicityDescription | No | no ethnicity field on Defendant in this schema |
| `dr.resultPromptValue('reasons',‘23d744a1-d71c-4849-b6a3-b736d71ea438’)` | helper:dr | Judicial result prompt | the judicial result whose judicialResultTypeId = 23d744a1-d71c-4849-b6a3-b736d71ea438 — one of its judicialResultPrompts, matched by promptReference | No | resultTexts[] has no judicialResultTypeId/resultDefinitionId to key against — the UUID this call needs isn't in the schema |
| `earlyReleaseProvisionsApply` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "earlyReleaseProvisionsApply"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "earlyReleaseProvisionsApply", then read .value |
| `earlyReleaseProvisionsDoNotApply` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "earlyReleaseProvisionsDoNotApply"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "earlyReleaseProvisionsDoNotApply", then read .value |
| `extLic` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "extLic"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "extLic", then read .value |
| `minimumTerm` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "minimumTerm"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "minimumTerm", then read .value |
| `numberOfDaysInCustodyInForeignJurisdictionToCount` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "numberOfDaysInCustodyInForeignJurisdictionToCount"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "numberOfDaysInCustodyInForeignJurisdictionToCount", then read .value |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderIndex` | field | Hearing-resulted event | offence.orderIndex | No | no offence ordering index field in this schema |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `prisonOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "prisonOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "prisonOrganisationName", then read .value |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `resultText` | loop-item field | Hearing-resulted event | judicialResult.resultText | Yes | offences[].results[].resultDescription (whole-result summary, not a per-prompt text) |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `totalCustodialPeriod` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "totalCustodialPeriod"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "totalCustodialPeriod", then read .value |
| `totalCustodialPeriodIsLife` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "totalCustodialPeriodIsLife"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "totalCustodialPeriodIsLife", then read .value |
| `whichWasImpBy` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "whichWasImpBy"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "whichWasImpBy", then read .value |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
