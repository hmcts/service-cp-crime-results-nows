package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.AttendanceDay;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CourtCentre;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.DefendantAttendance;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsVocabulary;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NowsVocabularyResolverTest {

    private static final String DEFENDANT_ID = "d-1";
    private static final LocalDate RESULT_DATE = LocalDate.parse("2026-07-23");

    private final NowsVocabularyResolver resolver = new NowsVocabularyResolver();

    @Test
    void resolve_should_setCustodyLocationIsPolice_whenCustodyIsPoliceStation() {
        final MergedDefendant defendant = defendantWithCustody("Police Station");

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.custodyLocationIsPolice()).isTrue();
        assertThat(vocabulary.custodyLocationIsPrison()).isFalse();
        assertThat(vocabulary.inCustody()).isTrue();
    }

    @Test
    void resolve_should_setCustodyLocationIsPrison_whenCustodyIsPrison() {
        final MergedDefendant defendant = defendantWithCustody("Prison");

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.custodyLocationIsPrison()).isTrue();
        assertThat(vocabulary.custodyLocationIsPolice()).isFalse();
        assertThat(vocabulary.inCustody()).isTrue();
    }

    @Test
    void resolve_should_setNeitherCustodyLocation_whenCustodyIsNeitherPoliceNorPrison() {
        final MergedDefendant defendant = defendantWithCustody(null);

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.custodyLocationIsPolice()).isFalse();
        assertThat(vocabulary.custodyLocationIsPrison()).isFalse();
        assertThat(vocabulary.inCustody()).isFalse();
    }

    @Test
    void resolve_should_setAtleastOneCustodialResult_whenAResultHasPrisonOrganisationNamePrompt() {
        final JudicialResult custodial = resultWithPrompt("prisonOrganisationName");
        final JudicialResult nonCustodial = JudicialResult.builder().build();
        final MergedDefendant defendant = defendantWithResults(List.of(custodial, nonCustodial));

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.atleastOneCustodialResult()).isTrue();
        assertThat(vocabulary.atleastOneNonCustodialResult()).isTrue();
        assertThat(vocabulary.allNonCustodialResults()).isFalse();
    }

    @Test
    void resolve_should_setAllNonCustodialResults_whenNoResultHasPrisonOrganisationNamePrompt() {
        final JudicialResult nonCustodial = JudicialResult.builder().build();
        final MergedDefendant defendant = defendantWithResults(List.of(nonCustodial));

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.allNonCustodialResults()).isTrue();
        assertThat(vocabulary.atleastOneCustodialResult()).isFalse();
        assertThat(vocabulary.atleastOneNonCustodialResult()).isTrue();
    }

    @Test
    void resolve_should_setCpsProsecuted_fromMergedDefendant() {
        final MergedDefendant defendant = defendantBuilder().cpsProsecuted(true).build();

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.cpsProsecuted()).isTrue();
    }

    @Test
    void resolve_should_setYouthDefendant_fromMergedDefendant() {
        final MergedDefendant defendant = defendantBuilder().isYouth(true).build();

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.youthDefendant()).isTrue();
        assertThat(vocabulary.adultDefendant()).isFalse();
    }

    @Test
    void resolve_should_setWelshCourtHearing_whenCourtCentreIsWelsh() {
        final MergedDefendant defendant = defendantWithResults(List.of());
        final HearingDetail hearing = HearingDetail.builder()
                .courtCentre(CourtCentre.builder().welshCourtCentre(true).build())
                .build();

        final NowsVocabulary vocabulary = resolver.resolve(defendant, hearing);

        assertThat(vocabulary.welshCourtHearing()).isTrue();
        assertThat(vocabulary.englishCourtHearing()).isFalse();
    }

    @Test
    void resolve_should_setEnglishCourtHearing_whenCourtCentreIsNotWelsh() {
        final MergedDefendant defendant = defendantWithResults(List.of());
        final HearingDetail hearing = HearingDetail.builder()
                .courtCentre(CourtCentre.builder().welshCourtCentre(false).build())
                .build();

        final NowsVocabulary vocabulary = resolver.resolve(defendant, hearing);

        assertThat(vocabulary.englishCourtHearing()).isTrue();
        assertThat(vocabulary.welshCourtHearing()).isFalse();
    }

    @Test
    void resolve_should_setAppearedInPerson_whenAttendanceDayMatchesAResultDate() {
        final JudicialResult result = JudicialResult.builder().orderedDate(RESULT_DATE).build();
        final MergedDefendant defendant = defendantWithResults(List.of(result));
        final HearingDetail hearing = hearingWithAttendance("IN_PERSON");

        final NowsVocabulary vocabulary = resolver.resolve(defendant, hearing);

        assertThat(vocabulary.appearedInPerson()).isTrue();
        assertThat(vocabulary.appearedByVideoLink()).isFalse();
        assertThat(vocabulary.anyAppearance()).isTrue();
    }

    @Test
    void resolve_should_setAppearedByVideoLink_whenAttendanceDayMatchesAResultDate() {
        final JudicialResult result = JudicialResult.builder().orderedDate(RESULT_DATE).build();
        final MergedDefendant defendant = defendantWithResults(List.of(result));
        final HearingDetail hearing = hearingWithAttendance("BY_VIDEO");

        final NowsVocabulary vocabulary = resolver.resolve(defendant, hearing);

        assertThat(vocabulary.appearedByVideoLink()).isTrue();
        assertThat(vocabulary.appearedInPerson()).isFalse();
    }

    @Test
    void resolve_should_setNoAttendance_whenAttendanceDayDoesNotMatchAnyResultDate() {
        final JudicialResult result = JudicialResult.builder().orderedDate(LocalDate.parse("2026-01-01")).build();
        final MergedDefendant defendant = defendantWithResults(List.of(result));
        final HearingDetail hearing = hearingWithAttendance("IN_PERSON");

        final NowsVocabulary vocabulary = resolver.resolve(defendant, hearing);

        assertThat(vocabulary.appearedInPerson()).isFalse();
        assertThat(vocabulary.appearedByVideoLink()).isFalse();
        assertThat(vocabulary.anyAppearance()).isFalse();
    }

    @Test
    void resolve_should_leaveMajorCreditorListsEmpty_alwaysUntilBuilt() {
        final MergedDefendant defendant = defendantWithResults(List.of());

        final NowsVocabulary vocabulary = resolver.resolve(defendant, HearingDetail.builder().build());

        assertThat(vocabulary.prosecutorMajorCreditor()).isEmpty();
        assertThat(vocabulary.nonProsecutorMajorCreditor()).isEmpty();
    }

    private MergedDefendant defendantWithCustody(final String custody) {
        return defendantBuilder().custody(custody).build();
    }

    private MergedDefendant defendantWithResults(final List<JudicialResult> results) {
        return defendantBuilder().results(results).build();
    }

    private MergedDefendant.MergedDefendantBuilder defendantBuilder() {
        return MergedDefendant.builder()
                .masterDefendantId("master-1")
                .cases(List.of(new DefendantCaseLink("RC363968376", DEFENDANT_ID)))
                .offences(List.of())
                .results(List.of());
    }

    private JudicialResult resultWithPrompt(final String promptReference) {
        return JudicialResult.builder()
                .judicialResultPrompts(List.of(JudicialResultPrompt.builder().promptReference(promptReference).build()))
                .build();
    }

    private HearingDetail hearingWithAttendance(final String attendanceType) {
        final AttendanceDay day = AttendanceDay.builder()
                .day(RESULT_DATE.toString())
                .attendanceType(attendanceType)
                .build();
        final DefendantAttendance attendance = DefendantAttendance.builder()
                .defendantId(DEFENDANT_ID)
                .attendanceDays(List.of(day))
                .build();
        return HearingDetail.builder().defendantAttendance(List.of(attendance)).build();
    }
}