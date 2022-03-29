package cz.bedla.samples.testcontainers;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static cz.bedla.samples.testcontainers.ApiUtils.apiCreateCountry;
import static cz.bedla.samples.testcontainers.ApiUtils.apiCreatePerson;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
                "spring.cache.type=redis",
                "spring.redis.host=localhost",
                "spring.cache.redis.time-to-live=2s"
        }
)
class CacheStatisticsControllerTests {
    private static final Map<String, Integer> REQUEST_BODY = Map.of("top", 2);

    private static final ContainerZoo containerZoo = ContainerZoo.createWithRedis();

    @DynamicPropertySource
    static void setupDynamiProperties(DynamicPropertyRegistry registry) {
        containerZoo.setupDynamicProperties(registry);
    }

    @LocalServerPort
    protected int localServerPort;

    @BeforeAll
    static void beforeAll() {
        containerZoo.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = localServerPort;
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        containerZoo.truncateDb();
        containerZoo.truncateCache();
    }

    @Test
    void statisticsTopCountriesWithExpiredCache() {
        var czId = apiCreateCountry("CZ");
        var deId = apiCreateCountry("DE");
        var usId = apiCreateCountry("US");

        apiCreatePerson("Kristine", "Kochanski", czId);
        apiCreatePerson("Arnold", "Rimmer", czId);

        apiCreatePerson("Friedrich", "Nietzsche", deId);

        requestTopCountriesStatistics(
                czId, "CZ", 2,
                deId, "DE", 1);

        var start = System.currentTimeMillis();

        apiCreatePerson("Butch", "Coolidge", usId);
        apiCreatePerson("Vincent", "Vega", usId);
        apiCreatePerson("Jules", "Winnfield", usId);

        // wait 2.5s for Redis cache TTL expiration
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> Duration.ofMillis(System.currentTimeMillis() - start).toMillis() > Duration.ofSeconds(2).plusMillis(500).toMillis());

        requestTopCountriesStatistics(
                usId, "US", 3,
                czId, "CZ", 2);
    }

    @Test
    void statisticsTopCountriesWithoutExpiredCache() {
        var czId = apiCreateCountry("CZ");
        var deId = apiCreateCountry("DE");
        var usId = apiCreateCountry("US");

        apiCreatePerson("Kristine", "Kochanski", czId);
        apiCreatePerson("Arnold", "Rimmer", czId);

        apiCreatePerson("Friedrich", "Nietzsche", deId);

        var result = get("/country")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body().as(new TypeRef<List<Map<String, Object>>>() {
                });
        System.out.println(result);

        requestTopCountriesStatistics(
                czId, "CZ", 2,
                deId, "DE", 1);

        apiCreatePerson("Butch", "Coolidge", usId);
        apiCreatePerson("Vincent", "Vega", usId);
        apiCreatePerson("Jules", "Winnfield", usId);

        // do not wait for cache expiration

        requestTopCountriesStatistics(
                czId, "CZ", 2,
                deId, "DE", 1);
    }

    private void requestTopCountriesStatistics(
            int country1Id, String country1Name, int country1PersonCount,
            int country2Id, String country2Name, int country2PersonCount
    ) {
        given()
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("top", hasSize(2))
                .body("top[0].country.id", equalTo(country1Id))
                .body("top[0].country.name", equalTo(country1Name))
                .body("top[0].personCount", equalTo(country1PersonCount))
                .body("top[1].country.id", equalTo(country2Id))
                .body("top[1].country.name", equalTo(country2Name))
                .body("top[1].personCount", equalTo(country2PersonCount));
    }
}
