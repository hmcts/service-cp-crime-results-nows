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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "defendant")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DefendantEntity {

    @Id
    private UUID id;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "master_defendant_id")
    private String masterDefendantId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}