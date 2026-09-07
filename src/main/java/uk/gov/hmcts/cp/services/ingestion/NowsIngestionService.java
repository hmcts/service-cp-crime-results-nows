package uk.gov.hmcts.cp.services.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.clients.HearingResultedCacheClient;
import uk.gov.hmcts.cp.clients.ResultsClient;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.exceptions.IncompleteHearingDetailsException;
import uk.gov.hmcts.cp.services.nowscompute.NowsDecisionEngine;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Fetches a hearing's results (Redis-first, REST-fallback, same as
 * service-cp-crime-results-pcr's ResultsIngestionService) and runs the NOW generation gate per
 * defendant. Persistence is not yet implemented — see the TODO in {@link #process}.
 *
 * <p>No in-process completeness-retry loop here, unlike pcr's ResultsIngestionService: ADR-001
 * gives NOWS a single ingestion trigger (the Service Bus queue), so retry on an incomplete result
 * is entirely queue-level (HearingResultedServiceBusConsumer completes the message and schedules
 * a follow-up), not an in-process loop like pcr's synchronous-webhook path needed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NowsIngestionService {

    private final HearingResultedCacheClient cacheClient;
    private final ResultsClient resultsClient;
    private final ObjectMapper objectMapper;
    private final NowsDecisionEngine decisionEngine;

    public void ingestAndProcessOnce(final UUID hearingId, final LocalDate hearingDay) {
        final HearingDetailsResponse hearingDetails = fetchIfComplete(hearingId, hearingDay)
                .orElseThrow(() -> new IncompleteHearingDetailsException(hearingId));
        process(hearingId, hearingDetails);
    }

    private Optional<HearingDetailsResponse> fetchIfComplete(final UUID hearingId, final LocalDate hearingDay) {
        final HearingDetailsResponse response = cacheClient.get(hearingId, hearingDay)
                .map(this::deserializeCachedHearingResults)
                .orElseGet(() -> resultsClient.getHearingDetails(hearingId));
        return isComplete(response) ? Optional.of(response) : Optional.empty();
    }

    private void process(final UUID hearingId, final HearingDetailsResponse hearingDetails) {
        final HearingDetail hearing = hearingDetails.getHearing();
        hearing.getProsecutionCases().forEach(prosecutionCase ->
                prosecutionCase.getDefendants().forEach(defendant -> processDefendant(hearingId, defendant, hearing)));
    }

    private void processDefendant(final UUID hearingId, final Defendant defendant, final HearingDetail hearing) {
        final Set<String> eligibleEventTypes = decisionEngine.determineEligibleEventTypes(defendant, hearing);
        log.info("NOW generation gate evaluated for hearingId:{} defendantId:{} — eligibleEventTypes:{}",
                hearingId, defendant.getId(), eligibleEventTypes);
        // TODO(transformer/datastore): persist eligible event types once those layers exist
        // (design doc §8: one row per (hearingId, masterDefendantId, eventType)).
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
