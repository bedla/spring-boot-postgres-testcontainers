package cz.bedla.samples.testcontainers;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTestcontainersTests {
    private static final String PGSQL_ALIAS = "pgsql-db";

    private static final Network network = Network.newNetwork();

    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withNetwork(network)
            .withNetworkAliases(PGSQL_ALIAS)
            .withDatabaseName("integration-tests");

    private static final GenericContainer<?> liquibaseContainer = new GenericContainer<>(DockerImageName.parse("liquibase/liquibase"))
            .withCommand(
                    "--url=jdbc:postgresql://" + PGSQL_ALIAS + ":5432/" + postgresqlContainer.getDatabaseName(),
                    "--changeLogFile=./changelog/changelog.xml",
                    "--username=" + postgresqlContainer.getUsername(),
                    "--password=" + postgresqlContainer.getPassword(),
                    "update")
            .withFileSystemBind("./sql", "/liquibase/changelog")
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx("Liquibase command '.+' was executed successfully\\.\\n")
                    .withStartupTimeout(Duration.ofSeconds(30)))
            .withNetwork(network)
            .dependsOn(postgresqlContainer);

    @DynamicPropertySource
    static void pgsqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @LocalServerPort
    protected int localServerPort;

    @BeforeAll
    static void beforeAll() {
        postgresqlContainer.start();
        liquibaseContainer.start();
    }

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        RestAssured.port = localServerPort;
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        var execResult = postgresqlContainer.execInContainer(
                "psql",
                "-U", postgresqlContainer.getUsername(),
                "-d", postgresqlContainer.getDatabaseName(),
                "-c", "TRUNCATE TABLE foo.PERSON, foo.COUNTRY;");
        if (execResult.getExitCode() != 0) {
            throw new IllegalStateException("Unable to run psql\n\n" + execResult);
        }
    }
}
