package uk.gov.hmcts.cp.services.nowscompute;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscriptionVocabulary;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.ResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsVocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
public class NowsSubscriptionMatcher {

    public boolean matches(final NowsSubscription subscription, final NowsVocabulary vocabulary,
                            final MergedDefendant defendant, final Set<String> matchedResultTypeIds) {
        final boolean matches;
        if (isTrue(subscription.getIsNowSubscription())) {
            matches = matchesRules(subscription, vocabulary, defendant, matchedResultTypeIds);
        } else {
            matches = false;
        }
        return matches;
    }

    private boolean matchesRules(final NowsSubscription subscription, final NowsVocabulary vocabulary,
                                  final MergedDefendant defendant, final Set<String> matchedResultTypeIds) {
        final NowsSubscriptionVocabulary rules = subscription.getSubscriptionVocabulary();
        final boolean matches;
        if (!isTrue(subscription.getApplySubscriptionRules()) || rules == null) {
            matches = true;
        } else if (isTrue(rules.getIsCpsProsecuted()) && vocabulary.cpsProsecuted()) {
            matches = true;
        } else {
            matches = matchesEveryRuleDimension(rules, vocabulary, defendant, matchedResultTypeIds);
        }
        return matches;
    }

    private boolean matchesEveryRuleDimension(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary,
                                               final MergedDefendant defendant, final Set<String> matchedResultTypeIds) {
        final List<JudicialResult> matchedResults = defendant.results().stream()
                .filter(r -> r.getJudicialResultTypeId() != null && matchedResultTypeIds.contains(r.getJudicialResultTypeId()))
                .toList();

        return attendanceMatches(rules, vocabulary)
                && majorCreditorTypeMatches(rules)
                && courtLanguageMatches(rules, vocabulary)
                && ageGroupMatches(rules, vocabulary)
                && custodyMatches(rules, vocabulary)
                && custodialOutcomeMatches(rules, vocabulary)
                && promptListsMatch(rules, matchedResults)
                && resultTypeListsMatch(rules, matchedResultTypeIds);
    }

    private boolean attendanceMatches(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary) {
        return isTrue(rules.getAnyAppearance())
                || (isTrue(rules.getAppearedInPerson()) && vocabulary.appearedInPerson())
                || (isTrue(rules.getAppearedByVideoLink()) && vocabulary.appearedByVideoLink());
    }

    // Lists are always empty until major-creditor is actually built (design doc §9 item 2) — a
    // specific-creditor requirement can never be satisfied yet, only "any" passes.
    private boolean majorCreditorTypeMatches(final NowsSubscriptionVocabulary rules) {
        return isTrue(rules.getAnyMajorCreditor())
                || (!isTrue(rules.getRequiresProsecutorMajorCreditor())
                        && !isTrue(rules.getRequiresNonProsecutorMajorCreditor()));
    }

    private boolean courtLanguageMatches(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary) {
        return isTrue(rules.getAnyCourtHearing())
                || (isTrue(rules.getEnglishCourtHearing()) && vocabulary.englishCourtHearing())
                || (isTrue(rules.getWelshCourtHearing()) && vocabulary.welshCourtHearing());
    }

    private boolean ageGroupMatches(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary) {
        return isTrue(rules.getAdultOrYouthDefendant())
                || (isTrue(rules.getYouthDefendant()) && vocabulary.youthDefendant())
                || (isTrue(rules.getAdultDefendant()) && vocabulary.adultDefendant());
    }

    private boolean custodyMatches(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary) {
        final boolean custodyLocationIsPolice = isTrue(rules.getCustodyLocationIsPolice());
        final boolean custodyLocationIsPrison = isTrue(rules.getCustodyLocationIsPrison());
        final boolean inCustody = isTrue(rules.getInCustody());
        return isTrue(rules.getIgnoreCustody())
                || (inCustody && !custodyLocationIsPolice && !custodyLocationIsPrison && vocabulary.inCustody())
                || (inCustody && custodyLocationIsPolice && !custodyLocationIsPrison
                        && vocabulary.custodyLocationIsPolice())
                || (inCustody && !custodyLocationIsPolice && custodyLocationIsPrison
                        && vocabulary.custodyLocationIsPrison());
    }

    private boolean custodialOutcomeMatches(final NowsSubscriptionVocabulary rules, final NowsVocabulary vocabulary) {
        return isTrue(rules.getIgnoreResults())
                || (isTrue(rules.getAllNonCustodialResults()) && vocabulary.allNonCustodialResults())
                || (isTrue(rules.getAtleastOneNonCustodialResult()) && vocabulary.atleastOneNonCustodialResult())
                || (isTrue(rules.getAtleastOneCustodialResult()) && vocabulary.atleastOneCustodialResult());
    }

    private boolean promptListsMatch(final NowsSubscriptionVocabulary rules, final List<JudicialResult> matchedResults) {
        final List<String> promptReferences = matchedResults.stream()
                .filter(r -> r.getJudicialResultPrompts() != null)
                .flatMap(r -> r.getJudicialResultPrompts().stream())
                .map(JudicialResultPrompt::getPromptReference)
                .toList();
        return listMatches(resultPromptReferencesOf(rules.getIncludedPrompts()), promptReferences, true)
                && listMatches(resultPromptReferencesOf(rules.getExcludedPrompts()), promptReferences, false);
    }

    private List<String> resultPromptReferencesOf(final List<ResultPrompt> prompts) {
        return prompts == null ? List.of() : prompts.stream().map(ResultPrompt::getResultPromptReference).toList();
    }

    private boolean resultTypeListsMatch(final NowsSubscriptionVocabulary rules, final Set<String> matchedResultTypeIds) {
        final List<String> resultTypeIds = new ArrayList<>(matchedResultTypeIds);
        return listMatches(rules.getIncludedResults(), resultTypeIds, true)
                && listMatches(rules.getExcludedResults(), resultTypeIds, false);
    }

    private boolean listMatches(final List<String> configured, final List<String> actual, final boolean isInclude) {
        return configured == null
                || configured.isEmpty()
                || isInclude == actual.stream().anyMatch(value -> containsIgnoreCase(configured, value));
    }

    private boolean containsIgnoreCase(final List<String> values, final String candidate) {
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(candidate));
    }

    private boolean isTrue(final Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}