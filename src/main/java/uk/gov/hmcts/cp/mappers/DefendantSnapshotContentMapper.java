package uk.gov.hmcts.cp.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Address;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.CourtCentre;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.Offence;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDefendant;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.PersonDetails;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotAddress;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotDefendant;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotHearing;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotOffence;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotPrompt;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotResult;
import uk.gov.hmcts.cp.services.nowscompute.MergedDefendant;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DefendantSnapshotContentMapper {

    public DefendantSnapshotContent toContent(final UUID hearingId, final MergedDefendant defendant,
                                               final HearingDetail hearing) {
        return DefendantSnapshotContent.builder()
                .defendant(toSnapshotDefendant(defendant.personDefendant()))
                .hearing(toSnapshotHearing(hearingId, hearing))
                .offences(defendant.offences().stream().map(this::toSnapshotOffence).toList())
                .build();
    }

    private SnapshotDefendant toSnapshotDefendant(final PersonDefendant personDefendant) {
        final PersonDetails details = personDefendant == null ? null : personDefendant.getPersonDetails();
        return details == null ? null : SnapshotDefendant.builder()
                .title(details.getTitle())
                .firstName(details.getFirstName())
                .middleName(details.getMiddleName())
                .lastName(details.getLastName())
                .dateOfBirth(details.getDateOfBirth())
                .address(toSnapshotAddress(details.getAddress()))
                .gender(details.getGender())
                .nationality(details.getNationalityDescription())
                .build();
    }

    private SnapshotAddress toSnapshotAddress(final Address address) {
        return address == null ? null : SnapshotAddress.builder()
                .address1(address.getAddress1())
                .address2(address.getAddress2())
                .address3(address.getAddress3())
                .address4(address.getAddress4())
                .address5(address.getAddress5())
                .postCode(address.getPostcode())
                .build();
    }

    private SnapshotHearing toSnapshotHearing(final UUID hearingId, final HearingDetail hearing) {
        final CourtCentre courtCentre = hearing.getCourtCentre();
        return SnapshotHearing.builder()
                .id(hearingId)
                .courtHouseCode(courtCentre == null ? null : courtCentre.getCode())
                .courtHouseName(courtCentre == null ? null : courtCentre.getName())
                .ljaName(courtCentre == null || courtCentre.getLja() == null ? null : courtCentre.getLja().getLjaName())
                .hearingDate(firstSittingDay(hearing))
                .jurisdiction(hearing.getJurisdictionType())
                .build();
    }

    private LocalDate firstSittingDay(final HearingDetail hearing) {
        return hearing.getHearingDays() == null || hearing.getHearingDays().isEmpty()
                ? null
                : parseDate(hearing.getHearingDays().get(0).getSittingDay()).orElse(null);
    }

    private Optional<LocalDate> parseDate(final String sittingDay) {
        Optional<LocalDate> parsed;
        try {
            parsed = sittingDay == null ? Optional.empty() : Optional.of(LocalDate.parse(sittingDay));
        } catch (DateTimeParseException e) {
            parsed = Optional.empty();
        }
        return parsed;
    }

    private SnapshotOffence toSnapshotOffence(final Offence offence) {
        return SnapshotOffence.builder()
                .code(offence.getOffenceCode())
                .title(offence.getOffenceTitle())
                .wording(offence.getWording())
                .convictionDate(offence.getConvictionDate())
                .results(nullSafe(offence.getJudicialResults()).stream().map(this::toSnapshotResult).toList())
                .build();
    }

    private SnapshotResult toSnapshotResult(final JudicialResult result) {
        return SnapshotResult.builder()
                .judicialResultTypeId(result.getJudicialResultTypeId())
                .cjsCode(result.getCjsCode())
                .label(result.getLabel())
                .orderedDate(result.getOrderedDate())
                .prompts(nullSafe(result.getJudicialResultPrompts()).stream().map(this::toSnapshotPrompt).toList())
                .build();
    }

    private SnapshotPrompt toSnapshotPrompt(final JudicialResultPrompt prompt) {
        return SnapshotPrompt.builder()
                .promptReference(prompt.getPromptReference())
                .label(prompt.getLabel())
                .value(prompt.getValue())
                .build();
    }

    private <T> List<T> nullSafe(final List<T> list) {
        return list == null ? List.of() : list;
    }
}