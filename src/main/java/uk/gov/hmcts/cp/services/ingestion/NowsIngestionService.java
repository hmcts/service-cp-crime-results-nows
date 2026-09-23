package uk.gov.hmcts.cp.services.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.clients.HearingResultedCacheClient;
import uk.gov.hmcts.cp.clients.ResultsClient;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.exceptions.IncompleteHearingDetailsException;
import uk.gov.hmcts.cp.services.nowscompute.DefendantMerger;
import uk.gov.hmcts.cp.services.nowscompute.MatchedEventType;
import uk.gov.hmcts.cp.services.nowscompute.MergedDefendant;
import uk.gov.hmcts.cp.services.nowscompute.NowsDecisionEngine;
import uk.gov.hmcts.cp.services.persistence.NowsRecordService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NowsIngestionService {

    private final HearingResultedCacheClient cacheClient;
    private final ResultsClient resultsClient;
    private final ObjectMapper objectMapper;
    private final DefendantMerger defendantMerger;
    private final NowsDecisionEngine decisionEngine;
    private final NowsRecordService recordService;

    public void ingestAndProcessOnce(final UUID hearingId, final LocalDate hearingDay) {
        final HearingDetailsResponse hearingDetails = fetchIfComplete(hearingId, hearingDay)
                .orElseThrow(() -> new IncompleteHearingDetailsException(hearingId));
        process(hearingId, hearingDay, hearingDetails);
    }

    private Optional<HearingDetailsResponse> fetchIfComplete(final UUID hearingId, final LocalDate hearingDay) {
        final HearingDetailsResponse response = cacheClient.get(hearingId, hearingDay)
                .map(this::deserializeCachedHearingResults)
                .orElseGet(() -> resultsClient.getHearingDetails(hearingId));
        return isComplete(response) ? Optional.of(response) : Optional.empty();
    }

    private void process(final UUID hearingId, final LocalDate hearingDay, final HearingDetailsResponse hearingDetails) {
        final HearingDetail hearing = hearingDetails.getHearing();
        defendantMerger.merge(hearing)
                .forEach(defendant -> processDefendant(hearingId, hearingDay, defendant, hearing));
    }

    private void processDefendant(final UUID hearingId, final LocalDate hearingDay, final MergedDefendant defendant,
                                   final HearingDetail hearing) {
        final Set<MatchedEventType> eligibleEventTypes = decisionEngine.determineEligibleEventTypes(defendant, hearing);
        log.info("NOW generation gate evaluated for hearingId:{} — eligibleEventTypes:{}",
                hearingId, eligibleEventTypes.stream().map(MatchedEventType::eventType).toList());
        recordService.record(hearingId, hearingDay, defendant, hearing, eligibleEventTypes);
    }

    private HearingDetailsResponse deserializeCachedHearingResults(final String cachedJson) {
        try {
            return objectMapper.readValue(cachedJson, HearingDetailsResponse.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Malformed cached hearing-result payload", e);
        }
    }

    private boolean isComplete(final HearingDetailsResponse response) {
        return response != null
                && response.getHearing() != null
                && response.getHearing().getProsecutionCases() != null
                && !response.getHearing().getProsecutionCases().isEmpty();
    }
}
