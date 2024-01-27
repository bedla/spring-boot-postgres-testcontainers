package cz.bedla.samples.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

public class ContainerZoo {
    private static final Logger log = LoggerFactory.getLogger(ContainerZoo.class);

    private final Network network = Network.newNetwork();
    private final PostgreSQLContainer<?> postgresqlContainer;
    private final GenericContainer<?> liquibaseContainer;
    private final RedisContainer redisContainer;

    public static ContainerZoo create() {
        return new ContainerZoo(false);
    }

    public static ContainerZoo createWithRedis() {
        return new ContainerZoo(true);
    }

    private ContainerZoo(boolean createRedis) {
        var pgsqlAlias = "pgsql-db";

        this.postgresqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"))
                .withNetwork(network)
                .withNetworkAliases(pgsqlAlias)
                .withDatabaseName("integration-tests");

        this.liquibaseContainer = new GenericContainer<>(DockerImageName.parse("liquibase/liquibase"))
                .withCommand(
                        "--url=jdbc:postgresql://" + pgsqlAlias + ":5432/" + postgresqlContainer.getDatabaseName(),
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

        this.redisContainer = createRedis
                ? new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG))
                .withNetworkAliases("redis")
                .withNetwork(network)
                .withEnv("ALLOW_EMPTY_PASSWORD", "yes")
                : null;
    }

    public void start() {
        log.info("> Start pgsql");
        postgresqlContainer.start();
        log.info("> Start liquibase");
        liquibaseContainer.start();
        if (redisContainer != null) {
            log.info("> Start redis");
            redisContainer.start();
        } else {
            log.info("> Redis start skip requested");
        }
    }

    public void setupDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        if (redisContainer != null) {
            registry.add("spring.data.redis.url", redisContainer::getRedisURI);
        }
    }

    public void truncateCache() {
        log.info("> Truncate Redis cache");
        runInContainer(redisContainer, "redis-cli", "flushall");
    }

    public void truncateDb() {
        log.info("> Truncate pgsql DB");
        runInContainer(postgresqlContainer,
                "psql",
                "-U", postgresqlContainer.getUsername(),
                "-d", postgresqlContainer.getDatabaseName(),
                "-c", "TRUNCATE TABLE foo.PERSON, foo.COUNTRY;");
    }

    private static void runInContainer(GenericContainer<?> container, String... command) {
        try {
            var execResult = container.execInContainer(command);
            if (execResult.getExitCode() != 0) {
                throw new IllegalStateException("Unable to run " + command[0] + "\n\n" + execResult);
            }
        } catch (IOException | InterruptedException e) {
            ExceptionUtils.rethrow(e);
        }
    }
}
