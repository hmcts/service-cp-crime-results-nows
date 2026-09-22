package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscriptionVocabulary;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.ResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsVocabulary;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NowsSubscriptionMatcherTest {

    private static final String MASTER_DEFENDANT_ID = "master-1";
    private static final String DEFENDANT_ID = "d-1";
    private static final String RESULT_TYPE_ID = "rt-1";

    private final NowsSubscriptionMatcher matcher = new NowsSubscriptionMatcher();

    @Test
    void matches_should_returnFalse_whenIsNowSubscriptionIsFalse() {
        final NowsSubscription subscription = NowsSubscription.builder()
                .isNowSubscription(false)
                .build();

        final boolean result = matcher.matches(subscription, emptyVocabulary(), defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_returnTrue_whenApplySubscriptionRulesIsFalse() {
        final NowsSubscription subscription = NowsSubscription.builder()
                .isNowSubscription(true)
                .applySubscriptionRules(false)
                .build();

        final boolean result = matcher.matches(subscription, emptyVocabulary(), defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_returnTrue_whenRulesAreNull() {
        final NowsSubscription subscription = NowsSubscription.builder()
                .isNowSubscription(true)
                .applySubscriptionRules(true)
                .subscriptionVocabulary(null)
                .build();

        final boolean result = matcher.matches(subscription, emptyVocabulary(), defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_shortCircuitTrue_whenIsCpsProsecutedRuleAndVocabularyBothTrue() {
        final NowsSubscriptionVocabulary rules = NowsSubscriptionVocabulary.builder()
                .isCpsProsecuted(true)
                // Deliberately impossible rule elsewhere — proves the CPS branch actually short-circuits.
                .anyCourtHearing(false)
                .englishCourtHearing(false)
                .welshCourtHearing(false)
                .build();
        final NowsSubscription subscription = subscriptionWithRules(rules);
        final NowsVocabulary vocabulary = vocabularyBuilder().cpsProsecuted(true).build();

        final boolean result = matcher.matches(subscription, vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_matchAttendance_whenAnyAppearanceRuleIsTrue() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept().anyAppearance(true).build();
        final NowsVocabulary vocabulary = vocabularyBuilder().anyAppearance(false).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failAttendance_whenNoAttendanceRuleMatchesVocabulary() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .anyAppearance(false)
                .appearedInPerson(true)
                .appearedByVideoLink(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().appearedInPerson(false).appearedByVideoLink(false).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchMajorCreditor_whenNoSpecificCreditorRequired() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .anyMajorCreditor(false)
                .requiresProsecutorMajorCreditor(false)
                .requiresNonProsecutorMajorCreditor(false)
                .build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failMajorCreditor_whenSpecificCreditorRequired_sinceListsAreAlwaysEmpty() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .anyMajorCreditor(false)
                .requiresProsecutorMajorCreditor(true)
                .build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchCourtLanguage_whenEnglishCourtHearingRuleAndVocabularyBothTrue() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .anyCourtHearing(false)
                .englishCourtHearing(true)
                .welshCourtHearing(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().englishCourtHearing(true).welshCourtHearing(false).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failCourtLanguage_whenVocabularyIsWelshButRuleRequiresEnglish() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .anyCourtHearing(false)
                .englishCourtHearing(true)
                .welshCourtHearing(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().englishCourtHearing(false).welshCourtHearing(true).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchAgeGroup_whenYouthDefendantRuleAndVocabularyBothTrue() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .adultOrYouthDefendant(false)
                .youthDefendant(true)
                .adultDefendant(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().youthDefendant(true).adultDefendant(false).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failAgeGroup_whenVocabularyIsAdultButRuleRequiresYouth() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .adultOrYouthDefendant(false)
                .youthDefendant(true)
                .adultDefendant(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().youthDefendant(false).adultDefendant(true).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchCustody_whenCustodyLocationIsPoliceRuleAndVocabularyBothTrue() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .ignoreCustody(false)
                .inCustody(true)
                .custodyLocationIsPolice(true)
                .custodyLocationIsPrison(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().custodyLocationIsPolice(true).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failCustody_whenVocabularyIsPrisonButRuleRequiresPolice() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .ignoreCustody(false)
                .inCustody(true)
                .custodyLocationIsPolice(true)
                .custodyLocationIsPrison(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().custodyLocationIsPrison(true).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchCustodialOutcome_whenAtleastOneCustodialResultRuleAndVocabularyBothTrue() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .ignoreResults(false)
                .allNonCustodialResults(false)
                .atleastOneNonCustodialResult(false)
                .atleastOneCustodialResult(true)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().atleastOneCustodialResult(true).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failCustodialOutcome_whenNoOutcomeRuleMatchesVocabulary() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .ignoreResults(false)
                .allNonCustodialResults(true)
                .atleastOneNonCustodialResult(false)
                .atleastOneCustodialResult(false)
                .build();
        final NowsVocabulary vocabulary = vocabularyBuilder().allNonCustodialResults(false).build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), vocabulary, defendant(), Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchIncludedPrompts_whenAMatchedResultCarriesTheRequiredPrompt() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .includedPrompts(List.of(ResultPrompt.builder().resultPromptReference("prisonOrganisationName").build()))
                .build();
        final JudicialResult matchedResult = JudicialResult.builder()
                .judicialResultTypeId(RESULT_TYPE_ID)
                .judicialResultPrompts(List.of(JudicialResultPrompt.builder().promptReference("prisonOrganisationName").build()))
                .build();
        final MergedDefendant defendant = new MergedDefendant(
                MASTER_DEFENDANT_ID, false, false, null, List.of(DEFENDANT_ID), List.of(matchedResult));

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant, Set.of(RESULT_TYPE_ID));

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failIncludedPrompts_whenMatchedResultLacksTheRequiredPrompt() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .includedPrompts(List.of(ResultPrompt.builder().resultPromptReference("prisonOrganisationName").build()))
                .build();
        final JudicialResult matchedResult = JudicialResult.builder().judicialResultTypeId(RESULT_TYPE_ID).build();
        final MergedDefendant defendant = new MergedDefendant(
                MASTER_DEFENDANT_ID, false, false, null, List.of(DEFENDANT_ID), List.of(matchedResult));

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant, Set.of(RESULT_TYPE_ID));

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_failExcludedPrompts_whenMatchedResultCarriesAnExcludedPrompt() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .excludedPrompts(List.of(ResultPrompt.builder().resultPromptReference("bannedPrompt").build()))
                .build();
        final JudicialResult matchedResult = JudicialResult.builder()
                .judicialResultTypeId(RESULT_TYPE_ID)
                .judicialResultPrompts(List.of(JudicialResultPrompt.builder().promptReference("bannedPrompt").build()))
                .build();
        final MergedDefendant defendant = new MergedDefendant(
                MASTER_DEFENDANT_ID, false, false, null, List.of(DEFENDANT_ID), List.of(matchedResult));

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant, Set.of(RESULT_TYPE_ID));

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_matchIncludedResults_whenAMatchedResultTypeIdIsIncluded() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .includedResults(List.of(RESULT_TYPE_ID))
                .build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant(), Set.of(RESULT_TYPE_ID));

        assertThat(result).isTrue();
    }

    @Test
    void matches_should_failIncludedResults_whenNoMatchedResultTypeIdIsIncluded() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .includedResults(List.of("rt-other"))
                .build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant(), Set.of(RESULT_TYPE_ID));

        assertThat(result).isFalse();
    }

    @Test
    void matches_should_failExcludedResults_whenAMatchedResultTypeIdIsExcluded() {
        final NowsSubscriptionVocabulary rules = fullyPermissiveRulesExcept()
                .excludedResults(List.of(RESULT_TYPE_ID))
                .build();

        final boolean result = matcher.matches(subscriptionWithRules(rules), emptyVocabulary(), defendant(), Set.of(RESULT_TYPE_ID));

        assertThat(result).isFalse();
    }

    private NowsSubscription subscriptionWithRules(final NowsSubscriptionVocabulary rules) {
        return NowsSubscription.builder()
                .isNowSubscription(true)
                .applySubscriptionRules(true)
                .subscriptionVocabulary(rules)
                .build();
    }

    // All dimensions wide-open ("any"/"ignore") except isCpsProsecuted (left null/false so the
    // short-circuit never fires) — a caller narrows exactly the one dimension under test.
    private NowsSubscriptionVocabulary.NowsSubscriptionVocabularyBuilder fullyPermissiveRulesExcept() {
        return NowsSubscriptionVocabulary.builder()
                .isCpsProsecuted(false)
                .anyAppearance(true)
                .anyMajorCreditor(true)
                .anyCourtHearing(true)
                .adultOrYouthDefendant(true)
                .ignoreCustody(true)
                .ignoreResults(true);
    }

    private NowsVocabulary.NowsVocabularyBuilder vocabularyBuilder() {
        return NowsVocabulary.builder()
                .prosecutorMajorCreditor(List.of())
                .nonProsecutorMajorCreditor(List.of());
    }

    private NowsVocabulary emptyVocabulary() {
        return vocabularyBuilder().build();
    }

    private MergedDefendant defendant() {
        return new MergedDefendant(MASTER_DEFENDANT_ID, false, false, null, List.of(DEFENDANT_ID), List.of());
    }
}