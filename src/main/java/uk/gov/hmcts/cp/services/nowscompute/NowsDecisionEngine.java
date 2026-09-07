package uk.gov.hmcts.cp.services.nowscompute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;

import java.util.Set;

/**
 * The NOW generation gate — decides which registered NOW event type(s) would be generated for a
 * hearing/defendant (ADR-002: "NOW generation-gate scope, event-type matching, and record
 * keying"). Deliberately a stub for now: no business logic, always reports no eligible event
 * types. Wired into {@link uk.gov.hmcts.cp.services.ingestion.NowsIngestionService} so the
 * end-to-end shape is in place ahead of the real implementation.
 *
 * <p>ADR-002 describes the real algorithm this will need, in order:
 * <ol>
 *     <li>Compute the defendant's vocabulary (custody location, custodial-outcome,
 *     CPS-prosecution, age group, court language, attendance, major-creditor status), merged
 *     across every prosecution case/court application sharing the defendant's masterDefendantId
 *     on the hearing.</li>
 *     <li>Fetch the NOW-definition catalogue (a new "nows-metadata" reference-data client) and
 *     prune it to the fixed, static 40-item allow-list <em>before</em> any requirement-tree
 *     matching runs.</li>
 *     <li>Match the hearing's judicial-result type identifiers against the pruned candidates'
 *     requirement trees, to determine which event type(s) apply.</li>
 *     <li>Fetch active NOW subscriptions (a new "now-subscriptions" reference-data client) and
 *     match each candidate event type's vocabulary/include-exclude rules against the computed
 *     vocabulary.</li>
 *     <li>Return the event type(s) that survive both matches — zero, one, or several.</li>
 * </ol>
 */
@Slf4j
@Service
public class NowsDecisionEngine {

    public Set<String> determineEligibleEventTypes(final Defendant defendant, final HearingDetail hearing) {
        log.info("NowsDecisionEngine stub — no generation-gate logic implemented yet (ADR-002); "
                + "reporting no eligible event types for defendantId:{}", defendant.getId());
        return Set.of();
    }
}
