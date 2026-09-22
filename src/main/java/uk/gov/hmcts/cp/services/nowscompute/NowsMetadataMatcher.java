package uk.gov.hmcts.cp.services.nowscompute;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowRequirement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class NowsMetadataMatcher {

    public List<MatchedEventType> match(final MergedDefendant defendant, final List<NowDefinition> definitions) {
        final List<MatchedEventType> matches = new ArrayList<>();
        for (final NowDefinition definition : definitions) {
            if (!RegisteredNowEventTypes.ALLOW_LIST.contains(definition.getName())) {
                continue;
            }
            final Set<String> matchedResultTypeIds = matchedResultTypeIds(defendant, definition);
            if (!matchedResultTypeIds.isEmpty()) {
                matches.add(new MatchedEventType(definition.getName(), matchedResultTypeIds));
            }
        }
        return matches;
    }

    private Set<String> matchedResultTypeIds(final MergedDefendant defendant, final NowDefinition definition) {
        final Set<String> matched;
        if (Boolean.TRUE.equals(definition.getIncludeAllResults())) {
            matched = allResultTypeIds(defendant);
        } else {
            matched = matchedByRequirementTree(defendant, definition);
        }
        return matched;
    }

    private Set<String> matchedByRequirementTree(final MergedDefendant defendant, final NowDefinition definition) {
        final List<NowRequirement> flattened = flatten(definition.getNowRequirements());
        final Set<String> matched = matchedByPrimaryRequirement(defendant, flattened);
        if (!matched.isEmpty()) {
            matched.addAll(matchedByNonPrimaryChildOfMatchedRoot(defendant, flattened, matched));
        }
        return matched;
    }

    private Set<String> allResultTypeIds(final MergedDefendant defendant) {
        final Set<String> all = new LinkedHashSet<>();
        for (final JudicialResult result : defendant.results()) {
            if (result.getJudicialResultTypeId() != null) {
                all.add(result.getJudicialResultTypeId());
            }
        }
        return all;
    }

    private Set<String> matchedByPrimaryRequirement(final MergedDefendant defendant,
                                                      final List<NowRequirement> flattened) {
        final Set<String> primaryResultDefinitionIds = new LinkedHashSet<>();
        for (final NowRequirement requirement : flattened) {
            if (Boolean.TRUE.equals(requirement.getPrimary())) {
                primaryResultDefinitionIds.add(requirement.getResultDefinitionId());
            }
        }

        final Set<String> matched = new LinkedHashSet<>();
        for (final JudicialResult result : defendant.results()) {
            if (result.getJudicialResultTypeId() != null
                    && primaryResultDefinitionIds.contains(result.getJudicialResultTypeId())) {
                matched.add(result.getJudicialResultTypeId());
            }
        }
        return matched;
    }

    // Non-primary children rooted under a matched primary requirement are included too.
    private Set<String> matchedByNonPrimaryChildOfMatchedRoot(final MergedDefendant defendant,
                                                                final List<NowRequirement> flattened,
                                                                final Set<String> matchedPrimaries) {
        final Set<String> additional = new LinkedHashSet<>();
        for (final NowRequirement requirement : flattened) {
            if (Boolean.TRUE.equals(requirement.getPrimary())
                    || requirement.getRootResultDefinitionId() == null
                    || !matchedPrimaries.contains(requirement.getRootResultDefinitionId())) {
                continue;
            }
            for (final JudicialResult result : defendant.results()) {
                if (requirement.getResultDefinitionId().equals(result.getJudicialResultTypeId())) {
                    additional.add(result.getJudicialResultTypeId());
                }
            }
        }
        return additional;
    }

    private List<NowRequirement> flatten(final List<NowRequirement> requirements) {
        final List<NowRequirement> flat = new ArrayList<>();
        if (requirements != null) {
            for (final NowRequirement requirement : requirements) {
                flat.add(requirement);
                flat.addAll(flatten(requirement.getNowRequirements()));
            }
        }
        return flat;
    }
}