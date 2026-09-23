package uk.gov.hmcts.cp.domain.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record DefendantSnapshotContent(
        SnapshotDefendant defendant,
        SnapshotHearing hearing,
        List<SnapshotOffence> offences) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotDefendant(
            String title,
            String firstName,
            String middleName,
            String lastName,
            LocalDate dateOfBirth,
            SnapshotAddress address,
            String gender,
            String nationality) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotAddress(
            String address1,
            String address2,
            String address3,
            String address4,
            String address5,
            String postCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotHearing(
            UUID id,
            String courtHouseCode,
            String courtHouseName,
            String ljaName,
            LocalDate hearingDate,
            String jurisdiction) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotOffence(
            String code,
            String title,
            String wording,
            LocalDate convictionDate,
            List<SnapshotResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotResult(
            String judicialResultTypeId,
            String cjsCode,
            String label,
            LocalDate orderedDate,
            List<SnapshotPrompt> prompts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record SnapshotPrompt(
            String promptReference,
            String label,
            String value) {
    }
}