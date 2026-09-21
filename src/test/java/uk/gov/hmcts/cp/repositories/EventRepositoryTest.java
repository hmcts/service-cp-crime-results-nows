package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventRepositoryTest extends RepositoryIntegrationTestBase {

    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID EVENT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID EVENT_ROW_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID NO_MATCH_DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private DefendantRepository defendantRepository;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    @Test
    void findByDefendantRowIdAndEventType_should_returnEntity_whenMatchExists() {
        final UUID defendantRowId = aSavedDefendant();
        eventRepository.save(EventEntity.builder()
                .id(EVENT_ROW_ID)
                .defendantRowId(defendantRowId)
                .eventType("WEE_CustodialSentence")
                .matchedResultTypeIds("[\"3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11\"]")
                .matchedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(eventRepository.findByDefendantRowIdAndEventType(defendantRowId, "WEE_CustodialSentence"))
                .isPresent();
    }

    @Transactional
    @Test
    void findByDefendantRowId_should_returnEveryEventType_forThatDefendant() {
        final UUID defendantRowId = aSavedDefendant();
        eventRepository.save(EventEntity.builder()
                .id(EVENT_ROW_ID)
                .defendantRowId(defendantRowId)
                .eventType("WEE_CustodialSentence")
                .matchedResultTypeIds("[\"3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11\"]")
                .matchedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        eventRepository.save(EventEntity.builder()
                .id(EVENT_ROW_ID_2)
                .defendantRowId(defendantRowId)
                .eventType("NEE_FootballBanning")
                .matchedResultTypeIds("[\"9a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9\"]")
                .matchedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(eventRepository.findByDefendantRowId(defendantRowId)).hasSize(2);
    }

    @Transactional
    @Test
    void findByDefendantRowIdAndEventType_should_returnEmpty_whenNoMatch() {
        assertThat(eventRepository.findByDefendantRowIdAndEventType(NO_MATCH_DEFENDANT_ROW_ID, "WEE_CustodialSentence"))
                .isEmpty();
    }

    private UUID aSavedDefendant() {
        hearingRepository.save(HearingEntity.builder()
                .id(HEARING_ROW_ID)
                .hearingId(HEARING_ID)
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        defendantRepository.save(DefendantEntity.builder()
                .id(DEFENDANT_ROW_ID)
                .hearingId(HEARING_ROW_ID)
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        return DEFENDANT_ROW_ID;
    }
}