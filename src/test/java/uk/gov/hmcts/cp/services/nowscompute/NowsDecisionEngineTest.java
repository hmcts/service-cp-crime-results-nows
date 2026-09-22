package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.clients.NowsMetadataClient;
import uk.gov.hmcts.cp.clients.NowsSubscriptionsClient;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDay;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowRequirement;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;
import uk.gov.hmcts.cp.services.ClockService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowsDecisionEngineTest {

    private static final String MASTER_DEFENDANT_ID = "21111111-1111-1111-1111-111111111111";
    private static final String REMAND_RESULT_TYPE_ID = "3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11";
    private static final LocalDate SITTING_DAY = LocalDate.parse("2026-07-23");
    private static final LocalDate TODAY = LocalDate.parse("2026-09-22");

    @Mock
    private NowsMetadataClient nowsMetadataClient;
    @Mock
    private NowsSubscriptionsClient nowsSubscriptionsClient;

    private NowsDecisionEngine decisionEngine;

    @BeforeEach
    void setUp() {
        final ClockService fixedClock = new ClockService(
                Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        decisionEngine = new NowsDecisionEngine(
                new NowsVocabularyResolver(),
                nowsMetadataClient,
                new NowsMetadataMatcher(),
                nowsSubscriptionsClient,
                new NowsSubscriptionMatcher(),
                fixedClock);
    }

    @Test
    void determineEligibleEventTypes_should_returnWeeRemand_whenPrimaryResultMatchesAndSubscriptionAppliesUnconditionally() {
        final MergedDefendant defendant = remandDefendant();
        final HearingDetail hearing = hearingSittingOn(SITTING_DAY);
        when(nowsMetadataClient.getNowDefinitions(SITTING_DAY)).thenReturn(List.of(weeRemandDefinition()));
        when(nowsSubscriptionsClient.getNowSubscriptions(SITTING_DAY))
                .thenReturn(List.of(unconditionalSubscription()));

        final Set<MatchedEventType> result = decisionEngine.determineEligibleEventTypes(defendant, hearing);

        assertThat(result).extracting(MatchedEventType::eventType).containsExactly("WEE_Remand");
        assertThat(result).flatExtracting(MatchedEventType::matchedResultTypeIds)
                .containsExactly(REMAND_RESULT_TYPE_ID);
    }

    @Test
    void determineEligibleEventTypes_should_returnEmptySet_whenDefinitionNotInAllowList() {
        final MergedDefendant defendant = remandDefendant();
        final HearingDetail hearing = hearingSittingOn(SITTING_DAY);
        final NowDefinition unregisteredDefinition = NowDefinition.builder()
                .id("def-2")
                .name("WEE_CustodialSentence")
                .includeAllResults(false)
                .nowRequirements(List.of(NowRequirement.builder()
                        .resultDefinitionId(REMAND_RESULT_TYPE_ID)
                        .primary(true)
                        .build()))
                .build();
        when(nowsMetadataClient.getNowDefinitions(SITTING_DAY)).thenReturn(List.of(unregisteredDefinition));

        final Set<MatchedEventType> result = decisionEngine.determineEligibleEventTypes(defendant, hearing);

        assertThat(result).isEmpty();
        verify(nowsSubscriptionsClient, never()).getNowSubscriptions(any());
    }

    @Test
    void determineEligibleEventTypes_should_returnEmptySet_whenNoSubscriptionIsANowSubscription() {
        final MergedDefendant defendant = remandDefendant();
        final HearingDetail hearing = hearingSittingOn(SITTING_DAY);
        when(nowsMetadataClient.getNowDefinitions(SITTING_DAY)).thenReturn(List.of(weeRemandDefinition()));
        final NowsSubscription notNowSubscription = NowsSubscription.builder()
                .isNowSubscription(false)
                .applySubscriptionRules(true)
                .build();
        when(nowsSubscriptionsClient.getNowSubscriptions(SITTING_DAY)).thenReturn(List.of(notNowSubscription));

        final Set<MatchedEventType> result = decisionEngine.determineEligibleEventTypes(defendant, hearing);

        assertThat(result).isEmpty();
    }

    @Test
    void determineEligibleEventTypes_should_fallBackToToday_whenHearingHasNoSittingDay() {
        final MergedDefendant defendant = remandDefendant();
        final HearingDetail hearing = HearingDetail.builder().hearingDays(List.of()).build();
        when(nowsMetadataClient.getNowDefinitions(TODAY)).thenReturn(List.of(weeRemandDefinition()));
        when(nowsSubscriptionsClient.getNowSubscriptions(TODAY))
                .thenReturn(List.of(unconditionalSubscription()));

        final Set<MatchedEventType> result = decisionEngine.determineEligibleEventTypes(defendant, hearing);

        assertThat(result).extracting(MatchedEventType::eventType).containsExactly("WEE_Remand");
    }

    private MergedDefendant remandDefendant() {
        final JudicialResult result = JudicialResult.builder()
                .judicialResultTypeId(REMAND_RESULT_TYPE_ID)
                .build();
        return new MergedDefendant(MASTER_DEFENDANT_ID, false, false, null,
                List.of("11111111-1111-1111-1111-111111111111"), List.of(result));
    }

    private HearingDetail hearingSittingOn(final LocalDate sittingDay) {
        return HearingDetail.builder()
                .hearingDays(List.of(HearingDay.builder().sittingDay(sittingDay.toString()).build()))
                .build();
    }

    private NowDefinition weeRemandDefinition() {
        return NowDefinition.builder()
                .id("def-1")
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(NowRequirement.builder()
                        .resultDefinitionId(REMAND_RESULT_TYPE_ID)
                        .primary(true)
                        .build()))
                .build();
    }

    private NowsSubscription unconditionalSubscription() {
        return NowsSubscription.builder()
                .isNowSubscription(true)
                .applySubscriptionRules(false)
                .build();
    }
}