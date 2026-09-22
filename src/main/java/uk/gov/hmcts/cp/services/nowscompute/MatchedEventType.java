package uk.gov.hmcts.cp.services.nowscompute;

import java.util.Set;

public record MatchedEventType(String eventType, Set<String> matchedResultTypeIds) {
}