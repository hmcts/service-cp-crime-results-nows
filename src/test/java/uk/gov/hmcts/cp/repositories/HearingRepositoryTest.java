package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HearingRepositoryTest extends RepositoryIntegrationTestBase {

    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID NO_MATCH_HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private HearingRepository hearingRepository;

    @Transactional
    @Test
    void findByHearingId_should_returnEntity_whenMatchExists() {
        hearingRepository.save(HearingEntity.builder()
                .id(HEARING_ROW_ID)
                .hearingId(HEARING_ID)
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(hearingRepository.findByHearingId(HEARING_ID)).isPresent();
    }

    @Transactional
    @Test
    void findByHearingId_should_returnEmpty_whenNoMatch() {
        assertThat(hearingRepository.findByHearingId(NO_MATCH_HEARING_ID)).isEmpty();
    }
}