package uk.gov.hmcts.cp.integration.e2e.driftdetection;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.integration.e2e.IngestionE2ETestBase;
import uk.gov.hmcts.cp.repositories.DefendantCaseRepository;
import uk.gov.hmcts.cp.repositories.DefendantRepository;
import uk.gov.hmcts.cp.repositories.DefendantSnapshotRepository;
import uk.gov.hmcts.cp.repositories.EventRepository;
import uk.gov.hmcts.cp.repositories.HearingRepository;
import uk.gov.hmcts.cp.servicebus.services.ServiceBusClientFactory;
import uk.gov.hmcts.cp.services.ClockService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.clients.NowsMetadataClient.NOWS_METADATA_PATH;
import static uk.gov.hmcts.cp.clients.NowsSubscriptionsClient.NOW_SUBSCRIPTIONS_PATH;

@TestPropertySource(properties = {
        "service-bus.auto-start-processors=true",
        "service-bus.ingestion-enabled=true"
})
@Import(NowsDriftDetectionIntegrationTest.FixedClockConfig.class)
class NowsDriftDetectionIntegrationTest extends IngestionE2ETestBase {

    // expected/*.json carries this as every eventTypes[].matchedAt, so the full-body diff needs no ignore-list.
    static final Instant FIXED_NOW = Instant.parse("2026-09-02T18:05:00Z");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration AWAIT_PERSISTENCE = Duration.ofSeconds(15);
    private static final Duration AWAIT_POLL_INTERVAL = Duration.ofMillis(500);

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ServiceBusClientFactory clientFactory;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private DefendantSnapshotRepository defendantSnapshotRepository;
    @Autowired
    private DefendantCaseRepository defendantCaseRepository;
    @Autowired
    private DefendantRepository defendantRepository;
    @Autowired
    private HearingRepository hearingRepository;

    private WireMockServer wireMockServer;

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        ClockService fixedClockService() {
            return new ClockService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        }
    }

    @BeforeEach
    void beforeEach() {
        clearRecords();
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        clearRecords();
    }

    @ParameterizedTest
    @ArgumentsSource(FixtureProvider.class)
    void replayedHearing_should_matchExpectedNowsOutput_forEveryDefendant(final DriftFixture fixture) throws Exception {
        final HearingIdentity identity = parseIdentity(fixture.root());

        stubReferenceData(NOWS_METADATA_PATH, fixture.root().resolve("nows-metadata.json"));
        stubReferenceData(NOW_SUBSCRIPTIONS_PATH, fixture.root().resolve("now-subscriptions.json"));
        seedRedis(fixture.root(), identity);
        publishHearingResultedEventToQueue(identity);

        try (Stream<Path> expectedFiles = Files.list(fixture.root().resolve("expected"))) {
            for (final Path expectedFile : expectedFiles.toList()) {
                final String defendantId = expectedFile.getFileName().toString().replace(".json", "");
                assertMatchesExpected(identity, defendantId, expectedFile);
            }
        }
    }

    private void clearRecords() {
        eventRepository.deleteAll();
        defendantSnapshotRepository.deleteAll();
        defendantCaseRepository.deleteAll();
        defendantRepository.deleteAll();
        hearingRepository.deleteAll();
    }

    private void stubReferenceData(final String path, final Path body) throws Exception {
        WireMock.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readString(body))));
    }

    private void seedRedis(final Path fixtureRoot, final HearingIdentity identity) throws Exception {
        final String cacheKey = "INT_" + identity.hearingId() + "_" + identity.hearingDay() + "_result_";
        redisTemplate.opsForValue().set(cacheKey, Files.readString(fixtureRoot.resolve("event.json")));
    }

    private void publishHearingResultedEventToQueue(final HearingIdentity identity) {
        final String body = """
                {
                  "id": "evt-1",
                  "eventType": "Hearing_Resulted",
                  "subject": "hearing/%s",
                  "eventTime": "2026-09-02T09:00:00.000Z",
                  "data": { "hearingId": "%s", "hearingDay": "%s", "userId": "00000000-0000-0000-0000-000000000099" }
                }
                """.formatted(identity.hearingId(), identity.hearingId(), identity.hearingDay());

        try (ServiceBusSenderClient sender = clientFactory.senderClient()) {
            sender.sendMessage(new ServiceBusMessage(body));
        }
    }

    private void assertMatchesExpected(final HearingIdentity identity, final String defendantId,
                                        final Path expectedFile) throws Exception {
        final String expectedJson = Files.readString(expectedFile);
        final String caseUrn = identity.caseUrnByDefendantId().get(defendantId);

        await().atMost(AWAIT_PERSISTENCE)
                .pollInterval(AWAIT_POLL_INTERVAL)
                .untilAsserted(() -> {
                    final String actualJson = mockMvc.perform(MockMvcRequestBuilders.get(
                                    "/cases/{caseURN}/hearings/{hearingId}/defendants/{defendantId}",
                                    caseUrn, identity.hearingId(), defendantId))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString();

                    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
                });
    }

    private HearingIdentity parseIdentity(final Path fixtureRoot) throws Exception {
        final JsonNode hearing = OBJECT_MAPPER.readTree(Files.readString(fixtureRoot.resolve("event.json"))).get("hearing");
        final String hearingId = hearing.get("id").asString();
        final String hearingDay = hearing.get("hearingDays").get(0).get("sittingDay").asString().substring(0, 10);
        final Map<String, String> caseUrnByDefendantId = new HashMap<>();
        for (final JsonNode prosecutionCase : hearing.get("prosecutionCases")) {
            final String caseUrn = prosecutionCase.get("prosecutionCaseIdentifier").get("caseURN").asString();
            for (final JsonNode defendant : prosecutionCase.get("defendants")) {
                caseUrnByDefendantId.put(defendant.get("id").asString(), caseUrn);
            }
        }
        return new HearingIdentity(hearingId, hearingDay, caseUrnByDefendantId);
    }

    private record HearingIdentity(String hearingId, String hearingDay, Map<String, String> caseUrnByDefendantId) {
    }
}