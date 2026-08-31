package uk.gov.hmcts.cp.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

// See HearingResultedEvent for why this is hand-written rather than generated.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HearingResultedEventData {

    private UUID hearingId;
    private LocalDate hearingDay;
    private UUID userId;
}
