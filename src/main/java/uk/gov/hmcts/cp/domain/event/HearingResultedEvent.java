package uk.gov.hmcts.cp.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Hand-written stand-in for the generated uk.gov.hmcts.cp.openapi.model.HearingResultedEvent that
// service-cp-crime-results-pcr consumes from its published api-cp-crime-results-pcr artifact.
// The equivalent api-cp-crime-results-nows OpenAPI spec/artifact does not exist yet, so this class
// mirrors that generated model's field shape exactly (confirmed via javap against the pcr jar).
// Delete this class and switch to the generated uk.gov.hmcts.cp.openapi.model equivalent once
// api-cp-crime-results-nows is published and added as a build.gradle dependency.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HearingResultedEvent {

    private String id;
    private String eventType;
    private String subject;
    private java.time.Instant eventTime;
    private String dataVersion;
    private String metadataVersion;
    private String topic;
    private HearingResultedEventData data;
}
