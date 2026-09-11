# OBE ChangeOfFootballBanning

- **Template ID:** `OBE_ChangeOfFootballBanning`
- **Source file:** `analysis/templates/OBE_ChangeOfFootballBanningTemplate.docx`
- **Type:** Order
- **Delivery:** Both
- **Language:** English
- **Code:** `OBE`
- **Field count:** 36

## Merge fields

| Field | Category | Source kind | Hearing-results source | API Response available | API Response note |
|---|---|---|---|---|---|
| `amendmentdate` | date field | Hearing-resulted event | derived: the matched judicial result's amendmentDate | No | no amendment concept in this schema |
| `applicants[0].address.line1` | field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].address.line2` | optional field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].address.line3` | optional field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].address.line4` | optional field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].address.line5` | optional field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].address.postCode` | field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].dateOfBirth` | date field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `applicants[0].name` | field | Hearing-resulted event | courtApplications[].applicant — same shape as defendant, via ApplicantMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `caseApplicationReferences[0]` | field | Hearing-resulted event | prosecutionCaseIdentifier.caseURN (or .prosecutionAuthorityReference) / courtApplication.applicationReference | Yes | prosecutionCase.caseURN (or courtApplications[].reference) |
| `dateOriginalOrderMade` | prompt (dateValue) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "dateOriginalOrderMade"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "dateOriginalOrderMade", then read .value |
| `defendant.name` | field | Hearing-resulted event | personDefendant.personDetails.firstName + lastName (or legalEntityDefendant.organisation.name) | Yes | defendant.title + firstName + middleName + lastName |
| `detailsOfVariation` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "detailsOfVariation"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "detailsOfVariation", then read .value |
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
| `originalCourt` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "originalCourt"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "originalCourt", then read .value |
| `prisonOrganisationName` | prompt (value) | Judicial result prompt | judicialResultPrompts[promptReference ≈ "prisonOrganisationName"].value / .label (matched case-insensitively) | Partial | no stable promptReference key exists here — would have to fuzzy-match offences[].results[].resultTexts[].label against a hardcoded display string for "prisonOrganisationName", then read .value |
| `respondents.dateOfBirth` | date field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.line1` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.line2` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.line3` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.line4` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.line5` | optional field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].address.postCode` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].dateOfBirth` | date field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
| `respondents[0].name` | field | Hearing-resulted event | courtApplications[].respondents[] — same shape as defendant, via RespondentMapper | No | courtApplications[] has no applicant/respondent party sub-object in this schema |
