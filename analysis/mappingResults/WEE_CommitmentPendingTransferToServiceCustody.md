# WEE CommitmentPendingTransferToServiceCustody

- **Template ID:** `WEE_CommitmentPendingTransferToServiceCustody`
- **Source file:** `analysis/templates/WEE_CommitmentPendingTransferToServiceCustodyTemplate.docx`
- **Type:** Warrant
- **Delivery:** Email
- **Language:** English
- **Code:** `WEE`
- **Field count:** 24

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentDate` | date field | Hearing-resulted event | built by the NowContent mapper layer (cpp-context-azure-legalaidagency) from hearing/court-centre/offence data — exact path not traced field-by-field | Partial | not individually verified against this schema — likely resolvable if it names a defendant/offence/court field already listed as available |
| `committedToDetentionPendingTheTransferByTheConstablesOfTheFollowingPoliceForce` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "committedToDetentionPendingTheTransferByTheConstablesOfTheFollowingPoliceForce"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "committedToDetentionPendingTheTransferByTheConstablesOfTheFollowingPoliceForce", then read .value |
| `committedtodetentionpendingthetransferbytheconstablesofthefollowingprisonOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "committedtodetentionpendingthetransferbytheconstablesofthefollowingprisonOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "committedtodetentionpendingthetransferbytheconstablesofthefollowingprisonOrganisationName", then read .value |
| `conveyorcustodiannameOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "conveyorcustodiannameOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "conveyorcustodiannameOrganisationName", then read .value |
| `defendant.address.line1` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line2` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line3` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line4` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.line5` | optional field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.address.postCode` | field | Hearing-resulted event | personDefendant.personDetails.address.address{1-5}/postcode | Yes | defendant.address.address{1-5} / .postCode |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `defendant.prosecutingAuthorityReference` | field | Hearing-resulted event | personDefendant.arrestSummonsNumber (joined across cases) | No | no arrest/summons reference on Defendant in this schema |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `rankRate` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "rankRate"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "rankRate", then read .value |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `serviceNumber` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "serviceNumber"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "serviceNumber", then read .value |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `unitOrShip` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "unitOrShip"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "unitOrShip", then read .value |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
