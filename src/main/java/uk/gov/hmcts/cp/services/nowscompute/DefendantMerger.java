package uk.gov.hmcts.cp.services.nowscompute;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CourtApplication;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.DefendantJudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Offence;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.ProsecutionCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
public class DefendantMerger {

    public List<MergedDefendant> merge(final HearingDetail hearing) {
        final Map<String, Accumulator> byMasterDefendantId = new LinkedHashMap<>();

        for (final ProsecutionCase prosecutionCase : nullSafe(hearing.getProsecutionCases())) {
            final boolean isCps = prosecutionCase.getProsecutor() != null
                    && Boolean.TRUE.equals(prosecutionCase.getProsecutor().getIsCps());
            for (final Defendant defendant : nullSafe(prosecutionCase.getDefendants())) {
                final String masterDefendantId = defendant.getMasterDefendantId();
                if (masterDefendantId == null) {
                    continue;
                }
                final Accumulator acc = byMasterDefendantId.computeIfAbsent(masterDefendantId, Accumulator::new);
                acc.isYouth = acc.isYouth || Boolean.TRUE.equals(defendant.getIsYouth());
                acc.cpsProsecuted = acc.cpsProsecuted || isCps;
                if (acc.custody == null && defendant.getPersonDefendant() != null
                        && defendant.getPersonDefendant().getCustodialEstablishment() != null) {
                    acc.custody = defendant.getPersonDefendant().getCustodialEstablishment().getCustody();
                }
                acc.defendantIds.add(defendant.getId());
                acc.results.addAll(nullSafe(defendant.getDefendantCaseJudicialResults()));
                for (final Offence offence : nullSafe(defendant.getOffences())) {
                    acc.results.addAll(nullSafe(offence.getJudicialResults()));
                }
            }
        }

        for (final CourtApplication courtApplication : nullSafe(hearing.getCourtApplications())) {
            final String masterDefendantId = masterDefendantIdOf(courtApplication);
            if (masterDefendantId == null) {
                continue;
            }
            final Accumulator acc = byMasterDefendantId.computeIfAbsent(masterDefendantId, Accumulator::new);
            acc.results.addAll(nullSafe(courtApplication.getJudicialResults()));
        }

        for (final DefendantJudicialResult defendantJudicialResult : nullSafe(hearing.getDefendantJudicialResults())) {
            final String masterDefendantId = defendantJudicialResult.getMasterDefendantId();
            if (masterDefendantId == null) {
                continue;
            }
            final Accumulator acc = byMasterDefendantId.computeIfAbsent(masterDefendantId, Accumulator::new);
            if (defendantJudicialResult.getJudicialResult() != null) {
                acc.results.add(defendantJudicialResult.getJudicialResult());
            }
        }

        return byMasterDefendantId.values().stream().map(Accumulator::toMergedDefendant).toList();
    }

    private String masterDefendantIdOf(final CourtApplication courtApplication) {
        if (courtApplication.getSubject() == null || courtApplication.getSubject().getMasterDefendant() == null) {
            return null;
        }
        return courtApplication.getSubject().getMasterDefendant().getMasterDefendantId();
    }

    private <T> List<T> nullSafe(final List<T> list) {
        return list == null ? List.of() : list;
    }

    private static final class Accumulator {
        private final String masterDefendantId;
        private boolean isYouth;
        private boolean cpsProsecuted;
        private String custody;
        private final List<String> defendantIds = new ArrayList<>();
        private final List<JudicialResult> results = new ArrayList<>();

        private Accumulator(final String masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
        }

        private MergedDefendant toMergedDefendant() {
            return new MergedDefendant(masterDefendantId, isYouth, cpsProsecuted, custody, defendantIds, results);
        }
    }
}