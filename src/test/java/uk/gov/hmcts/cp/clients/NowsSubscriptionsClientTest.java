package uk.gov.hmcts.cp.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.nowscompute.NowsSubscriptionsResponse.NowsSubscription;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

class NowsSubscriptionsClientTest {

    private static final LocalDate ACTIVE_AT = LocalDate.of(2026, 9, 2);

    private WireMockServer wireMockServer;
    private NowsSubscriptionsClient nowsSubscriptionsClient;

    @BeforeEach
    void beforeEach() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);

        final AppPropertiesBackend appProperties = new AppPropertiesBackend(
                "http://localhost:8081", "00000000-0000-0000-0000-000000000000",
                "http://localhost:8081", "00000000-0000-0000-0000-000000000000");
        nowsSubscriptionsClient = new NowsSubscriptionsClient(appProperties, RestClient.create());
    }

    @AfterEach
    void afterEach() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void getNowSubscriptions_should_callCorrectUrlAndAcceptHeader() {
        final String url = NowsSubscriptionsClient.NOW_SUBSCRIPTIONS_PATH + "?on=" + ACTIVE_AT;
        stubFor(WireMock.get(urlEqualTo(url)).willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(readResourceContents("nows/now-subscriptions-sample.json"))));

        nowsSubscriptionsClient.getNowSubscriptions(ACTIVE_AT);

        verify(getRequestedFor(urlEqualTo(url))
                .withHeader("Accept", WireMock.equalTo("application/vnd.referencedata.query.get-now-subscriptions+json"))
                .withHeader("CJSCPPUID", WireMock.equalTo("00000000-0000-0000-0000-000000000000")));
    }

    @Test
    void getNowSubscriptions_should_deserializeSubscriptionVocabulary() {
        final String url = NowsSubscriptionsClient.NOW_SUBSCRIPTIONS_PATH + "?on=" + ACTIVE_AT;
        stubFor(WireMock.get(urlEqualTo(url)).willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(readResourceContents("nows/now-subscriptions-sample.json"))));

        final List<NowsSubscription> subscriptions = nowsSubscriptionsClient.getNowSubscriptions(ACTIVE_AT);

        assertThat(subscriptions).hasSize(1);
        final NowsSubscription subscription = subscriptions.get(0);
        assertThat(subscription.getIsNowSubscription()).isTrue();
        assertThat(subscription.getIsEDTSubscription()).isFalse();
        assertThat(subscription.getSubscriptionVocabulary().getInCustody()).isTrue();
        assertThat(subscription.getSubscriptionVocabulary().getCustodyLocationIsPrison()).isTrue();
    }

    @SneakyThrows
    private String readResourceContents(final String resourceName) {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        return Files.readString(Path.of(resource.toURI()));
    }
}