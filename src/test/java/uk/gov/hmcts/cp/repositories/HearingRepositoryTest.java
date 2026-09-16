package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HearingRepositoryTest extends RepositoryIntegrationTestBase {

    @Autowired
    private HearingRepository hearingRepository;

    @Transactional
    @Test
    void findByHearingId_should_returnEntity_whenMatchExists() {
        final UUID hearingId = UUID.randomUUID();
        hearingRepository.save(HearingEntity.builder()
                .id(UUID.randomUUID())
                .hearingId(hearingId)
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(hearingRepository.findByHearingId(hearingId)).isPresent();
    }

    @Transactional
    @Test
    void findByHearingId_should_returnEmpty_whenNoMatch() {
        assertThat(hearingRepository.findByHearingId(UUID.randomUUID())).isEmpty();
    }
}