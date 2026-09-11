# WEE CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurder

- **Template ID:** `WEE_CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurder`
- **Source file:** `analysis/templates/WEE_CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurderTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 30

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `convictionStatus` | field | Hearing-resulted event | derived: offence.convictionDate present → "Convicted"/"Not Convicted" | Yes | derivable: offences[].convictionDate present → "Convicted"/"Not Convicted" |
| `dateToBeFixed` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "dateToBeFixed"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "dateToBeFixed", then read .value |
| `defendant.address.line1` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line2` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line3` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line4` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line5` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.postCode` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `defendant.nationality` | field | Hearing-resulted event | personDefendant.personDetails.nationalityDescription | Yes | defendant.nationality (present, but null in the sample payload) |
| `fixedDate` | prompt (dateValue) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "fixedDate"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "fixedDate", then read .value |
| `hCHOUSEOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "hCHOUSEOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "hCHOUSEOrganisationName", then read .value |
| `isApplication` | field | Hearing-resulted event | Case.isApplication — true when the case is a court application, not a prosecution case | Yes | derivable: entry came from courtApplications[] rather than offences[]/prosecutionCase |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `prisonOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "prisonOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "prisonOrganisationName", then read .value |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `riskOrVulnerabilityFactors` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "riskOrVulnerabilityFactors"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "riskOrVulnerabilityFactors", then read .value |
| `theDefendantIsExcusedAtTheHearingUnlessAttendanceIsOrderedByTheCrownCourt` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "theDefendantIsExcusedAtTheHearingUnlessAttendanceIsOrderedByTheCrownCourt"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "theDefendantIsExcusedAtTheHearingUnlessAttendanceIsOrderedByTheCrownCourt", then read .value |
| `theDefendantIsToBeProducedForTheHearingAtTheCrownCourt` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "theDefendantIsToBeProducedForTheHearingAtTheCrownCourt"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "theDefendantIsToBeProducedForTheHearingAtTheCrownCourt", then read .value |
| `timeOfHearing` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "timeOfHearing"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "timeOfHearing", then read .value |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `weekCommencing` | prompt (dateValue) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "weekCommencing"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "weekCommencing", then read .value |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
