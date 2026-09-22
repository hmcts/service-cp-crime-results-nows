package uk.gov.hmcts.cp.domain.nowscompute;

import lombok.Builder;

import java.util.List;

@Builder
public record NowsVocabulary(
        boolean custodyLocationIsPolice,
        boolean custodyLocationIsPrison,
        boolean inCustody,
        boolean atleastOneCustodialResult,
        boolean allNonCustodialResults,
        boolean atleastOneNonCustodialResult,
        boolean cpsProsecuted,
        boolean youthDefendant,
        boolean adultDefendant,
        boolean welshCourtHearing,
        boolean englishCourtHearing,
        boolean appearedInPerson,
        boolean appearedByVideoLink,
        boolean anyAppearance,
        List<String> prosecutorMajorCreditor,
        List<String> nonProsecutorMajorCreditor) {
}