package uk.gov.hmcts.cp.services.nowscompute;

import java.util.Set;

/**
 * The fixed, static allow-list of registered NOW event types (design doc §3e) — pruned against
 * before any requirement-tree matching runs. Sourced from
 * {@code GET /event-types} on {@code api-cp-crime-hearing-results-document-subscription}, kept as a
 * named constant here rather than fetched at runtime (ADR-002).
 *
 * <p>Scoped to just {@code WEE_Remand} for this first phase (Part-1: Remand Warrant) — the full
 * 40-item catalogue from the real {@code event_type} table is a follow-up, not yet pulled in.
 */
public final class RegisteredNowEventTypes {

    public static final Set<String> ALLOW_LIST = Set.of("WEE_Remand");

    private RegisteredNowEventTypes() {
    }
}