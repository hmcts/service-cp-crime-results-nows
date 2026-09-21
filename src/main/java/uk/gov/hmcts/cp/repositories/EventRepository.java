package uk.gov.hmcts.cp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.entities.EventEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    List<EventEntity> findByDefendantRowId(UUID defendantRowId);

    Optional<EventEntity> findByDefendantRowIdAndEventType(UUID defendantRowId, String eventType);
}