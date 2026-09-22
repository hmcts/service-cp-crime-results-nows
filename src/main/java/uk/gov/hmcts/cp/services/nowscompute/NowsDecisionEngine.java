package uk.gov.hmcts.cp.services.nowscompute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.clients.NowsMetadataClient;
import uk.gov.hmcts.cp.clients.NowsSubscriptionsClient;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDay;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;
import uk.gov.hmcts.cp.domain.nowscompute.NowsVocabulary;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NowsDecisionEngine {

    private final NowsVocabularyResolver vocabularyResolver;
    private final NowsMetadataClient nowsMetadataClient;
    private final NowsMetadataMatcher nowsMetadataMatcher;
    private final NowsSubscriptionsClient nowsSubscriptionsClient;
    private final NowsSubscriptionMatcher nowsSubscriptionMatcher;

    public Set<MatchedEventType> determineEligibleEventTypes(final MergedDefendant defendant, final HearingDetail hearing) {
        final NowsVocabulary vocabulary = vocabularyResolver.resolve(defendant, hearing);
        final LocalDate activeAt = activeAt(hearing);

        final List<NowDefinition> nowDefinitions = nowsMetadataClient.getNowDefinitions(activeAt);
        final List<MatchedEventType> candidates = nowsMetadataMatcher.match(defendant, nowDefinitions);
        if (candidates.isEmpty()) {
            log.info("determineEligibleEventTypes found no candidate NOW definitions for masterDefendantId:{}",
                    defendant.masterDefendantId());
            return Set.of();
        }

        final List<NowsSubscription> nowSubscriptions = nowsSubscriptionsClient.getNowSubscriptions(activeAt).stream()
                .filter(subscription -> Boolean.TRUE.equals(subscription.getIsNowSubscription()))
                .toList();

        final Set<MatchedEventType> eligible = new LinkedHashSet<>();
        for (final MatchedEventType candidate : candidates) {
            final boolean anySubscriptionMatches = nowSubscriptions.stream()
                    .anyMatch(subscription -> nowsSubscriptionMatcher.matches(
                            subscription, vocabulary, defendant, candidate.matchedResultTypeIds()));
            if (anySubscriptionMatches) {
                eligible.add(candidate);
            }
        }
        log.info("determineEligibleEventTypes evaluated masterDefendantId:{} — eligibleEventTypes:{}",
                defendant.masterDefendantId(), eligible.stream().map(MatchedEventType::eventType).toList());
        return eligible;
    }

    // The reference-data catalogues are keyed by an "active on" date, per the legacy pipeline's own
    // ReferenceDataService.js — the hearing's own sitting day, not an invented value.
    private LocalDate activeAt(final HearingDetail hearing) {
        if (hearing.getHearingDays() == null || hearing.getHearingDays().isEmpty()) {
            return LocalDate.now();
        }
        final HearingDay firstDay = hearing.getHearingDays().get(0);
        if (firstDay.getSittingDay() == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(firstDay.getSittingDay());
        } catch (DateTimeParseException e) {
            log.warn("activeAt could not parse hearingDays[0].sittingDay:{} — falling back to today",
                    firstDay.getSittingDay());
            return LocalDate.now();
        }
    }
}