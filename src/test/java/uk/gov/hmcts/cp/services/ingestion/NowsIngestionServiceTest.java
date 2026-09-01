package uk.gov.hmcts.cp.services.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.clients.HearingResultedCacheClient;
import uk.gov.hmcts.cp.clients.ResultsClient;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.ProsecutionCase;
import uk.gov.hmcts.cp.exceptions.IncompleteHearingDetailsException;
import uk.gov.hmcts.cp.services.nowscompute.NowsDecisionEngine;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowsIngestionServiceTest {

    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final LocalDate HEARING_DAY = LocalDate.parse("2026-07-23");

    @Mock
    private HearingResultedCacheClient cacheClient;
    @Mock
    private ResultsClient resultsClient;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private NowsDecisionEngine decisionEngine;

    @InjectMocks
    private NowsIngestionService ingestionService;

    @Test
    void ingestAndProcessOnce_should_evaluateDecisionEngine_forEachDefendant_whenRedisMissAndRestComplete() {
        when(cacheClient.get(HEARING_ID, HEARING_DAY)).thenReturn(Optional.empty());
        when(resultsClient.getHearingDetails(HEARING_ID)).thenReturn(hearingWithOneDefendant());
        when(decisionEngine.determineEligibleEventTypes(any(), any())).thenReturn(Set.of());

        ingestionService.ingestAndProcessOnce(HEARING_ID, HEARING_DAY);

        verify(decisionEngine, times(1)).determineEligibleEventTypes(any(), any());
    }

    @Test
    void ingestAndProcessOnce_should_useCachedPayload_whenRedisHit() {
        when(cacheClient.get(HEARING_ID, HEARING_DAY))
                .thenReturn(Optional.of("{\"hearing\":{\"prosecutionCases\":[{\"id\":\"case-1\",\"defendants\":[{\"id\":\"11111111-1111-1111-1111-111111111111\"}]}]}}"));
        when(decisionEngine.determineEligibleEventTypes(any(), any())).thenReturn(Set.of());

        ingestionService.ingestAndProcessOnce(HEARING_ID, HEARING_DAY);

        verify(resultsClient, never()).getHearingDetails(any(UUID.class));
        verify(decisionEngine, times(1)).determineEligibleEventTypes(any(), any());
    }

    @Test
    void ingestAndProcessOnce_should_throwIllegalStateException_whenCachedPayloadMalformed() {
        when(cacheClient.get(HEARING_ID, HEARING_DAY)).thenReturn(Optional.of("not-json"));

        assertThatThrownBy(() -> ingestionService.ingestAndProcessOnce(HEARING_ID, HEARING_DAY))
                .isInstanceOf(IllegalStateException.class);

        verify(decisionEngine, never()).determineEligibleEventTypes(any(), any());
    }

    @Test
    void ingestAndProcessOnce_should_throwIncompleteHearingDetailsException_withoutRetrying_whenIncomplete() {
        when(cacheClient.get(HEARING_ID, HEARING_DAY)).thenReturn(Optional.empty());
        when(resultsClient.getHearingDetails(HEARING_ID)).thenReturn(incompleteResponse());

        assertThatThrownBy(() -> ingestionService.ingestAndProcessOnce(HEARING_ID, HEARING_DAY))
                .isInstanceOf(IncompleteHearingDetailsException.class);

        verify(resultsClient, times(1)).getHearingDetails(HEARING_ID);
        verify(decisionEngine, never()).determineEligibleEventTypes(any(), any());
    }

    @Test
    void ingestAndProcessOnce_should_evaluateEachDefendant_whenMultipleDefendantsOnOneCase() {
        when(cacheClient.get(HEARING_ID, HEARING_DAY)).thenReturn(Optional.empty());
        when(resultsClient.getHearingDetails(HEARING_ID)).thenReturn(hearingWithTwoDefendantsOnOneCase());
        when(decisionEngine.determineEligibleEventTypes(any(), any())).thenReturn(Set.of());

        ingestionService.ingestAndProcessOnce(HEARING_ID, HEARING_DAY);

        verify(decisionEngine, times(2)).determineEligibleEventTypes(any(), any());
    }

    private HearingDetailsResponse hearingWithOneDefendant() {
        final Defendant defendant = Defendant.builder().id("11111111-1111-1111-1111-111111111111").build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendant))
                .build();
        return HearingDetailsResponse.builder()
                .hearing(HearingDetailsResponse.HearingDetail.builder()
                        .prosecutionCases(List.of(prosecutionCase))
                        .build())
                .build();
    }

    private HearingDetailsResponse hearingWithTwoDefendantsOnOneCase() {
        final Defendant defendantOne = Defendant.builder().id("11111111-1111-1111-1111-111111111111").build();
        final Defendant defendantTwo = Defendant.builder().id("22222222-2222-2222-2222-222222222222").build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.builder()
                .id("case-1")
                .defendants(List.of(defendantOne, defendantTwo))
                .build();
        return HearingDetailsResponse.builder()
                .hearing(HearingDetailsResponse.HearingDetail.builder()
                        .prosecutionCases(List.of(prosecutionCase))
                        .build())
                .build();
    }

    private HearingDetailsResponse incompleteResponse() {
        return HearingDetailsResponse.builder()
                .hearing(HearingDetailsResponse.HearingDetail.builder()
                        .prosecutionCases(List.of())
                        .build())
                .build();
    }
}
