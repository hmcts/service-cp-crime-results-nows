package uk.gov.hmcts.cp.services.nowscompute;

import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;

import java.util.List;

public record MergedDefendant(
        String masterDefendantId,
        boolean isYouth,
        boolean cpsProsecuted,
        String custody,
        List<String> defendantIds,
        List<JudicialResult> results) {
}