package uk.gov.hmcts.cp.services.nowscompute;

import lombok.Builder;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Offence;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDefendant;

import java.util.List;

@Builder
public record MergedDefendant(
        String masterDefendantId,
        boolean isYouth,
        boolean cpsProsecuted,
        String custody,
        PersonDefendant personDefendant,
        List<DefendantCaseLink> cases,
        List<Offence> offences,
        List<JudicialResult> results) {

    public List<String> defendantIds() {
        return cases.stream().map(DefendantCaseLink::defendantId).toList();
    }
}