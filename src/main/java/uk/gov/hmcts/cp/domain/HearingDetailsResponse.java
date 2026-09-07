package uk.gov.hmcts.cp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// Same hearingDetails/internal contract service-cp-crime-results-pcr already consumes (ADR-001) —
// this model is carried over from that service's own domain.HearingDetailsResponse rather than
// re-derived from scratch. Field-level rationale comments below describe the shared upstream
// payload shape, confirmed against real fixtures/legacy source in that service; they are not
// re-verified here independently.
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HearingDetailsResponse {

    private HearingDetail hearing;
    // Sibling of hearing in the upstream payload, not nested under it — a possible
    // version-correlation input for a future reshare/amendment design (NOWS design doc §7/§13
    // item 5), not yet used to correlate anything.
    private Instant sharedTime;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class HearingDetail {
        private CourtCentre courtCentre;
        private List<HearingDay> hearingDays;
        private List<ProsecutionCase> prosecutionCases;
        private List<CourtApplication> courtApplications;
        private HearingType type;
        private String jurisdictionType;
        private List<DefendantAttendance> defendantAttendance;
        // Hearing-wide defendant-level results, matched by masterDefendantId — a distinct concept
        // from Defendant.defendantCaseJudicialResults (which is per-defendant nested, not
        // hearing-wide). This masterDefendantId keying is exactly the merge key NOWS's own
        // generation-gate vocabulary computation needs (ADR-002) — a record is keyed by
        // (hearingId, masterDefendantId), not a raw per-case defendantId.
        private List<DefendantJudicialResult> defendantJudicialResults;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class DefendantJudicialResult {
        private String masterDefendantId;
        private JudicialResult judicialResult;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class DefendantAttendance {
        private String defendantId;
        private List<AttendanceDay> attendanceDays;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class AttendanceDay {
        private String day;
        private String attendanceType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class HearingType {
        private String id;
        private String description;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtCentre {
        private String id;
        private String code;
        private String name;
        // Boxed, not primitive — not confirmed present on every real hearingDetails/internal
        // response; a missing field must not fail deserialization of the whole payload.
        private Boolean welshCourtCentre;
        private LocalJusticeArea lja;
        private Address address;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class LocalJusticeArea {
        private String ljaName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class HearingDay {
        private String sittingDay;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ProsecutionCase {
        private String id;
        private ProsecutionCaseIdentifier prosecutionCaseIdentifier;
        private List<CaseMarker> caseMarkers;
        private List<Defendant> defendants;
        private Prosecutor prosecutor;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ProsecutionCaseIdentifier {
        private String caseURN;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Prosecutor {
        // Boxed, not primitive — see CourtCentre.welshCourtCentre for why. Feeds the
        // CPS-prosecution vocabulary dimension (ADR-002).
        private Boolean isCps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CaseMarker {
        private String markerTypeCode;
        private String markerTypeDescription;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Defendant {
        private String id;
        // The merge key ADR-002 records are keyed by, alongside hearingId — one physical
        // defendant can span multiple prosecution-case defendantIds/court applications on the
        // same hearing; NOWS's vocabulary computation and NOW-definition matching are both
        // evaluated against the merged view this identifies.
        private String masterDefendantId;
        // Youth/adult age-group vocabulary dimension source (ADR-002) — boxed, not primitive,
        // see CourtCentre.welshCourtCentre for why.
        private Boolean isYouth;
        private PersonDefendant personDefendant;
        private List<Offence> offences;
        // Case-level results attached directly to the defendant, not tied to any specific
        // offence — a distinct concept from offences[].judicialResults.
        private List<JudicialResult> defendantCaseJudicialResults;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PersonDefendant {
        private CustodialEstablishment custodialEstablishment;
        private PersonDetails personDetails;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CustodialEstablishment {
        private String id;
        private String name;
        private String custody;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PersonDetails {
        private String title;
        private String firstName;
        private String middleName;
        private String lastName;
        private LocalDate dateOfBirth;
        private Address address;
        private String gender;
        private String nationalityDescription;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Address {
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String address5;
        private String postcode;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Offence {
        private String id;
        private String offenceCode;
        private String offenceTitle;
        private String wording;
        private Integer listingNumber;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate convictionDate;
        private PleaDetails plea;
        private List<JudicialResult> judicialResults;
        private String offenceLegislation;
        private Verdict verdict;
        private AllocationDecision allocationDecision;
        private IndicatedPlea indicatedPlea;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class AllocationDecision {
        private String motReasonDescription;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class IndicatedPlea {
        private String indicatedPleaValue;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Verdict {
        private VerdictType verdictType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class VerdictType {
        private String verdictCode;
        private String description;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PleaDetails {
        private String pleaValue;
        private LocalDate pleaDate;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class JudicialResult {
        private String cjsCode;
        private String label;
        private String resultText;
        private String category;
        private String postHearingCustodyStatus;
        private boolean isFinancialResult;
        private boolean isConvictedResult;
        // publishedForNows: carried over from the shared hearingDetails/internal contract — its
        // exact meaning/reuse for this service's own generation gate (ADR-002) is not yet
        // confirmed; do not assume it's equivalent to "already NOW-generation-eligible" without
        // re-validating against a real fixture.
        private Boolean publishedForNows;
        // orderedDate: candidate source for a hearing-wide "active at" date, mirroring
        // service-cp-crime-results-pcr's own use of this field — not yet needed by NOWS's
        // decision-engine stub, kept for parity with the shared contract.
        private LocalDate orderedDate;
        private NextHearing nextHearing;
        private List<JudicialResultPrompt> judicialResultPrompts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NextHearing {
        private String bookingReference;
        private Instant listedStartDateTime;
        private CourtCentre courtCentre;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class JudicialResultPrompt {
        private String promptReference;
        private String value;
        private String label;
        private String type;
    }

    // Court applications are hearing-level, not nested per-defendant. `subject` is the only party
    // role modelled here — it's what a defendant-linkage/vocabulary merge needs
    // (subject.masterDefendant.masterDefendantId). The real payload also carries
    // respondents[]/applicant, serving separate document-mapping/CPS-eligibility concerns not in
    // this service's scope, so neither is modelled here.
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtApplication {
        private String id;
        private String applicationReference;
        private ApplicationType type;
        private ApplicationParty subject;
        private List<CourtApplicationCase> courtApplicationCases;
        private List<JudicialResult> judicialResults;
        private CourtOrder courtOrder;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtOrder {
        private List<CourtOrderOffence> courtOrderOffences;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtOrderOffence {
        private Offence offence;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ApplicationType {
        private String code;
        private String type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ApplicationParty {
        private MasterDefendant masterDefendant;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MasterDefendant {
        private String masterDefendantId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtApplicationCase {
        private List<Offence> offences;
    }
}
