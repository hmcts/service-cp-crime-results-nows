package uk.gov.hmcts.cp.services.query;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.mappers.NowsQueryMapper;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;
import uk.gov.hmcts.cp.repositories.DefendantCaseRepository;
import uk.gov.hmcts.cp.repositories.DefendantRepository;
import uk.gov.hmcts.cp.repositories.DefendantSnapshotRepository;
import uk.gov.hmcts.cp.repositories.EventRepository;
import uk.gov.hmcts.cp.repositories.HearingRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NowsQueryService {

    private final HearingRepository hearingRepository;
    private final DefendantRepository defendantRepository;
    private final DefendantCaseRepository defendantCaseRepository;
    private final DefendantSnapshotRepository defendantSnapshotRepository;
    private final EventRepository eventRepository;
    private final NowsQueryMapper queryMapper;

    @Transactional(readOnly = true)
    public DefendantResult getDefendantResult(final String caseUrn, final UUID hearingId, final UUID defendantId,
                                               final String eventType) {
        if (!hearingRepository.existsById(hearingId)) {
            throw new EntityNotFoundException("No hearing ingested for the supplied hearingId");
        }
        final DefendantEntity defendant = resolveDefendant(caseUrn, defendantId, hearingId);
        final DefendantSnapshotEntity snapshot = defendantSnapshotRepository.findByDefendantRowId(defendant.getId())
                .orElseThrow(() -> new EntityNotFoundException("No content recorded for the supplied defendant"));
        return queryMapper.toDefendantResult(caseUrn, defendantId, snapshot.getContent(),
                events(defendant.getId(), eventType));
    }

    private DefendantEntity resolveDefendant(final String caseUrn, final UUID defendantId, final UUID hearingId) {
        final DefendantCaseEntity defendantCase =
                defendantCaseRepository.findByCaseUrnAndDefendantId(caseUrn, defendantId.toString())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "No defendant recorded for the supplied caseURN and defendantId"));
        final DefendantEntity defendant = defendantRepository.findById(defendantCase.getDefendantRowId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No defendant recorded for the supplied caseURN and defendantId"));
        if (!defendant.getHearingId().equals(hearingId)) {
            throw new EntityNotFoundException("The supplied defendant was not recorded against the supplied hearing");
        }
        return defendant;
    }

    private List<EventEntity> events(final UUID defendantRowId, final String eventType) {
        final List<EventEntity> events = eventRepository.findByDefendantRowId(defendantRowId);
        return eventType == null
                ? events
                : events.stream().filter(event -> eventType.equals(event.getEventType())).toList();
    }
}