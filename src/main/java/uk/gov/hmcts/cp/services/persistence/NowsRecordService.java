package uk.gov.hmcts.cp.services.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.mappers.DefendantSnapshotContentMapper;
import uk.gov.hmcts.cp.mappers.NowsRecordMapper;
import uk.gov.hmcts.cp.repositories.DefendantCaseRepository;
import uk.gov.hmcts.cp.repositories.DefendantRepository;
import uk.gov.hmcts.cp.repositories.DefendantSnapshotRepository;
import uk.gov.hmcts.cp.repositories.EventRepository;
import uk.gov.hmcts.cp.repositories.HearingRepository;
import uk.gov.hmcts.cp.services.nowscompute.DefendantCaseLink;
import uk.gov.hmcts.cp.services.nowscompute.MatchedEventType;
import uk.gov.hmcts.cp.services.nowscompute.MergedDefendant;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NowsRecordService {

    private final HearingRepository hearingRepository;
    private final DefendantRepository defendantRepository;
    private final DefendantCaseRepository defendantCaseRepository;
    private final DefendantSnapshotRepository defendantSnapshotRepository;
    private final EventRepository eventRepository;
    private final NowsRecordMapper recordMapper;
    private final DefendantSnapshotContentMapper snapshotContentMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(final UUID hearingId, final LocalDate hearingDay, final MergedDefendant defendant,
                        final HearingDetail hearing, final Set<MatchedEventType> eventTypes) {
        upsertHearing(hearingId, hearingDay);
        final DefendantEntity defendantRow = upsertDefendant(hearingId, defendant.masterDefendantId());
        defendant.cases().forEach(link -> upsertDefendantCase(defendantRow.getId(), link));
        upsertSnapshot(defendantRow.getId(), hearingId, defendant, hearing);
        eventTypes.forEach(eventType -> upsertEvent(defendantRow.getId(), eventType));
    }

    private void upsertHearing(final UUID hearingId, final LocalDate hearingDay) {
        if (!hearingRepository.existsById(hearingId)) {
            hearingRepository.save(recordMapper.toHearing(hearingId, hearingDay));
        }
    }

    private DefendantEntity upsertDefendant(final UUID hearingRowId, final String masterDefendantId) {
        final UUID masterId = UUID.fromString(masterDefendantId);
        return defendantRepository.findByHearingIdAndMasterDefendantId(hearingRowId, masterId)
                .orElseGet(() -> defendantRepository.save(recordMapper.toDefendant(hearingRowId, masterId)));
    }

    private void upsertDefendantCase(final UUID defendantRowId, final DefendantCaseLink link) {
        defendantCaseRepository.findByCaseUrnAndDefendantId(link.caseUrn(), link.defendantId())
                .orElseGet(() -> defendantCaseRepository.save(
                        recordMapper.toDefendantCase(defendantRowId, link.caseUrn(), link.defendantId())));
    }

    // Re-delivery can carry a refreshed payload, so the snapshot is updated rather than left as-is
    // (design doc §4).
    private void upsertSnapshot(final UUID defendantRowId, final UUID hearingId, final MergedDefendant defendant,
                                 final HearingDetail hearing) {
        final String content = objectMapper.writeValueAsString(
                snapshotContentMapper.toContent(hearingId, defendant, hearing));
        final DefendantSnapshotEntity snapshot = defendantSnapshotRepository.findByDefendantRowId(defendantRowId)
                .map(existing -> recordMapper.withRefreshedContent(existing, content))
                .orElseGet(() -> recordMapper.toSnapshot(defendantRowId, content));
        defendantSnapshotRepository.save(snapshot);
    }

    private void upsertEvent(final UUID defendantRowId, final MatchedEventType eventType) {
        if (eventRepository.findByDefendantRowIdAndEventType(defendantRowId, eventType.eventType()).isPresent()) {
            log.info("Event type already recorded for defendantRowId:{} eventType:{} — skipping",
                    defendantRowId, eventType.eventType());
            return;
        }
        final String matchedResultTypeIds = objectMapper.writeValueAsString(eventType.matchedResultTypeIds());
        eventRepository.save(recordMapper.toEvent(defendantRowId, eventType.eventType(), matchedResultTypeIds));
    }
}