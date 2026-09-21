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
import uk.gov.hmcts.cp.domain.nowscompute.NowMetadataResponse.NowDefinition;

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

class NowsMetadataClientTest {

    private static final LocalDate ACTIVE_AT = LocalDate.of(2026, 9, 2);

    private WireMockServer wireMockServer;
    private NowsMetadataClient nowsMetadataClient;

    @BeforeEach
    void beforeEach() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);

        final AppPropertiesBackend appProperties = new AppPropertiesBackend(
                "http://localhost:8081", "00000000-0000-0000-0000-000000000000",
                "http://localhost:8081", "00000000-0000-0000-0000-000000000000");
        nowsMetadataClient = new NowsMetadataClient(appProperties, RestClient.create());
    }

    @AfterEach
    void afterEach() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void getNowDefinitions_should_callCorrectUrlAndAcceptHeader() {
        final String url = NowsMetadataClient.NOWS_METADATA_PATH + "?on=" + ACTIVE_AT;
        stubFor(WireMock.get(urlEqualTo(url)).willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(readResourceContents("nows/nows-metadata-sample.json"))));

        nowsMetadataClient.getNowDefinitions(ACTIVE_AT);

        verify(getRequestedFor(urlEqualTo(url))
                .withHeader("Accept", WireMock.equalTo("application/vnd.referencedata.get-nows-metadata+json"))
                .withHeader("CJSCPPUID", WireMock.equalTo("00000000-0000-0000-0000-000000000000")));
    }

    @Test
    void getNowDefinitions_should_deserializeRequirementTreeAndStaticText() {
        final String url = NowsMetadataClient.NOWS_METADATA_PATH + "?on=" + ACTIVE_AT;
        stubFor(WireMock.get(urlEqualTo(url)).willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(readResourceContents("nows/nows-metadata-sample.json"))));

        final List<NowDefinition> definitions = nowsMetadataClient.getNowDefinitions(ACTIVE_AT);

        assertThat(definitions).hasSize(1);
        final NowDefinition definition = definitions.get(0);
        assertThat(definition.getName()).isEqualTo("WEE_CustodialSentence");
        assertThat(definition.getNowTextList()).hasSize(1);
        assertThat(definition.getNowTextList().get(0).getNowReference()).isEqualTo("orderText");
        assertThat(definition.getNowRequirements()).hasSize(1);
        assertThat(definition.getNowRequirements().get(0).getResultDefinitionId())
                .isEqualTo("3f8e2a10-9c44-4b6a-8f01-2b7d9e5a6c11");
        assertThat(definition.getNowRequirements().get(0).getPrimary()).isTrue();
    }

    @SneakyThrows
    private String readResourceContents(final String resourceName) {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        return Files.readString(Path.of(resource.toURI()));
    }
}