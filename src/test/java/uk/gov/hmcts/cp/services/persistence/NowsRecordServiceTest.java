package uk.gov.hmcts.cp.services.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowsRecordServiceTest {

    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MASTER_DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final LocalDate HEARING_DAY = LocalDate.parse("2026-09-02");
    private static final String CASE_URN = "RC363968376";
    private static final String DEFENDANT_ID = "d2151771-41a1-42e1-af36-a99d9b39c0b2";
    private static final String EVENT_TYPE = "WEE_Remand";

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private DefendantCaseRepository defendantCaseRepository;
    @Mock
    private DefendantSnapshotRepository defendantSnapshotRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private NowsRecordMapper recordMapper;
    @Mock
    private DefendantSnapshotContentMapper snapshotContentMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NowsRecordService recordService;

    @Test
    void record_should_insertEveryRow_whenNothingRecordedYet() {
        stubFreshHearingAndDefendant();
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID)).thenReturn(Optional.empty());
        when(defendantSnapshotRepository.findByDefendantRowId(DEFENDANT_ROW_ID)).thenReturn(Optional.empty());
        when(eventRepository.findByDefendantRowIdAndEventType(DEFENDANT_ROW_ID, EVENT_TYPE))
                .thenReturn(Optional.empty());
        when(recordMapper.toDefendantCase(DEFENDANT_ROW_ID, CASE_URN, DEFENDANT_ID))
                .thenReturn(DefendantCaseEntity.builder().build());
        when(recordMapper.toSnapshot(eq(DEFENDANT_ROW_ID), any()))
                .thenReturn(DefendantSnapshotEntity.builder().build());
        when(recordMapper.toEvent(eq(DEFENDANT_ROW_ID), eq(EVENT_TYPE), any()))
                .thenReturn(EventEntity.builder().build());

        recordService.record(HEARING_ID, HEARING_DAY, mergedDefendant(), HearingDetail.builder().build(),
                Set.of(new MatchedEventType(EVENT_TYPE, Set.of("rt-1"))));

        verify(hearingRepository).save(any(HearingEntity.class));
        verify(defendantRepository).save(any(DefendantEntity.class));
        verify(defendantCaseRepository).save(any(DefendantCaseEntity.class));
        verify(defendantSnapshotRepository).save(any(DefendantSnapshotEntity.class));
        verify(eventRepository).save(any(EventEntity.class));
    }

    @Test
    void record_should_notReinsertEvent_whenAlreadyRecordedForTheSameDefendant() {
        stubExistingHearingAndDefendant();
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID))
                .thenReturn(Optional.of(DefendantCaseEntity.builder().build()));
        when(defendantSnapshotRepository.findByDefendantRowId(DEFENDANT_ROW_ID))
                .thenReturn(Optional.of(DefendantSnapshotEntity.builder().build()));
        when(eventRepository.findByDefendantRowIdAndEventType(DEFENDANT_ROW_ID, EVENT_TYPE))
                .thenReturn(Optional.of(EventEntity.builder().build()));

        recordService.record(HEARING_ID, HEARING_DAY, mergedDefendant(), HearingDetail.builder().build(),
                Set.of(new MatchedEventType(EVENT_TYPE, Set.of("rt-1"))));

        verify(hearingRepository, never()).save(any());
        verify(defendantRepository, never()).save(any());
        verify(defendantCaseRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void record_should_refreshSnapshotContent_whenSnapshotAlreadyExists() {
        stubExistingHearingAndDefendant();
        final DefendantSnapshotEntity existing = DefendantSnapshotEntity.builder().build();
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID))
                .thenReturn(Optional.of(DefendantCaseEntity.builder().build()));
        when(defendantSnapshotRepository.findByDefendantRowId(DEFENDANT_ROW_ID)).thenReturn(Optional.of(existing));
        when(recordMapper.withRefreshedContent(eq(existing), any()))
                .thenReturn(DefendantSnapshotEntity.builder().build());

        recordService.record(HEARING_ID, HEARING_DAY, mergedDefendant(), HearingDetail.builder().build(), Set.of());

        verify(defendantSnapshotRepository).save(any(DefendantSnapshotEntity.class));
    }

    private void stubFreshHearingAndDefendant() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.empty());
        when(recordMapper.toHearing(HEARING_ID, HEARING_DAY)).thenReturn(hearingEntity());
        when(hearingRepository.save(any(HearingEntity.class))).thenReturn(hearingEntity());
        when(defendantRepository.findByHearingIdAndMasterDefendantId(HEARING_ROW_ID, MASTER_DEFENDANT_ID))
                .thenReturn(Optional.empty());
        when(recordMapper.toDefendant(HEARING_ROW_ID, MASTER_DEFENDANT_ID)).thenReturn(defendantEntity());
        when(defendantRepository.save(any(DefendantEntity.class))).thenReturn(defendantEntity());
    }

    private void stubExistingHearingAndDefendant() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(hearingEntity()));
        when(defendantRepository.findByHearingIdAndMasterDefendantId(HEARING_ROW_ID, MASTER_DEFENDANT_ID))
                .thenReturn(Optional.of(defendantEntity()));
    }

    private HearingEntity hearingEntity() {
        return HearingEntity.builder().id(HEARING_ROW_ID).hearingId(HEARING_ID).build();
    }

    private DefendantEntity defendantEntity() {
        return DefendantEntity.builder().id(DEFENDANT_ROW_ID).build();
    }

    private MergedDefendant mergedDefendant() {
        return MergedDefendant.builder()
                .masterDefendantId(MASTER_DEFENDANT_ID.toString())
                .cases(List.of(new DefendantCaseLink(CASE_URN, DEFENDANT_ID)))
                .offences(List.of())
                .results(List.of())
                .build();
    }
}