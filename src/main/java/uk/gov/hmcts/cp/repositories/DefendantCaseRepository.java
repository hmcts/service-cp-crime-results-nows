package uk.gov.hmcts.cp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefendantCaseRepository extends JpaRepository<DefendantCaseEntity, UUID> {

    Optional<DefendantCaseEntity> findByCaseUrnAndDefendantId(String caseUrn, String defendantId);
}