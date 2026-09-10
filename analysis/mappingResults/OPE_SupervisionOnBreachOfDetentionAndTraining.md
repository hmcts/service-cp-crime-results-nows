# OPE SupervisionOnBreachOfDetentionAndTraining

- **Template ID:** `OPE_SupervisionOnBreachOfDetentionAndTraining`
- **Source file:** `analysis/templates/OPE_SupervisionOnBreachOfDetentionAndTrainingTemplate.docx`
- **Type:** Order
- **Delivery:** Post
- **Language:** English
- **Code:** `OPE`
- **Field count:** 25

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `defendant.dateOfBirth` | date field | Hearing-resulted event | personDefendant.personDetails.dateOfBirth | Yes | defendant.dateOfBirth |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `furtherSupervisionPeriod` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "furtherSupervisionPeriod"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "furtherSupervisionPeriod", then read .value |
| `nt.value(‘orderDate’)` | helper:nt | NOW definition catalogue | NOW definition catalogue's static nowTextList / nowRequirementText — not from the hearing-resulted event (though it can interpolate a prompt value inline) | No | NOW catalogue static text — outside any hearing/case API's scope |
| `nt.value(‘orderText3’)` | helper:nt | NOW definition catalogue | NOW definition catalogue's static nowTextList / nowRequirementText — not from the hearing-resulted event (though it can interpolate a prompt value inline) | No | NOW catalogue static text — outside any hearing/case API's scope |
| `nt.value(’orderText1’)` | helper:nt | NOW definition catalogue | NOW definition catalogue's static nowTextList / nowRequirementText — not from the hearing-resulted event (though it can interpolate a prompt value inline) | No | NOW catalogue static text — outside any hearing/case API's scope |
| `nt.value(’orderText2’)` | helper:nt | NOW definition catalogue | NOW definition catalogue's static nowTextList / nowRequirementText — not from the hearing-resulted event (though it can interpolate a prompt value inline) | No | NOW catalogue static text — outside any hearing/case API's scope |
| `orderAddressee.address.line1` | field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.address.line2` | optional field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.address.line3` | optional field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.address.line4` | optional field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.address.line5` | optional field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.address.postCode` | field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderAddressee.name` | field | Hearing-resulted event | subscription-matched recipient (defendant / prison / third party) via OrderAddresseeMapper | No | no recipient/addressee concept in this schema |
| `orderDate` | date field | Hearing-resulted event | derived: the ordered judicial result's orderedDate | No | no order/result date field — closest are hearing.hearingDate / offences[].convictionDate / .pleaDate |
| `orderingCourt.courtCentreName` | field | Hearing-resulted event | resolved court-house lookup (courtCentre.name / convictingCourt.name) | Yes | hearing.courtDetails.court.courtHouseName |
| `orderingCourt.ljaCode` | field | Hearing-resulted event | resolved court-house lookup (courtCentre / offence.convictingCourt).lja.ljaCode | No | Court carries courtHouseId/Code/Name only — no separate LJA code field |
| `orderingCourt.ljaName` | field | Hearing-resulted event | courtCentre.lja.ljaName | Yes | hearing.courtDetails.ljaName |
| `orderName` | field | NOW definition catalogue | NOW definition catalogue's own `now.name` — static per document type | No | NOW catalogue metadata — not part of any hearing/case API response |
| `reference` | field | Hearing-resulted event | offence's case: prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) | Yes | prosecutionCase.caseURN |
| `Supervisor` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "Supervisor"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "Supervisor", then read .value |
| `title` | field | Hearing-resulted event | offence.offenceTitle | Yes | offences[].title |
| `value` | field | Hearing-resulted event | built by the NowContent mapper layer (cpp-context-azure-legalaidagency) from hearing/court-centre/offence data — exact path not traced field-by-field | Partial | not individually verified against this schema — likely resolvable if it names a defendant/offence/court field already listed as available |
| `wording` | field | Hearing-resulted event | offence.wording + "\n" + offence.offenceLegislation | Yes | offences[].wording + "\n" + offences[].offenceLegislation (kept as two separate fields here) |
