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
                .id(UUID.randomUUID())
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
                .masterDefendantId(UUID.randomUUID().toString())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build());
        return defendantRowId;
    }
}