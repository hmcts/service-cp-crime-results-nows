package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;

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

    private static final String CONTENT = """
            {
              "defendant": {
                "title": "Mr", "firstName": "Lacy", "lastName": "Braun",
                "dateOfBirth": "1998-09-02", "gender": "MALE",
                "address": { "address1": "221B Baker Street", "postCode": "NW1 5BR" }
              },
              "hearing": {
                "id": "6988027f-e786-49f4-a00f-7c35ab459464",
                "courtHouseCode": "B01LY00",
                "courtHouseName": "Lavender Hill Magistrates' Court",
                "ljaName": "South West London Magistrates' Court",
                "hearingDate": "2026-09-02",
                "jurisdiction": "MAGISTRATES"
              },
              "offences": [
                {
                  "code": "TH68013A", "title": "Attempt theft of motor vehicle",
                  "wording": "Attempt theft to vehicle", "convictionDate": "2026-09-02",
                  "results": [
                    {
                      "judicialResultTypeId": "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11",
                      "cjsCode": "RIBA48", "label": "Remanded in custody", "orderedDate": "2026-09-02",
                      "prompts": [
                        { "promptReference": "prisonOrganisationName", "label": "Prison organisation name", "value": "HMP/YOI Durham" }
                      ]
                    },
                    {
                      "judicialResultTypeId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9",
                      "cjsCode": "OTHER", "label": "Unrelated result", "orderedDate": "2026-09-02",
                      "prompts": []
                    }
                  ]
                },
                {
                  "code": "ZZ99999", "title": "Unmatched offence", "results": [
                    {
                      "judicialResultTypeId": "9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9",
                      "cjsCode": "OTHER", "label": "Unrelated result", "prompts": []
                    }
                  ]
                }
              ]
            }
            """;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NowsQueryMapper mapper;

    @Test
    void toDefendantResult_should_mapDefendantAndHearing_fromSnapshotContent() {
        final DefendantResult result =
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, CONTENT, List.of(event(MATCHED_RESULT_TYPE_ID)));

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
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, CONTENT, List.of(event(MATCHED_RESULT_TYPE_ID)));

        assertThat(result.getEventTypes()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getEventType()).isEqualTo("WEE_Remand");
        assertThat(result.getEventTypes().get(0).getOffences()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getCode()).isEqualTo("TH68013A");
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults()).hasSize(1);
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults().get(0).getJudicialResultTypeId())
                .isEqualTo(UUID.fromString(MATCHED_RESULT_TYPE_ID));
        assertThat(result.getEventTypes().get(0).getOffences().get(0).getResults().get(0).getPrompts().get(0)
                .getPromptReference()).isEqualTo("prisonOrganisationName");
    }

    @Test
    void toDefendantResult_should_returnEmptyEventTypes_whenNoEventsRecorded() {
        final DefendantResult result = mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, CONTENT, List.of());

        assertThat(result.getEventTypes()).isEmpty();
    }

    @Test
    void toDefendantResult_should_keepBothOffences_whenResultTypeMatchesAcrossThem() {
        final DefendantResult result =
                mapper.toDefendantResult(CASE_URN, DEFENDANT_ID, CONTENT, List.of(event(UNMATCHED_RESULT_TYPE_ID)));

        assertThat(result.getEventTypes().get(0).getOffences()).hasSize(2);
    }

    private EventEntity event(final String matchedResultTypeId) {
        return EventEntity.builder()
                .eventType("WEE_Remand")
                .matchedResultTypeIds("[\"" + matchedResultTypeId + "\"]")
                .matchedAt(OffsetDateTime.parse("2026-09-02T18:05:00Z"))
                .build();
    }
}