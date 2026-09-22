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
        if (Boolean.TRUE.equals(definition.getIncludeAllResults())) {
            final Set<String> all = new LinkedHashSet<>();
            for (final JudicialResult result : defendant.results()) {
                if (result.getJudicialResultTypeId() != null) {
                    all.add(result.getJudicialResultTypeId());
                }
            }
            return all;
        }

        final List<NowRequirement> flattened = flatten(definition.getNowRequirements());
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
        if (matched.isEmpty()) {
            return matched;
        }

        // Non-primary children rooted under a matched primary requirement are included too.
        for (final NowRequirement requirement : flattened) {
            if (Boolean.TRUE.equals(requirement.getPrimary())
                    || requirement.getRootResultDefinitionId() == null
                    || !matched.contains(requirement.getRootResultDefinitionId())) {
                continue;
            }
            for (final JudicialResult result : defendant.results()) {
                if (requirement.getResultDefinitionId().equals(result.getJudicialResultTypeId())) {
                    matched.add(result.getJudicialResultTypeId());
                }
            }
        }
        return matched;
    }

    private List<NowRequirement> flatten(final List<NowRequirement> requirements) {
        final List<NowRequirement> flat = new ArrayList<>();
        if (requirements == null) {
            return flat;
        }
        for (final NowRequirement requirement : requirements) {
            flat.add(requirement);
            flat.addAll(flatten(requirement.getNowRequirements()));
        }
        return flat;
    }
}