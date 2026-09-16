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
@Table(name = "defendant_snapshot")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DefendantSnapshotEntity {

    @Id
    private UUID id;

    @Column(name = "defendant_row_id")
    private UUID defendantRowId;

    // Resolved defendant/hearing/offences content (design doc §5b) — raw JSON text, mapped to the
    // jsonb column type. Not a fully normalized column-per-field schema, deliberately (see the
    // migration's own comment).
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}