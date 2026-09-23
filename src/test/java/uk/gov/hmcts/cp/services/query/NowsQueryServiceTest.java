package uk.gov.hmcts.cp.services.query;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.DefendantCaseEntity;
import uk.gov.hmcts.cp.entities.DefendantEntity;
import uk.gov.hmcts.cp.entities.DefendantSnapshotEntity;
import uk.gov.hmcts.cp.entities.EventEntity;
import uk.gov.hmcts.cp.entities.HearingEntity;
import uk.gov.hmcts.cp.mappers.NowsQueryMapper;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;
import uk.gov.hmcts.cp.repositories.DefendantCaseRepository;
import uk.gov.hmcts.cp.repositories.DefendantRepository;
import uk.gov.hmcts.cp.repositories.DefendantSnapshotRepository;
import uk.gov.hmcts.cp.repositories.EventRepository;
import uk.gov.hmcts.cp.repositories.HearingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowsQueryServiceTest {

    private static final String CASE_URN = "RC363968376";
    private static final UUID HEARING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DEFENDANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID DEFENDANT_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID OTHER_HEARING_ROW_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final String CONTENT = "{}";

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
    private NowsQueryMapper queryMapper;

    @InjectMocks
    private NowsQueryService queryService;

    @Test
    void getDefendantResult_should_returnMappedResult_whenHearingAndDefendantKnown() {
        stubResolvedDefendant();
        final DefendantResult expected = DefendantResult.builder().caseURN(CASE_URN).build();
        when(eventRepository.findByDefendantRowId(DEFENDANT_ROW_ID)).thenReturn(List.of(event("WEE_Remand")));
        when(queryMapper.toDefendantResult(eq(CASE_URN), eq(DEFENDANT_ID), eq(CONTENT), any())).thenReturn(expected);

        final DefendantResult result = queryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getDefendantResult_should_throwEntityNotFound_whenHearingNeverIngested() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getDefendantResult_should_throwEntityNotFound_whenCaseUrnDefendantPairUnknown() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(hearingEntity()));
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID.toString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getDefendantResult_should_throwEntityNotFound_whenDefendantBelongsToAnotherHearing() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(hearingEntity()));
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID.toString()))
                .thenReturn(Optional.of(defendantCaseEntity()));
        when(defendantRepository.findById(DEFENDANT_ROW_ID))
                .thenReturn(Optional.of(defendantEntity(OTHER_HEARING_ROW_ID)));

        assertThatThrownBy(() -> queryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getDefendantResult_should_narrowToRequestedEventType_whenEventTypeFilterSupplied() {
        stubResolvedDefendant();
        when(eventRepository.findByDefendantRowId(DEFENDANT_ROW_ID))
                .thenReturn(List.of(event("WEE_Remand"), event("WEE_CustodialSentence")));
        when(queryMapper.toDefendantResult(eq(CASE_URN), eq(DEFENDANT_ID), eq(CONTENT),
                argThat(events -> events.size() == 1 && "WEE_Remand".equals(events.get(0).getEventType()))))
                .thenReturn(DefendantResult.builder().build());

        final DefendantResult result =
                queryService.getDefendantResult(CASE_URN, HEARING_ID, DEFENDANT_ID, "WEE_Remand");

        assertThat(result).isNotNull();
    }

    private void stubResolvedDefendant() {
        when(hearingRepository.findByHearingId(HEARING_ID)).thenReturn(Optional.of(hearingEntity()));
        when(defendantCaseRepository.findByCaseUrnAndDefendantId(CASE_URN, DEFENDANT_ID.toString()))
                .thenReturn(Optional.of(defendantCaseEntity()));
        when(defendantRepository.findById(DEFENDANT_ROW_ID)).thenReturn(Optional.of(defendantEntity(HEARING_ROW_ID)));
        when(defendantSnapshotRepository.findByDefendantRowId(DEFENDANT_ROW_ID))
                .thenReturn(Optional.of(DefendantSnapshotEntity.builder().content(CONTENT).build()));
    }

    private HearingEntity hearingEntity() {
        return HearingEntity.builder().id(HEARING_ROW_ID).hearingId(HEARING_ID).build();
    }

    private DefendantCaseEntity defendantCaseEntity() {
        return DefendantCaseEntity.builder().defendantRowId(DEFENDANT_ROW_ID).build();
    }

    private DefendantEntity defendantEntity(final UUID hearingRowId) {
        return DefendantEntity.builder().id(DEFENDANT_ROW_ID).hearingId(hearingRowId).build();
    }

    private EventEntity event(final String eventType) {
        return EventEntity.builder().eventType(eventType).build();
    }
}