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

    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID DEFENDANT_ROW_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID MASTER_DEFENDANT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID NO_MATCH_HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000098");
    private static final UUID NO_MATCH_MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private DefendantRepository defendantRepository;

    @Transactional
    @Test
    void findByHearingIdAndMasterDefendantId_should_returnEntity_whenMatchExists() {
        final UUID hearingId = aSavedHearing();
        defendantRepository.save(DefendantEntity.builder()
                .id(DEFENDANT_ROW_ID)
                .hearingId(hearingId)
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(defendantRepository.findByHearingIdAndMasterDefendantId(hearingId, MASTER_DEFENDANT_ID))
                .isPresent();
    }

    @Transactional
    @Test
    void findByHearingIdAndMasterDefendantId_should_returnEmpty_whenNoMatch() {
        assertThat(defendantRepository.findByHearingIdAndMasterDefendantId(
                NO_MATCH_HEARING_ID, NO_MATCH_MASTER_DEFENDANT_ID))
                .isEmpty();
    }

    @Transactional
    @Test
    void findByHearingId_should_returnEveryDefendant_onThatHearing() {
        final UUID hearingId = aSavedHearing();
        defendantRepository.save(DefendantEntity.builder()
                .id(DEFENDANT_ROW_ID)
                .hearingId(hearingId)
                .masterDefendantId(MASTER_DEFENDANT_ID)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        defendantRepository.save(DefendantEntity.builder()
                .id(DEFENDANT_ROW_ID_2)
                .hearingId(hearingId)
                .masterDefendantId(MASTER_DEFENDANT_ID_2)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());

        assertThat(defendantRepository.findByHearingId(hearingId)).hasSize(2);
    }

    private UUID aSavedHearing() {
        hearingRepository.save(HearingEntity.builder()
                .hearingId(HEARING_ID)
                .hearingDay(LocalDate.of(2026, 9, 2))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        return HEARING_ID;
    }
}