package uk.gov.hmcts.cp.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotAddress;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotDefendant;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotHearing;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotOffence;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotPrompt;
import uk.gov.hmcts.cp.domain.snapshot.DefendantSnapshotContent.SnapshotResult;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.Court;
import uk.gov.hmcts.cp.openapi.model.CourtDetails;
import uk.gov.hmcts.cp.openapi.model.Defendant;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;
import uk.gov.hmcts.cp.openapi.model.EventType;
import uk.gov.hmcts.cp.openapi.model.HearingSummary;
import uk.gov.hmcts.cp.openapi.model.JudicialResult;
import uk.gov.hmcts.cp.openapi.model.Offence;
import uk.gov.hmcts.cp.openapi.model.Prompt;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NowsQueryMapper {

    private static final TypeReference<Set<String>> RESULT_TYPE_IDS = new TypeReference<>() { };

    private final ObjectMapper objectMapper;

    public DefendantResult toDefendantResult(final String caseUrn, final UUID defendantId, final String content,
                                              final List<EventEntity> events) {
        final DefendantSnapshotContent snapshot = objectMapper.readValue(content, DefendantSnapshotContent.class);
        return DefendantResult.builder()
                .caseURN(caseUrn)
                .defendant(toDefendant(defendantId, snapshot.defendant()))
                .hearing(toHearingSummary(snapshot.hearing()))
                .eventTypes(events.stream().map(event -> toEventType(event, snapshot.offences())).toList())
                .build();
    }

    private Defendant toDefendant(final UUID defendantId, final SnapshotDefendant defendant) {
        return defendant == null ? Defendant.builder().id(defendantId).build() : Defendant.builder()
                .id(defendantId)
                .title(defendant.title())
                .firstName(defendant.firstName())
                .middleName(defendant.middleName())
                .lastName(defendant.lastName())
                .dateOfBirth(defendant.dateOfBirth())
                .address(toAddress(defendant.address()))
                .gender(defendant.gender())
                .nationality(defendant.nationality())
                .build();
    }

    private Address toAddress(final SnapshotAddress address) {
        return address == null ? null : Address.builder()
                .address1(address.address1())
                .address2(address.address2())
                .address3(address.address3())
                .address4(address.address4())
                .address5(address.address5())
                .postCode(address.postCode())
                .build();
    }

    private HearingSummary toHearingSummary(final SnapshotHearing hearing) {
        return hearing == null ? null : HearingSummary.builder()
                .id(hearing.id())
                .courtDetails(CourtDetails.builder()
                        .court(Court.builder()
                                .courtHouseCode(hearing.courtHouseCode())
                                .courtHouseName(hearing.courtHouseName())
                                .build())
                        .ljaName(hearing.ljaName())
                        .build())
                .hearingDate(hearing.hearingDate())
                .jurisdiction(hearing.jurisdiction())
                .build();
    }

    private EventType toEventType(final EventEntity event, final List<SnapshotOffence> offences) {
        final Set<String> matchedResultTypeIds =
                objectMapper.readValue(event.getMatchedResultTypeIds(), RESULT_TYPE_IDS);
        return EventType.builder()
                .eventType(event.getEventType())
                .matchedAt(event.getMatchedAt() == null ? null : event.getMatchedAt().toInstant())
                .offences(filterOffences(offences, matchedResultTypeIds))
                .build();
    }

    private List<Offence> filterOffences(final List<SnapshotOffence> offences, final Set<String> matchedResultTypeIds) {
        return offences == null ? List.of() : offences.stream()
                .map(offence -> toOffence(offence, matchedResultTypeIds))
                .filter(offence -> !offence.getResults().isEmpty())
                .toList();
    }

    private Offence toOffence(final SnapshotOffence offence, final Set<String> matchedResultTypeIds) {
        final List<SnapshotResult> results = offence.results() == null ? List.of() : offence.results();
        return Offence.builder()
                .code(offence.code())
                .title(offence.title())
                .wording(offence.wording())
                .convictionDate(offence.convictionDate())
                .results(results.stream()
                        .filter(result -> matchedResultTypeIds.contains(result.judicialResultTypeId()))
                        .map(this::toJudicialResult)
                        .toList())
                .build();
    }

    private JudicialResult toJudicialResult(final SnapshotResult result) {
        final List<SnapshotPrompt> prompts = result.prompts() == null ? List.of() : result.prompts();
        return JudicialResult.builder()
                .judicialResultTypeId(UUID.fromString(result.judicialResultTypeId()))
                .cjsCode(result.cjsCode())
                .label(result.label())
                .orderedDate(result.orderedDate())
                .prompts(prompts.stream().map(this::toPrompt).toList())
                .build();
    }

    private Prompt toPrompt(final SnapshotPrompt prompt) {
        return Prompt.builder()
                .promptReference(prompt.promptReference())
                .label(prompt.label())
                .value(prompt.value())
                .build();
    }
}