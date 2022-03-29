package cz.bedla.samples.testcontainers;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
                "spring.cache.type=none"
        }
)
public abstract class BaseTestcontainersTests {
    private static final ContainerZoo containerZoo = ContainerZoo.create();

    @DynamicPropertySource
    static void setupDynamicProperties(DynamicPropertyRegistry registry) {
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
    }
}
