package uk.gov.hmcts.cp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Trimmed to the Results Query Client's own config for now. Add reference-data-client fields
// back here (mirroring service-cp-crime-results-pcr's AppPropertiesBackend) once the decision
// engine needs the nows-metadata/now-subscriptions reference-data clients (ADR-002).
@Service
@Getter
public class AppPropertiesBackend {

    private final String resultsQueryUrl;
    private final String resultsQueryCjscppuid;

    public AppPropertiesBackend(
            @Value("${results-query-client.url}") final String resultsQueryUrl,
            @Value("${results-query-client.cjscppuid}") final String resultsQueryCjscppuid) {
        this.resultsQueryUrl = resultsQueryUrl;
        this.resultsQueryCjscppuid = resultsQueryCjscppuid;
    }
}
