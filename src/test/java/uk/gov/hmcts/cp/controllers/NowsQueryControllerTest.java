package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;
import uk.gov.hmcts.cp.services.query.NowsQueryService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowsQueryControllerTest {

    private static final String CASE_URN = "RC363968376";
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private NowsQueryService nowsQueryService;

    @InjectMocks
    private NowsQueryController controller;

    @Test
    void getDefendantResult_should_returnOkWithServiceResult_whenCaseUrnValid() {
        final DefendantResult expected = DefendantResult.builder().caseURN(CASE_URN).build();
        when(nowsQueryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null)).thenReturn(expected);

        final ResponseEntity<DefendantResult> response =
                controller.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
    }

    @Test
    void getDefendantResult_should_throwBadRequest_whenCaseUrnMalformed() {
        assertThatThrownBy(() -> controller.getDefendantResult("not a urn!", HEARING_ID, DEFENDANT_ID, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }
}