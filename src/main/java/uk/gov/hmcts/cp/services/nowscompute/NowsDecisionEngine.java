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
import uk.gov.hmcts.cp.services.ClockService;

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
    private final ClockService clockService;

    public Set<MatchedEventType> determineEligibleEventTypes(final MergedDefendant defendant, final HearingDetail hearing) {
        final NowsVocabulary vocabulary = vocabularyResolver.resolve(defendant, hearing);
        final LocalDate activeAt = activeAt(hearing);

        final List<NowDefinition> nowDefinitions = nowsMetadataClient.getNowDefinitions(activeAt);
        final List<MatchedEventType> candidates = nowsMetadataMatcher.match(defendant, nowDefinitions);

        final Set<MatchedEventType> eligible;
        if (candidates.isEmpty()) {
            log.info("determineEligibleEventTypes found no candidate NOW definitions for masterDefendantId:{}",
                    defendant.masterDefendantId());
            eligible = Set.of();
        } else {
            eligible = eligibleAgainstSubscriptions(defendant, vocabulary, activeAt, candidates);
            log.info("determineEligibleEventTypes evaluated masterDefendantId:{} — eligibleEventTypes:{}",
                    defendant.masterDefendantId(), eligible.stream().map(MatchedEventType::eventType).toList());
        }
        return eligible;
    }

    private Set<MatchedEventType> eligibleAgainstSubscriptions(final MergedDefendant defendant,
                                                                 final NowsVocabulary vocabulary,
                                                                 final LocalDate activeAt,
                                                                 final List<MatchedEventType> candidates) {
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
        return eligible;
    }

    // The reference-data catalogues are keyed by an "active on" date, per the legacy pipeline's own
    // ReferenceDataService.js — the hearing's own sitting day, not an invented value.
    private LocalDate activeAt(final HearingDetail hearing) {
        final LocalDate activeAt;
        if (hearing.getHearingDays() == null || hearing.getHearingDays().isEmpty()) {
            activeAt = clockService.today();
        } else {
            activeAt = activeAtFromSittingDay(hearing.getHearingDays().get(0));
        }
        return activeAt;
    }

    private LocalDate activeAtFromSittingDay(final HearingDay firstDay) {
        final LocalDate activeAt;
        if (firstDay.getSittingDay() == null) {
            activeAt = clockService.today();
        } else {
            activeAt = parseSittingDayOrToday(firstDay.getSittingDay());
        }
        return activeAt;
    }

    private LocalDate parseSittingDayOrToday(final String sittingDay) {
        LocalDate activeAt;
        try {
            activeAt = LocalDate.parse(sittingDay);
        } catch (DateTimeParseException e) {
            log.warn("activeAt could not parse hearingDays[0].sittingDay:{} — falling back to today", sittingDay);
            activeAt = clockService.today();
        }
        return activeAt;
    }
}