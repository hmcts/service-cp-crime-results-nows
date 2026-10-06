package uk.gov.hmcts.cp.mappers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NowsQueryMapperTest {

    private static final UUID DEFENDANT_ID = UUID.fromString("d2151771-41a1-42e1-af36-a99d9b39c0b2");
    private static final UUID HEARING_ID = UUID.fromString("6988027f-e786-49f4-a00f-7c35ab459464");
    private static final String MATCHED_RESULT_TYPE_ID = "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11";
    private static final String UNMATCHED_RESULT_TYPE_ID = "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9";
    private static final String CASE_URN = "RC363968376";

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NowsQueryMapper mapper;

    private final String content = readResourceContents("nows/defendant-snapshot-content-sample.json");

    @Test
    void toDefendantResult_should_mapDefendantAndHearing_fromSnapshotContent() {
        final DefendantResult result =
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, content, List.of(event(MATCHED_RESULT_TYPE_ID)));

        assertThat(result.getCaseURN()).isEqualTo(CASE_URN);
        assertThat(result.getDefendant().getId()).isEqualTo(DEFENDANT_ID);
        assertThat(result.getDefendant().getFirstName()).isEqualTo("Lacy");
        assertThat(result.getDefendant().getAddress().getPostCode()).isEqualTo("NW1 5BR");
        assertThat(result.getHearing().getId()).isEqualTo(HEARING_ID);
        assertThat(result.getHearing().getCourtDetails().getCourt().getCourtHouseName())
                .isEqualTo("Lavender Hill Magistrates' Court");
        assertThat(result.getHearing().getCourtDetails().getLjaName())
                .isEqualTo("South West London Magistrates' Court");
    }

    @Test
    void toDefendantResult_should_keepOnlyResultsMatchedForTheEventType() {
        final DefendantResult result =
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, content, List.of(event(MATCHED_RESULT_TYPE_ID)));

        assertThat(result.getEventTypes()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getEventType()).isEqualTo("WEE_Remand");
        assertThat(result.getEventTypes().get(0).getOrderName()).isEqualTo("Remand Warrant");
        assertThat(result.getEventTypes().get(0).getOffences()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getCode()).isEqualTo("TH68013A");
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults().get(0).getJudicialResultTypeId())
                .isEqualTo(UUID.fromString(MATCHED_RESULT_TYPE_ID));
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults().get(0).getPrompts().get(0)
                .getPromptReference()).isEqualTo("prisonOrganisationName");
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getLegislation())
                .isEqualTo("Contrary to section 1(1) of the Criminal Attempts Act 1981.");
        assertThat(result.getEventTypes().get(0).getDefendantResults()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getDefendantResults().get(0).getPrompts().get(0)
                .getPromptReference()).isEqualTo("riskOrVulnerabilityFactors");
    }

    @Test
    void toDefendantResult_should_returnEmptyDefendantResults_whenSnapshotHasNone() {
        final String contentWithoutDefendantResults = """
                { "hearing": { "id": "6988027f-e786-49f4-a00f-7c35ab459464" }, "offences": [] }
                """;

        final DefendantResult result = mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, contentWithoutDefendantResults,
                List.of(event(MATCHED_RESULT_TYPE_ID)));

        assertThat(result.getEventTypes().get(0).getDefendantResults()).isEmpty();
    }

    @Test
    void toDefendantResult_should_returnEmptyEventTypes_whenNoEventsRecorded() {
        final DefendantResult result = mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, content, List.of());

        assertThat(result.getEventTypes()).isEmpty();
    }

    @Test
    void toDefendantResult_should_keepBothOffences_whenResultTypeMatchesAcrossThem() {
        final DefendantResult result =
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, content, List.of(event(UNMATCHED_RESULT_TYPE_ID)));

        assertThat(result.getEventTypes().get(0).getOffences()).hasSize(2);
    }

    private EventEntity event(final String matchedResultTypeId) {
        return EventEntity.builder()
                .eventType("WEE_Remand")
                .orderName("Remand Warrant")
                .matchedResultTypeIds("[\"" + matchedResultTypeId + "\"]")
                .matchedAt(OffsetDateTime.parse("2026-09-02T18:05:00Z"))
                .build();
    }

    @SneakyThrows
    private String readResourceContents(final String resourceName) {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        return Files.readString(Path.of(resource.toURI()));
    }
}