package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.services.ClockService;
import uk.gov.hmcts.cp.services.nowscompute.MatchedEventType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NowsRecordMapperTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-09-02T18:05:00Z");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Spy
    private ClockService clockService = new ClockService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));

    @InjectMocks
    private NowsRecordMapper mapper;

    @Test
    void toEvent_should_mapMatchedEventType() {
        final MatchedEventType matched = new MatchedEventType("WEE_CustodialSentence",
                "b4b55110-1d50-11e8-accf-0ed5f89f718b", "Warrant for Custodial Sentence", Set.of("rt-1"));

        final EventEntity event = mapper.toEvent(DEFENDANT_ROW_ID, matched, "[\"rt-1\"]");

        assertThat(event.getDefendantRowId()).isEqualTo(DEFENDANT_ROW_ID);
        assertThat(event.getEventType()).isEqualTo("WEE_CustodialSentence");
        assertThat(event.getOrderName()).isEqualTo("Warrant for Custodial Sentence");
        assertThat(event.getMatchedResultTypeIds()).isEqualTo("[\"rt-1\"]");
        assertThat(event.getMatchedAt()).isEqualTo(FIXED_NOW.atOffset(ZoneOffset.UTC));
    }
}
