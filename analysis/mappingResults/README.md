# NOWS Merge Field Register — mapping results

Source: Docmosis merge fields extracted from every custody, warrant & detention template in `templates/nows/`, mapped back to where each one is sourced from the hearing-resulted event.

- Templates: 40
- Distinct fields (across all templates): 226

## Pipeline

1. **Hearing resulted** — `judicialResults[]`, `judicialResultPrompts[]`: a hearing's results land on the defendant/offence, each carrying a `promptReference`-keyed prompts array.
2. **NOW content mapper** (`cpp-context-azure-legalaidagency`) — `NowDefendantMapper` / `OrderCourtMapper` / `CaseMapper` build defendant, orderingCourt & case fields; `distinctResults` / `distinctPrompts` carry the results through untouched.
3. **Docmosis render** — `DistinctPromptsHelper` (dp) / `DistinctResultsHelper` (dr): template tags look prompts up by the same `promptReference` string at render time — that's the field name you see in this register.

## Templates

| Template | Type | Delivery | Language | Code | Fields | File |
|---|---|---|---|---|---|---|
| NEE DetentionOnRecommendationForDeportation | Notice | Email | English | `NEE` | 20 | [NEE_DetentionOnRecommendationForDeportation.md](./NEE_DetentionOnRecommendationForDeportation.md) |
| NEE FootballBanning | Notice | Email | English | `NEE` | 38 | [NEE_FootballBanning.md](./NEE_FootballBanning.md) |
| OBE ChangeOfFootballBanning | Order | Both | English | `OBE` | 36 | [OBE_ChangeOfFootballBanning.md](./OBE_ChangeOfFootballBanning.md) |
| OBE TerminationOfFootballBanning | Order | Both | English | `OBE` | 34 | [OBE_TerminationOfFootballBanning.md](./OBE_TerminationOfFootballBanning.md) |
| OEE BailAppealEndOfCustody | Order | Email | English | `OEE` | 17 | [OEE_BailAppealEndOfCustody.md](./OEE_BailAppealEndOfCustody.md) |
| OEE MedicalRemandAdditionalDetails | Order | Email | English | `OEE` | 26 | [OEE_MedicalRemandAdditionalDetails.md](./OEE_MedicalRemandAdditionalDetails.md) |
| OPE SupervisionOnBreachOfDetentionAndTraining | Order | Post | English | `OPE` | 25 | [OPE_SupervisionOnBreachOfDetentionAndTraining.md](./OPE_SupervisionOnBreachOfDetentionAndTraining.md) |
| OXE DetentionAndTraining | Order | File storage | English | `OXE` | 23 | [OXE_DetentionAndTraining.md](./OXE_DetentionAndTraining.md) |
| WEE CommitmentPendingTransferToServiceCustody | Warrant | Email | English | `WEE` | 24 | [WEE_CommitmentPendingTransferToServiceCustody.md](./WEE_CommitmentPendingTransferToServiceCustody.md) |
| WEE CommittalToCrownCourtAuthorityToHoldInYouthDetentionAccommodation | Warrant | Email | English | `WEE` | 34 | [WEE_CommittalToCrownCourtAuthorityToHoldInYouthDetentionAccommodation.md](./WEE_CommittalToCrownCourtAuthorityToHoldInYouthDetentionAccommodation.md) |
| WEE CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurder | Warrant | Email | English | `WEE` | 30 | [WEE_CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurder.md](./WEE_CommittalToCrownCourtForConsiderationOfTheQuestionOfBailOnAChargeOfMurder.md) |
| WEE CommittalToCrownCourtForSentence | Warrant | Email | English | `WEE` | 33 | [WEE_CommittalToCrownCourtForSentence.md](./WEE_CommittalToCrownCourtForSentence.md) |
| WEE CustodialSentence | Warrant | Email | English | `WEE` | 44 | [WEE_CustodialSentence.md](./WEE_CustodialSentence.md) |
| WEE CustodialSentenceWitness | Warrant | Email | English | `WEE` | 29 | [WEE_CustodialSentenceWitness.md](./WEE_CustodialSentenceWitness.md) |
| WEE CustodyWarrantOnDischargeOfExtraditionPendingAppeal | Warrant | Email | English | `WEE` | 27 | [WEE_CustodyWarrantOnDischargeOfExtraditionPendingAppeal.md](./WEE_CustodyWarrantOnDischargeOfExtraditionPendingAppeal.md) |
| WEE CustodyWarrantOnDischargeOfExtraditionPendingAppealPart2 | Warrant | Email | English | `WEE` | 26 | [WEE_CustodyWarrantOnDischargeOfExtraditionPendingAppealPart2.md](./WEE_CustodyWarrantOnDischargeOfExtraditionPendingAppealPart2.md) |
| WEE CustodyWarrantOnExtradition | Warrant | Email | English | `WEE` | 29 | [WEE_CustodyWarrantOnExtradition.md](./WEE_CustodyWarrantOnExtradition.md) |
| WEE CustodyWarrantOnExtraditionCategory2Territory | Warrant | Email | English | `WEE` | 30 | [WEE_CustodyWarrantOnExtraditionCategory2Territory.md](./WEE_CustodyWarrantOnExtraditionCategory2Territory.md) |
| WEE CustodyWarrantOnExtraditionWithBailDirection | Warrant | Email | English | `WEE` | 30 | [WEE_CustodyWarrantOnExtraditionWithBailDirection.md](./WEE_CustodyWarrantOnExtraditionWithBailDirection.md) |
| WEE CustodyWarrantOnExtraditionWithBailDirectionCategory2Territory | Warrant | Email | English | `WEE` | 31 | [WEE_CustodyWarrantOnExtraditionWithBailDirectionCategory2Territory.md](./WEE_CustodyWarrantOnExtraditionWithBailDirectionCategory2Territory.md) |
| WEE CustodyWarrantOnExtraditionWithBailDirectionWithConsent | Warrant | Email | English | `WEE` | 33 | [WEE_CustodyWarrantOnExtraditionWithBailDirectionWithConsent.md](./WEE_CustodyWarrantOnExtraditionWithBailDirectionWithConsent.md) |
| WEE CustodyWarrantOnExtraditionWithConsent | Warrant | Email | English | `WEE` | 32 | [WEE_CustodyWarrantOnExtraditionWithConsent.md](./WEE_CustodyWarrantOnExtraditionWithConsent.md) |
| WEE CustodyWarrantSendingToSecretaryOfStateCategory2Territory | Warrant | Email | English | `WEE` | 27 | [WEE_CustodyWarrantSendingToSecretaryOfStateCategory2Territory.md](./WEE_CustodyWarrantSendingToSecretaryOfStateCategory2Territory.md) |
| WEE CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppeal | Warrant | Email | English | `WEE` | 27 | [WEE_CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppeal.md](./WEE_CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppeal.md) |
| WEE CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppealPart2 | Warrant | Email | English | `WEE` | 26 | [WEE_CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppealPart2.md](./WEE_CustodyWarrantWithBailDirectionOnDischargeOfExtraditionPendingAppealPart2.md) |
| WEE CustodyWarrantWithBailDirectionSendingToSecretaryOfStateCategory2Territory | Warrant | Email | English | `WEE` | 28 | [WEE_CustodyWarrantWithBailDirectionSendingToSecretaryOfStateCategory2Territory.md](./WEE_CustodyWarrantWithBailDirectionSendingToSecretaryOfStateCategory2Territory.md) |
| WEE CustodyWarrantWithBailDirectionSendingToSecretaryOfStateOnConsentCategory2Territory | Warrant | Email | English | `WEE` | 28 | [WEE_CustodyWarrantWithBailDirectionSendingToSecretaryOfStateOnConsentCategory2Territory.md](./WEE_CustodyWarrantWithBailDirectionSendingToSecretaryOfStateOnConsentCategory2Territory.md) |
| WEE Detention | Warrant | Email | English | `WEE` | 24 | [WEE_Detention.md](./WEE_Detention.md) |
| WEE DetentionInYouthDetentionAccommodationBreach | Warrant | Email | English | `WEE` | 31 | [WEE_DetentionInYouthDetentionAccommodationBreach.md](./WEE_DetentionInYouthDetentionAccommodationBreach.md) |
| WEE ExtraditionRemandAfterBailAppealByProsecutor | Warrant | Email | English | `WEE` | 22 | [WEE_ExtraditionRemandAfterBailAppealByProsecutor.md](./WEE_ExtraditionRemandAfterBailAppealByProsecutor.md) |
| WEE ExtraditionSupplementToCustodyWarrant | Warrant | Email | English | `WEE` | 21 | [WEE_ExtraditionSupplementToCustodyWarrant.md](./WEE_ExtraditionSupplementToCustodyWarrant.md) |
| WEE GenerateCustodyWarrantSendingToSecretaryOfStateOnConsentCategory2Territory | Warrant | Email | English | `WEE` | 27 | [WEE_GenerateCustodyWarrantSendingToSecretaryOfStateOnConsentCategory2Territory.md](./WEE_GenerateCustodyWarrantSendingToSecretaryOfStateOnConsentCategory2Territory.md) |
| WEE InjunctionDetention | Warrant | Email | English | `WEE` | 24 | [WEE_InjunctionDetention.md](./WEE_InjunctionDetention.md) |
| WEE Layout5 | Warrant | Email | English | `WEE` | 21 | [WEE_Layout5.md](./WEE_Layout5.md) |
| WEE NonPaymentOfMoneyOwedCivilDebt | Warrant | Email | English | `WEE` | 36 | [WEE_NonPaymentOfMoneyOwedCivilDebt.md](./WEE_NonPaymentOfMoneyOwedCivilDebt.md) |
| WEE Remand | Warrant | Email | English | `WEE` | 58 | [WEE_Remand.md](./WEE_Remand.md) |
| WEE RemandAfterBailAppealByProsecutor | Warrant | Email | English | `WEE` | 36 | [WEE_RemandAfterBailAppealByProsecutor.md](./WEE_RemandAfterBailAppealByProsecutor.md) |
| WEE SendingToCrownCourtAuthorityToHoldInYouthDetentionAccommodation | Warrant | Email | English | `WEE` | 35 | [WEE_SendingToCrownCourtAuthorityToHoldInYouthDetentionAccommodation.md](./WEE_SendingToCrownCourtAuthorityToHoldInYouthDetentionAccommodation.md) |
| WEE SendingToCrownCourtForTrial | Warrant | Email | English | `WEE` | 34 | [WEE_SendingToCrownCourtForTrial.md](./WEE_SendingToCrownCourtForTrial.md) |
| WXE RemandWarrantYouthDetentionAccommodation | Warrant | File storage | English | `WXE` | 43 | [WXE_RemandWarrantYouthDetentionAccommodation.md](./WXE_RemandWarrantYouthDetentionAccommodation.md) |
