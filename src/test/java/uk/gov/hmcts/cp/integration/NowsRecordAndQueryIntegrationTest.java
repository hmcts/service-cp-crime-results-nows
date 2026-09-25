package uk.gov.hmcts.cp.integration;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
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
import uk.gov.hmcts.cp.integration.config.PostgresInitialise;
import uk.gov.hmcts.cp.repositories.DefendantCaseRepository;
import uk.gov.hmcts.cp.repositories.DefendantRepository;
import uk.gov.hmcts.cp.repositories.DefendantSnapshotRepository;
import uk.gov.hmcts.cp.repositories.EventRepository;
import uk.gov.hmcts.cp.repositories.HearingRepository;
import uk.gov.hmcts.cp.services.nowscompute.DefendantCaseLink;
import uk.gov.hmcts.cp.services.nowscompute.MatchedEventType;
import uk.gov.hmcts.cp.services.nowscompute.MergedDefendant;
import uk.gov.hmcts.cp.services.persistence.NowsRecordService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresInitialise.class)
@TestPropertySource(properties = "service-bus.auto-start-processors=false")
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class NowsRecordAndQueryIntegrationTest {

    private static final UUID HEARING_ID = UUID.fromString("6988027f-e786-49f4-a00f-7c35ab459464");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("d2151771-41a1-42e1-af36-a99d9b39c0b2");
    private static final UUID DEFENDANT_ID = UUID.fromString("86fc543b-4090-43f3-bd6d-8c1522844c99");
    private static final UUID UNKNOWN_ID = UUID.fromString("00000000-0000-0000-0000-0000000000ff");
    private static final String CASE_URN = "RC363968376";
    private static final String REMAND_RESULT_TYPE_ID = "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11";
    private static final String OTHER_RESULT_TYPE_ID = "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9";
    private static final LocalDate HEARING_DAY = LocalDate.parse("2026-09-02");

    @Resource
    private MockMvc mockMvc;
    @Resource
    private NowsRecordService recordService;
    @Resource
    private HearingRepository hearingRepository;
    @Resource
    private DefendantRepository defendantRepository;
    @Resource
    private DefendantCaseRepository defendantCaseRepository;
    @Resource
    private DefendantSnapshotRepository defendantSnapshotRepository;
    @Resource
    private EventRepository eventRepository;

    @BeforeEach
    void clearRecords() {
        eventRepository.deleteAll();
        defendantSnapshotRepository.deleteAll();
        defendantCaseRepository.deleteAll();
        defendantRepository.deleteAll();
        hearingRepository.deleteAll();
    }

    @Test
    void getDefendantResult_should_returnContentFilteredToMatchedResults() throws Exception {
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), matchedRemand());

        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, HEARING_ID, DEFENDANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseURN").value(CASE_URN))
                .andExpect(jsonPath("$.defendant.id").value(DEFENDANT_ID.toString()))
                .andExpect(jsonPath("$.defendant.firstName").value("Lacy"))
                .andExpect(jsonPath("$.defendant.address.postCode").value("NW1 5BR"))
                .andExpect(jsonPath("$.hearing.id").value(HEARING_ID.toString()))
                .andExpect(jsonPath("$.hearing.courtDetails.court.courtHouseName")
                        .value("Lavender Hill Magistrates' Court"))
                .andExpect(jsonPath("$.hearing.courtDetails.ljaName").value("South West London Magistrates' Court"))
                .andExpect(jsonPath("$.eventTypes.length()").value(1))
                .andExpect(jsonPath("$.eventTypes[0].eventType").value("WEE_Remand"))
                .andExpect(jsonPath("$.eventTypes[0].offences.length()").value(1))
                .andExpect(jsonPath("$.eventTypes[0].offences[0].code").value("TH68013A"))
                .andExpect(jsonPath("$.eventTypes[0].offences[0].results.length()").value(1))
                .andExpect(jsonPath("$.eventTypes[0].offences[0].results[0].judicialResultTypeId")
                        .value(REMAND_RESULT_TYPE_ID))
                .andExpect(jsonPath("$.eventTypes[0].offences[0].results[0].prompts[0].promptReference")
                        .value("prisonOrganisationName"));
    }

    @Test
    void getDefendantResult_should_returnEmptyEventTypes_whenNothingMatchedForAKnownDefendant() throws Exception {
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), Set.of());

        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, HEARING_ID, DEFENDANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTypes.length()").value(0));
    }

    @Test
    void getDefendantResult_should_narrowToRequestedEventType_whenEventTypeFilterSupplied() throws Exception {
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), matchedRemand());

        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, HEARING_ID, DEFENDANT_ID)
                        .param("eventType", "WEE_CustodialSentence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTypes.length()").value(0));
    }

    @Test
    void getDefendantResult_should_returnSingleEventType_whenSameHearingRecordedTwice() throws Exception {
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), matchedRemand());
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), matchedRemand());

        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, HEARING_ID, DEFENDANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTypes.length()").value(1));
    }

    @Test
    void getDefendantResult_should_return404_whenHearingNeverIngested() throws Exception {
        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, UNKNOWN_ID, DEFENDANT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDefendantResult_should_return404_whenDefendantUnknownOnAKnownHearing() throws Exception {
        recordService.recordResult(HEARING_ID, HEARING_DAY, mergedDefendant(), hearing(), matchedRemand());

        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        CASE_URN, HEARING_ID, UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDefendantResult_should_return400_whenCaseUrnMalformed() throws Exception {
        mockMvc.perform(get("/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                        "not a urn!", HEARING_ID, DEFENDANT_ID))
                .andExpect(status().isBadRequest());
    }

    private Set<MatchedEventType> matchedRemand() {
        return Set.of(new MatchedEventType("WEE_Remand", Set.of(REMAND_RESULT_TYPE_ID)));
    }

    private MergedDefendant mergedDefendant() {
        return MergedDefendant.builder()
                .masterDefendantId(MASTER_DEFENDANT_ID.toString())
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
                .cases(List.of(new DefendantCaseLink(CASE_URN, DEFENDANT_ID.toString())))
                .offences(List.of(offence()))
                .results(List.of(remandResult(), unrelatedResult()))
                .build();
    }

    private Offence offence() {
        return Offence.builder()
                .offenceCode("TH68013A")
                .offenceTitle("Attempt theft of motor vehicle")
                .wording("Attempt theft to vehicle")
                .convictionDate(HEARING_DAY)
                .judicialResults(List.of(remandResult(), unrelatedResult()))
                .build();
    }

    private JudicialResult remandResult() {
        return JudicialResult.builder()
                .judicialResultTypeId(REMAND_RESULT_TYPE_ID)
                .cjsCode("RIBA48")
                .label("Remanded in custody")
                .orderedDate(HEARING_DAY)
                .judicialResultPrompts(List.of(JudicialResultPrompt.builder()
                        .promptReference("prisonOrganisationName")
                        .label("Prison organisation name")
                        .value("HMP/YOI Durham")
                        .build()))
                .build();
    }

    private JudicialResult unrelatedResult() {
        return JudicialResult.builder()
                .judicialResultTypeId(OTHER_RESULT_TYPE_ID)
                .cjsCode("OTHER")
                .label("Unrelated result")
                .orderedDate(HEARING_DAY)
                .build();
    }

    private HearingDetail hearing() {
        return HearingDetail.builder()
                .courtCentre(CourtCentre.builder()
                        .code("B01LY00")
                        .name("Lavender Hill Magistrates' Court")
                        .lja(LocalJusticeArea.builder().ljaName("South West London Magistrates' Court").build())
                        .build())
                .hearingDays(List.of(HearingDay.builder().sittingDay(HEARING_DAY.toString()).build()))
                .jurisdictionType("MAGISTRATES")
                .build();
    }
}