package uk.gov.hmcts.cp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.entities.HearingEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HearingRepository extends JpaRepository<HearingEntity, UUID> {

    Optional<HearingEntity> findByHearingId(UUID hearingId);
}