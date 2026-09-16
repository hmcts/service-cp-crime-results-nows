package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefendantSnapshotRepositoryTest extends RepositoryIntegrationTestBase {

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private DefendantRepository defendantRepository;

    @Autowired
    private DefendantSnapshotRepository defendantSnapshotRepository;

    @Transactional
    @Test
    void findByDefendantRowId_should_returnContent_whenMatchExists() {
        final UUID defendantRowId = aSavedDefendant();
        defendantSnapshotRepository.save(DefendantSnapshotEntity.builder()
                .id(UUID.randomUUID())
                .defendantRowId(defendantRowId)
                .content("{\"defendant\":{\"firstName\":\"Example\"}}")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        final var found = defendantSnapshotRepository.findByDefendantRowId(defendantRowId);

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).contains("Example");
    }

    @Transactional
    @Test
    void findByDefendantRowId_should_returnEmpty_whenNoMatch() {
        assertThat(defendantSnapshotRepository.findByDefendantRowId(UUID.randomUUID())).isEmpty();
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