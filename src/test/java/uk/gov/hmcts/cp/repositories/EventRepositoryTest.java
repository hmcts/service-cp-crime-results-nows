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
                .id(UUID.randomUUID())
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
                .id(UUID.randomUUID())
                .defendantRowId(defendantRowId)
                .eventType("WEE_CustodialSentence")
                .matchedResultTypeIds("[\"3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11\"]")
                .matchedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        eventRepository.save(EventEntity.builder()
                .id(UUID.randomUUID())
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
        assertThat(eventRepository.findByDefendantRowIdAndEventType(UUID.randomUUID(), "WEE_CustodialSentence"))
                .isEmpty();
    }

    private UUID aSavedDefendant() {
        final UUID hearingRowId = UUID.randomUUID();
        hearingRepository.save(HearingEntity.builder()
                .id(hearingRowId)
                .hearingId(UUID.randomUUID())
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        final UUID defendantRowId = UUID.randomUUID();
        defendantRepository.save(DefendantEntity.builder()
                .id(defendantRowId)
                .hearingId(hearingRowId)
                .masterDefendantId(UUID.randomUUID())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        return defendantRowId;
    }
}