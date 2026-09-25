package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Address;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CourtCentre;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDay;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.LocalJusticeArea;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Offence;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDefendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDetails;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent;
import uk.gov.hmcts.cp.services.nowscompute.DefendantCaseLink;
import uk.gov.hmcts.cp.services.nowscompute.MergedDefendant;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefendantSnapshotContentMapperTest {

    private static final UUID HEARING_ID = UUID.fromString("6988027f-e786-49f4-a00f-7c35ab459464");
    private static final String RESULT_TYPE_ID = "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11";
    private static final LocalDate SITTING_DAY = LocalDate.parse("2026-09-02");

    @InjectMocks
    private DefendantSnapshotContentMapper mapper;

    @Test
    void toContent_should_mapDefendantHearingAndOffences() {
        final DefendantSnapshotContent content = mapper.toContent(HEARING_ID, mergedDefendant(), hearing());

        assertThat(content.defendant().firstName()).isEqualTo("Lacy");
        assertThat(content.defendant().lastName()).isEqualTo("Braun");
        assertThat(content.defendant().dateOfBirth()).isEqualTo(LocalDate.parse("1998-09-02"));
        assertThat(content.defendant().address().postCode()).isEqualTo("NW1 5BR");
        assertThat(content.defendant().gender()).isEqualTo("MALE");

        assertThat(content.hearing().id()).isEqualTo(HEARING_ID);
        assertThat(content.hearing().courtHouseName()).isEqualTo("Lavender Hill Magistrates' Court");
        assertThat(content.hearing().ljaName()).isEqualTo("South West London Magistrates' Court");
        assertThat(content.hearing().hearingDate()).isEqualTo(SITTING_DAY);
        assertThat(content.hearing().jurisdiction()).isEqualTo("MAGISTRATES");

        assertThat(content.offences()).hasSize(1);
        assertThat(content.offences().get(0).code()).isEqualTo("TH68013A");
        assertThat(content.offences().get(0).results()).hasSize(1);
        assertThat(content.offences().get(0).results().get(0).judicialResultTypeId()).isEqualTo(RESULT_TYPE_ID);
        assertThat(content.offences().get(0).results().get(0).prompts().get(0).promptReference())
                .isEqualTo("prisonOrganisationName");
    }

    @Test
    void toContent_should_leaveDefendantNull_whenNoPersonDetailsRecorded() {
        final MergedDefendant defendant = mergedDefendantBuilder().personDefendant(null).build();

        final DefendantSnapshotContent content = mapper.toContent(HEARING_ID, defendant, hearing());

        assertThat(content.defendant()).isNull();
    }

    @Test
    void toContent_should_leaveHearingDateNull_whenSittingDayUnparseable() {
        final HearingDetail hearing = HearingDetail.builder()
                .hearingDays(List.of(HearingDay.builder().sittingDay("not-a-date").build()))
                .build();

        final DefendantSnapshotContent content = mapper.toContent(HEARING_ID, mergedDefendant(), hearing);

        assertThat(content.hearing().hearingDate()).isNull();
    }

    private MergedDefendant mergedDefendant() {
        return mergedDefendantBuilder().build();
    }

    private MergedDefendant.MergedDefendantBuilder mergedDefendantBuilder() {
        final JudicialResult result = JudicialResult.builder()
                .judicialResultTypeId(RESULT_TYPE_ID)
                .cjsCode("RIBA48")
                .label("Remanded in custody")
                .orderedDate(SITTING_DAY)
                .judicialResultPrompts(List.of(JudicialResultPrompt.builder()
                        .promptReference("prisonOrganisationName")
                        .label("Prison organisation name")
                        .value("HMP/YOI Durham")
                        .build()))
                .build();
        final Offence offence = Offence.builder()
                .offenceCode("TH68013A")
                .offenceTitle("Attempt theft of motor vehicle")
                .wording("Attempt theft to vehicle")
                .convictionDate(SITTING_DAY)
                .judicialResults(List.of(result))
                .build();
        return MergedDefendant.builder()
                .masterDefendantId("d2151771-41a1-42e1-af36-a99d9b39c0b2")
                .personDefendant(PersonDefendant.builder()
                        .personDetails(PersonDetails.builder()
                                .title("Mr")
                                .firstName("Lacy")
                                .lastName("Braun")
                                .dateOfBirth(LocalDate.parse("1998-09-02"))
                                .gender("MALE")
                                .address(Address.builder().address1("221B Baker Street").postcode("NW1 5BR").build())
                                .build())
                        .build())
                .cases(List.of(new DefendantCaseLink("RC363968376", "d2151771-41a1-42e1-af36-a99d9b39c0b2")))
                .offences(List.of(offence))
                .results(List.of(result));
    }

    private HearingDetail hearing() {
        return HearingDetail.builder()
                .courtCentre(CourtCentre.builder()
                        .code("B01LY00")
                        .name("Lavender Hill Magistrates' Court")
                        .lja(LocalJusticeArea.builder().ljaName("South West London Magistrates' Court").build())
                        .build())
                .hearingDays(List.of(HearingDay.builder().sittingDay(SITTING_DAY.toString()).build()))
                .jurisdictionType("MAGISTRATES")
                .build();
    }
}