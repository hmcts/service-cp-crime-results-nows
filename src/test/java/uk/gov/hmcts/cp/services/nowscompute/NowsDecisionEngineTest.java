package uk.gov.hmcts.cp.services.nowscompute;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Defendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Documents current (stub) behaviour only — no generation-gate logic implemented yet (ADR-002).
// Replace/expand these assertions once the real matching algorithm lands.
class NowsDecisionEngineTest {

    private final NowsDecisionEngine decisionEngine = new NowsDecisionEngine();

    @Test
    void determineEligibleEventTypes_should_returnEmptySet_alwaysUntilImplemented() {
        final Defendant defendant = Defendant.builder().id("11111111-1111-1111-1111-111111111111").build();
        final HearingDetail hearing = HearingDetail.builder().build();

        final Set<String> result = decisionEngine.determineEligibleEventTypes(defendant, hearing);

        assertThat(result).isEmpty();
    }
}
