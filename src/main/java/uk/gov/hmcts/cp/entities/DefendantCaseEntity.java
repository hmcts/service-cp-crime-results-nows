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

import java.util.UUID;

@Entity
@Table(name = "defendant_case")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DefendantCaseEntity {

    @Id
    private UUID id;

    @Column(name = "defendant_row_id")
    private UUID defendantRowId;

    @Column(name = "case_urn")
    private String caseUrn;

    @Column(name = "defendant_id")
    private String defendantId;
}