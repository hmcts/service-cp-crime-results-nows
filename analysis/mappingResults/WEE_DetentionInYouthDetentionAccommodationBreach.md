# WEE DetentionInYouthDetentionAccommodationBreach

- **Template ID:** `WEE_DetentionInYouthDetentionAccommodationBreach`
- **Source file:** `analysis/templates/WEE_DetentionInYouthDetentionAccommodationBreachTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 31

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentDate` | date field | Hearing-resulted event | built by the NowContent mapper layer (cpp-context-azure-legalaidagency) from hearing/court-centre/offence data — exact path not traced field-by-field | Partial | not individually verified against this schema — likely resolvable if it names a defendant/offence/court field already listed as available |
| `caseApplicationReferences[0]` | field | Hearing-resulted event | prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) / courtApplication.applicationReference | Yes | prosecutionCase.caseURN (or courtApplications[].reference) |
| `concurrent` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "concurrent"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "concurrent", then read .value |
| `consecutiveToOffenceNumber` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "consecutiveToOffenceNumber"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "consecutiveToOffenceNumber", then read .value |
| `consecutiveToSentenceImposedOn` | prompt (dateValue) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "consecutiveToSentenceImposedOn"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "consecutiveToSentenceImposedOn", then read .value |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `defendant.address.line1` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line2` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line3` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line4` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line5` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.postCode` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `defendant.pncId` | field | Hearing-resulted event | defendant.pncId | No | no pncId field on Defendant in this schema |
| `designatedLocalAuthority` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "designatedLocalAuthority"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "designatedLocalAuthority", then read .value |
| `detentionPeriod` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "detentionPeriod"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "detentionPeriod", then read .value |
| `isApplication` | field | Hearing-resulted event | Case.isApplication — true when the case is a court application, not a prosecution case | Yes | derivable: entry came from courtApplications[] rather than offences[]/prosecutionCase |
| `label` | derived field | Hearing-resulted event | same judicial-result/prompt object currently in scope (passed through a template function, e.g. substring/dateFormat) | Partial | depends entirely on whether the field it wraps is available (see that field's row) |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `riskOrVulnerabilityFactors` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "riskOrVulnerabilityFactors"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "riskOrVulnerabilityFactors", then read .value |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `value` | field | Hearing-resulted event | built by the NowContent mapper layer (cpp-context-azure-legalaidagency) from hearing/court-centre/offence data — exact path not traced field-by-field | Partial | not individually verified against this schema — likely resolvable if it names a defendant/offence/court field already listed as available |
| `whereConsecutiveToAnOffenceOnAnotherCaseSpecifyThatCaseNumber` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "whereConsecutiveToAnOffenceOnAnotherCaseSpecifyThatCaseNumber"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "whereConsecutiveToAnOffenceOnAnotherCaseSpecifyThatCaseNumber", then read .value |
| `whichWasImpBy` | prompt (label) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "whichWasImpBy"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "whichWasImpBy", then read .value |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
