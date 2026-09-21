package uk.gov.hmcts.cp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.entities.DefendantEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefendantRepository extends JpaRepository<DefendantEntity, UUID> {

    Optional<DefendantEntity> findByHearingIdAndMasterDefendantId(UUID hearingId, UUID masterDefendantId);

    List<DefendantEntity> findByHearingId(UUID hearingId);
}