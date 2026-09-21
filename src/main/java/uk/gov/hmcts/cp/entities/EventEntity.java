package uk.gov.hmcts.cp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cp_event")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventEntity {

    @Id
    private UUID id;

    @Column(name = "defendant_row_id")
    private UUID defendantRowId;

    @Column(name = "event_type")
    private String eventType;

    // judicialResultTypeId list that made this event type eligible (design doc §5c) — raw JSON
    // text, mapped to the jsonb column type. Lets the Query API filter defendant_snapshot.content
    // per event type at read time without re-running the requirement-tree match.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_result_type_ids")
    private String matchedResultTypeIds;

    @Column(name = "matched_at")
    private OffsetDateTime matchedAt;
}