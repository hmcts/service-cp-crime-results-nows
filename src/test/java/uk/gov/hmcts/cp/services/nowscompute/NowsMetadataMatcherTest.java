package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowRequirement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NowsMetadataMatcherTest {

    private static final String MASTER_DEFENDANT_ID = "master-1";
    private static final String DEFENDANT_ID = "d-1";

    private final NowsMetadataMatcher matcher = new NowsMetadataMatcher();

    @Test
    void match_should_pruneDefinition_whenNameNotInAllowList() {
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_NotRegistered")
                .includeAllResults(true)
                .build();
        final MergedDefendant defendant = defendantWithResults(List.of(resultWithId("rt-1")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).isEmpty();
    }

    @Test
    void match_should_includeEveryResult_whenIncludeAllResultsIsTrue() {
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(true)
                .build();
        final MergedDefendant defendant = defendantWithResults(
                List.of(resultWithId("rt-1"), resultWithId("rt-2")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).eventType()).isEqualTo("WEE_Remand");
        assertThat(matches.get(0).matchedResultTypeIds()).containsExactlyInAnyOrder("rt-1", "rt-2");
    }

    @Test
    void match_should_matchOnPrimaryRequirement_whenResultTypeIdMatches() {
        final NowRequirement primaryRequirement = NowRequirement.builder()
                .resultDefinitionId("rt-1")
                .primary(true)
                .build();
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(primaryRequirement))
                .build();
        final MergedDefendant defendant = defendantWithResults(List.of(resultWithId("rt-1")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).matchedResultTypeIds()).containsExactly("rt-1");
    }

    @Test
    void match_should_returnNoCandidate_whenNoResultMatchesAnyPrimaryRequirement() {
        final NowRequirement primaryRequirement = NowRequirement.builder()
                .resultDefinitionId("rt-1")
                .primary(true)
                .build();
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(primaryRequirement))
                .build();
        final MergedDefendant defendant = defendantWithResults(List.of(resultWithId("rt-unrelated")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).isEmpty();
    }

    @Test
    void match_should_includeNonPrimaryChild_whenRootedUnderMatchedPrimary() {
        final NowRequirement childRequirement = NowRequirement.builder()
                .resultDefinitionId("rt-child")
                .primary(false)
                .rootResultDefinitionId("rt-1")
                .build();
        final NowRequirement primaryRequirement = NowRequirement.builder()
                .resultDefinitionId("rt-1")
                .primary(true)
                .nowRequirements(List.of(childRequirement))
                .build();
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(primaryRequirement))
                .build();
        final MergedDefendant defendant = defendantWithResults(
                List.of(resultWithId("rt-1"), resultWithId("rt-child")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).matchedResultTypeIds()).containsExactlyInAnyOrder("rt-1", "rt-child");
    }

    @Test
    void match_should_excludeNonPrimaryChild_whenItsRootWasNotMatched() {
        final NowRequirement childOfOtherRoot = NowRequirement.builder()
                .resultDefinitionId("rt-child")
                .primary(false)
                .rootResultDefinitionId("rt-not-matched")
                .build();
        final NowRequirement primaryRequirement = NowRequirement.builder()
                .resultDefinitionId("rt-1")
                .primary(true)
                .nowRequirements(List.of(childOfOtherRoot))
                .build();
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(primaryRequirement))
                .build();
        final MergedDefendant defendant = defendantWithResults(
                List.of(resultWithId("rt-1"), resultWithId("rt-child")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).matchedResultTypeIds()).containsExactly("rt-1");
    }

    @Test
    void match_should_flattenThreeLevelDeepTree_whenMatchingAtDeepestLevel() {
        final NowRequirement grandchild = NowRequirement.builder()
                .resultDefinitionId("rt-grandchild")
                .primary(true)
                .build();
        final NowRequirement child = NowRequirement.builder()
                .resultDefinitionId("rt-child")
                .primary(false)
                .nowRequirements(List.of(grandchild))
                .build();
        final NowRequirement root = NowRequirement.builder()
                .resultDefinitionId("rt-root")
                .primary(false)
                .nowRequirements(List.of(child))
                .build();
        final NowDefinition definition = NowDefinition.builder()
                .name("WEE_Remand")
                .includeAllResults(false)
                .nowRequirements(List.of(root))
                .build();
        final MergedDefendant defendant = defendantWithResults(List.of(resultWithId("rt-grandchild")));

        final List<MatchedEventType> matches = matcher.match(defendant, List.of(definition));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).matchedResultTypeIds()).containsExactly("rt-grandchild");
    }

    private MergedDefendant defendantWithResults(final List<JudicialResult> results) {
        return new MergedDefendant(MASTER_DEFENDANT_ID, false, false, null, List.of(DEFENDANT_ID), results);
    }

    private JudicialResult resultWithId(final String judicialResultTypeId) {
        return JudicialResult.builder().judicialResultTypeId(judicialResultTypeId).build();
    }
}