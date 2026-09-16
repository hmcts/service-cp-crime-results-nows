package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefendantRepositoryTest extends RepositoryIntegrationTestBase {

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private DefendantRepository defendantRepository;

    @Transactional
    @Test
    void findByHearingIdAndMasterDefendantId_should_returnEntity_whenMatchExists() {
        final UUID hearingRowId = aSavedHearing();
        final UUID masterDefendantId = UUID.randomUUID();
        defendantRepository.save(DefendantEntity.builder()
                .id(UUID.randomUUID())
                .hearingId(hearingRowId)
                .masterDefendantId(masterDefendantId)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(defendantRepository.findByHearingIdAndMasterDefendantId(hearingRowId, masterDefendantId))
                .isPresent();
    }

    @Transactional
    @Test
    void findByHearingIdAndMasterDefendantId_should_returnEmpty_whenNoMatch() {
        assertThat(defendantRepository.findByHearingIdAndMasterDefendantId(UUID.randomUUID(), UUID.randomUUID()))
                .isEmpty();
    }

    @Transactional
    @Test
    void findByHearingId_should_returnEveryDefendant_onThatHearing() {
        final UUID hearingRowId = aSavedHearing();
        defendantRepository.save(DefendantEntity.builder()
                .id(UUID.randomUUID())
                .hearingId(hearingRowId)
                .masterDefendantId(UUID.randomUUID())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        defendantRepository.save(DefendantEntity.builder()
                .id(UUID.randomUUID())
                .hearingId(hearingRowId)
                .masterDefendantId(UUID.randomUUID())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(defendantRepository.findByHearingId(hearingRowId)).hasSize(2);
    }

    private UUID aSavedHearing() {
        final UUID id = UUID.randomUUID();
        hearingRepository.save(HearingEntity.builder()
                .id(id)
                .hearingId(UUID.randomUUID())
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        return id;
    }
}