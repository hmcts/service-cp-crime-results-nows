package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefendantCaseRepositoryTest extends RepositoryIntegrationTestBase {

    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID DEFENDANT_CASE_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private DefendantRepository defendantRepository;

    @Autowired
    private DefendantCaseRepository defendantCaseRepository;

    @Transactional
    @Test
    void findByCaseUrnAndDefendantId_should_returnEntity_whenMatchExists() {
        final UUID defendantRowId = aSavedDefendant();
        defendantCaseRepository.save(DefendantCaseEntity.builder()
                .id(DEFENDANT_CASE_ROW_ID)
                .defendantRowId(defendantRowId)
                .caseUrn("RC363968376")
                .defendantId("d2151771-41a1-42e1-af36-a99d9b39c0b2")
                .build());

        assertThat(defendantCaseRepository.findByCaseUrnAndDefendantId(
                "RC363968376", "d2151771-41a1-42e1-af36-a99d9b39c0b2"))
                .isPresent();
    }

    @Transactional
    @Test
    void findByCaseUrnAndDefendantId_should_returnEmpty_whenNoMatch() {
        assertThat(defendantCaseRepository.findByCaseUrnAndDefendantId("NOMATCH", "no-match"))
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