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

    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID DEFENDANT_SNAPSHOT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID NO_MATCH_DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

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
                .id(DEFENDANT_SNAPSHOT_ROW_ID)
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
        assertThat(defendantSnapshotRepository.findByDefendantRowId(NO_MATCH_DEFENDANT_ROW_ID)).isEmpty();
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