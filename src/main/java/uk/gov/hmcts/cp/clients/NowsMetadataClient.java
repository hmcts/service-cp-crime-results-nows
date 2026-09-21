package uk.gov.hmcts.cp.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse;
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NowsMetadataClient {

    private static final String ACCEPT_NOWS_METADATA =
            "application/vnd.referencedata.get-nows-metadata+json";
    // Fixed resource path on the reference-data service — only the host/port (appProperties
    // .getReferenceDataUrl()) is meant to vary per environment, not this path.
    @SuppressWarnings("java:S1075")
    public static final String NOWS_METADATA_PATH = "/referencedata-query-api/query/api/rest/referencedata/nows-metadata";

    private final AppPropertiesBackend appProperties;
    private final RestClient restClient;

    public List<NowDefinition> getNowDefinitions(final LocalDate activeAt) {
        final String url = buildUrl(activeAt);
        log.info("Getting nows-metadata from {}", Encode.forJava(url));
        final NowMetadataResponse response = restClient.get()
                .uri(url)
                .header("Accept", ACCEPT_NOWS_METADATA)
                .header("CJSCPPUID", appProperties.getReferenceDataCjscppuid())
                .retrieve()
                .body(NowMetadataResponse.class);
        return response == null || response.getNows() == null ? List.of() : response.getNows();
    }

    private String buildUrl(final LocalDate activeAt) {
        return UriComponentsBuilder
                .fromUriString(appProperties.getReferenceDataUrl() + NOWS_METADATA_PATH)
                .queryParam("on", activeAt)
                .toUriString();
    }
}