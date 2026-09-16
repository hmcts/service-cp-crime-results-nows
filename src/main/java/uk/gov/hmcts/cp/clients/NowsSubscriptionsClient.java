package uk.gov.hmcts.cp.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NowsSubscriptionsClient {

    private static final String ACCEPT_NOW_SUBSCRIPTIONS =
            "application/vnd.referencedata.query.get-now-subscriptions+json";
    // Fixed resource path on the reference-data service — only the host/port (appProperties
    // .getReferenceDataUrl()) is meant to vary per environment, not this path.
    @SuppressWarnings("java:S1075")
    public static final String NOW_SUBSCRIPTIONS_PATH = "/referencedata-query-api/query/api/rest/referencedata/now-subscriptions";

    private final AppPropertiesBackend appProperties;
    private final RestClient restClient;

    public List<NowsSubscription> getNowSubscriptions(final LocalDate activeAt) {
        final String url = buildUrl(activeAt);
        log.info("Getting now-subscriptions from {}", Encode.forJava(url));
        final NowsSubscriptionsResponse response = restClient.get()
                .uri(url)
                .header("Accept", ACCEPT_NOW_SUBSCRIPTIONS)
                .header("CJSCPPUID", appProperties.getReferenceDataCjscppuid())
                .retrieve()
                .body(NowsSubscriptionsResponse.class);
        return response == null || response.getNowSubscriptions() == null
                ? List.of()
                : response.getNowSubscriptions();
    }

    private String buildUrl(final LocalDate activeAt) {
        return UriComponentsBuilder
                .fromUriString(appProperties.getReferenceDataUrl() + NOW_SUBSCRIPTIONS_PATH)
                .queryParam("on", activeAt)
                .toUriString();
    }
}