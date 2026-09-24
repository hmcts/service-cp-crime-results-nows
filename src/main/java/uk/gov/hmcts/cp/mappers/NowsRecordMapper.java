package uk.gov.hmcts.cp.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;
import uk.gov.hmcts.cp.services.ClockService;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NowsRecordMapper {

    private final ClockService clockService;

    public HearingEntity toHearing(final UUID hearingId, final LocalDate hearingDay) {
        return HearingEntity.builder()
                .hearingId(hearingId)
                .hearingDay(hearingDay)
                .createdAt(clockService.nowOffsetUTC())
                .build();
    }

    public DefendantEntity toDefendant(final UUID hearingRowId, final UUID masterDefendantId) {
        return DefendantEntity.builder()
                .id(UUID.randomUUID())
                .hearingId(hearingRowId)
                .masterDefendantId(masterDefendantId)
                .createdAt(clockService.nowOffsetUTC())
                .build();
    }

    public DefendantCaseEntity toDefendantCase(final UUID defendantRowId, final String caseUrn, final String defendantId) {
        return DefendantCaseEntity.builder()
                .id(UUID.randomUUID())
                .defendantRowId(defendantRowId)
                .caseUrn(caseUrn)
                .defendantId(defendantId)
                .build();
    }

    public DefendantSnapshotEntity toSnapshot(final UUID defendantRowId, final String content) {
        return DefendantSnapshotEntity.builder()
                .id(UUID.randomUUID())
                .defendantRowId(defendantRowId)
                .content(content)
                .createdAt(clockService.nowOffsetUTC())
                .build();
    }

    public DefendantSnapshotEntity withRefreshedContent(final DefendantSnapshotEntity existing, final String content) {
        return existing.toBuilder().content(content).build();
    }

    public EventEntity toEvent(final UUID defendantRowId, final String eventType, final String matchedResultTypeIds) {
        return EventEntity.builder()
                .id(UUID.randomUUID())
                .defendantRowId(defendantRowId)
                .eventType(eventType)
                .matchedResultTypeIds(matchedResultTypeIds)
                .matchedAt(clockService.nowOffsetUTC())
                .build();
    }
}