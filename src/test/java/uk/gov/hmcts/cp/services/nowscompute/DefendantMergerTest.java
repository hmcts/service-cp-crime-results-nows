package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.ApplicationParty;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CourtApplication;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CustodialEstablishment;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.DefendantJudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.MasterDefendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Offence;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDefendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.ProsecutionCase;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Prosecutor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefendantMergerTest {

    private static final String MASTER_DEFENDANT_ID = "21111111-1111-1111-1111-111111111111";

    private final DefendantMerger merger = new DefendantMerger();

    @Test
    void merge_should_returnSingleMergedDefendant_whenSingleProsecutionCaseDefendant() {
        final JudicialResult caseResult = JudicialResult.builder().judicialResultTypeId("rt-case").build();
        final Defendant defendant = Defendant.builder()
                .id("d-1")
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .isYouth(false)
                .defendantCaseJudicialResults(List.of(caseResult))
                .build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .prosecutor(Prosecutor.builder().isCps(true).build())
                .build();
        final HearingDetail hearing = HearingDetail.builder().prosecutionCases(List.of(prosecutionCase)).build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).hasSize(1);
        final MergedDefendant only = merged.get(0);
        assertThat(only.masterDefendantId()).isEqualTo(MASTER_DEFENDANT_ID);
        assertThat(only.cpsProsecuted()).isTrue();
        assertThat(only.defendantIds()).containsExactly("d-1");
        assertThat(only.results()).containsExactly(caseResult);
    }

    @Test
    void merge_should_foldOffenceJudicialResults_intoResults() {
        final JudicialResult offenceResult = JudicialResult.builder().judicialResultTypeId("rt-offence").build();
        final Offence offence = Offence.builder().id("off-1").judicialResults(List.of(offenceResult)).build();
        final Defendant defendant = Defendant.builder()
                .id("d-1")
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .offences(List.of(offence))
                .build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();
        final HearingDetail hearing = HearingDetail.builder().prosecutionCases(List.of(prosecutionCase)).build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).results()).containsExactly(offenceResult);
    }

    @Test
    void merge_should_capturesCustody_fromPersonDefendant() {
        final Defendant defendant = Defendant.builder()
                .id("d-1")
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .personDefendant(PersonDefendant.builder()
                        .custodialEstablishment(CustodialEstablishment.builder().custody("Prison").build())
                        .build())
                .build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();
        final HearingDetail hearing = HearingDetail.builder().prosecutionCases(List.of(prosecutionCase)).build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged.get(0).custody()).isEqualTo("Prison");
    }

    @Test
    void merge_should_skipDefendant_whenMasterDefendantIdMissing() {
        final Defendant defendant = Defendant.builder().id("d-1").build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();
        final HearingDetail hearing = HearingDetail.builder().prosecutionCases(List.of(prosecutionCase)).build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).isEmpty();
    }

    @Test
    void merge_should_foldCourtApplicationResults_intoMatchingMasterDefendant() {
        final JudicialResult caseResult = JudicialResult.builder().judicialResultTypeId("rt-case").build();
        final Defendant defendant = Defendant.builder()
                .id("d-1")
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .defendantCaseJudicialResults(List.of(caseResult))
                .build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();

        final JudicialResult applicationResult = JudicialResult.builder().judicialResultTypeId("rt-application").build();
        final CourtApplication courtApplication = CourtApplication.builder()
                .id("app-1")
                .subject(ApplicationParty.builder()
                        .masterDefendant(MasterDefendant.builder().masterDefendantId(MASTER_DEFENDANT_ID).build())
                        .build())
                .judicialResults(List.of(applicationResult))
                .build();

        final HearingDetail hearing = HearingDetail.builder()
                .prosecutionCases(List.of(prosecutionCase))
                .courtApplications(List.of(courtApplication))
                .build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).results()).containsExactlyInAnyOrder(caseResult, applicationResult);
    }

    @Test
    void merge_should_createMergedDefendant_forCourtApplicationOnlyMasterDefendant() {
        final JudicialResult applicationResult = JudicialResult.builder().judicialResultTypeId("rt-application").build();
        final CourtApplication courtApplication = CourtApplication.builder()
                .id("app-1")
                .subject(ApplicationParty.builder()
                        .masterDefendant(MasterDefendant.builder().masterDefendantId(MASTER_DEFENDANT_ID).build())
                        .build())
                .judicialResults(List.of(applicationResult))
                .build();
        final HearingDetail hearing = HearingDetail.builder().courtApplications(List.of(courtApplication)).build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).masterDefendantId()).isEqualTo(MASTER_DEFENDANT_ID);
        assertThat(merged.get(0).results()).containsExactly(applicationResult);
    }

    @Test
    void merge_should_foldHearingWideDefendantJudicialResults_intoMatchingMasterDefendant() {
        final JudicialResult caseResult = JudicialResult.builder().judicialResultTypeId("rt-case").build();
        final Defendant defendant = Defendant.builder()
                .id("d-1")
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .defendantCaseJudicialResults(List.of(caseResult))
                .build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();

        final JudicialResult hearingWideResult = JudicialResult.builder().judicialResultTypeId("rt-hearing-wide").build();
        final DefendantJudicialResult defendantJudicialResult = DefendantJudicialResult.builder()
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .judicialResult(hearingWideResult)
                .build();

        final HearingDetail hearing = HearingDetail.builder()
                .prosecutionCases(List.of(prosecutionCase))
                .defendantJudicialResults(List.of(defendantJudicialResult))
                .build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).results()).containsExactlyInAnyOrder(caseResult, hearingWideResult);
    }

    @Test
    void merge_should_returnEmptyList_whenHearingHasNoDefendantSources() {
        final HearingDetail hearing = HearingDetail.builder().build();

        final List<MergedDefendant> merged = merger.merge(hearing);

        assertThat(merged).isEmpty();
    }
}