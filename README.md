# Complex example how to use Testcontainers with various development scenarios

In this repository you can find sample application that uses various technologies, described below, to show how easy it
is possible to create & test application when you have correct tools. Mainly it focuses on writing integration tests
using Testcontainers.

Technologies used:

- Java 17
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
    - Best IDE
- [Spring Boot](https://spring.io/projects/spring-boot)
    - framework for creating REST microservices
- [PostgreSQL](https://www.postgresql.org/)
    - DB for persistent storage
- [JOOQ](https://www.jooq.org/)
    - DB access layer
- [Liquibase](https://liquibase.org/)
    - DB migration tool
- [Redis](https://redis.io/)
    - For caching
- [JUnit 5](https://junit.org/junit5/)
    - For testing
- [Docker](https://www.docker.com/)
    - For local development and testing
- [Testcontainers](https://www.testcontainers.org/)
    - Integration testing framework using Docker

## Application use-case from business perspective

Application use-case is simple. It contains DB of countries and persons. Person belongs to one country.

Application is able to operate on **Country** or **Person** with CRUD functionality.

There is also statistics endpoint that is able to calculate "**Top countries with most persons**"

You can check source code for functionality which is super simple.

# Technical description

Application is written in Java 17 (usage of Records, var keyword), build by Maven and wired using Spring Boot.
Application is usual REST endpoint application that uses DB as it's persistent store.

## Database & migration

Data model is defined by `./sql/changelog.xml` file which is Liquibase DB migration definition.

Because we want to simulate microservices environment where many microservices can share one DB, and we want to have
application's classpath as small as possible, we run liquibase migration. This can be seen as bad-practice, but in this
repository we want to show how to achieve this, and we do not discuss here if this is bad-practice or not.

You can find usage of migration from two sides:

- Start of Local development environment
- Start of Testcontainers integration tests

## Database & JOOQ metamodel generation

JOOQ is DSL for creating SQL in Java code. When you generate metamodel from DB, you can take advantage from having
static compiler check during build time. JOOQ will generate classes that conform to DB objects you have defined. For
example, we have table `PERSON` and JOOQ will generate for us `Person` that can be used for table operations,
and `PersonRecord` class as counterpart to table's DB row.

Compare this to SQL statements in pure String (hard to check correct usage of DB primitives), or even usage
overcomplicated ORM tools like Hibernate/JPA (rigid mapping, performance problem, corner cases, ...).

Metamodel generation is run by Maven's `jooq-codegen-maven` plugin. It is defined in `generate-jooq` profile, and it
contains following steps:

1. Start PostgreSQL container using Testcontainers
    1. Set `generated-db.url`, `generated-db.username`, and `generated-db.password` properties to Maven runtime context.
2. Run `liquibase-maven-plugin` with those properties to migrate DB to recent state
3. Run `jooq-codegen-maven` to generate metamodel source code classes to `./target/generated-sources/jooq` directory and
   into `cz.bedla.samples.testcontainers.entity` Java package.

Result of those steps is ready to compile (and use) Java source code with metamodel of database.

## Local development environment

To start Local development environment you should run `docker compose up` command in repo-root directory.

First it starts PostgreSQL database and expose it at port `5432` with username `postgres` and password `Password1234`,
it also creates database `my-database`. You can use [JetBrains DataGrip](https://www.jetbrains.com/datagrip/) to access
database.

After DB start Liquibase container is used to migrate DB to correct state.

Also, Redis cache is started at port `6379`. To show content of the cache you can
use [Redis Insight tool](https://redis.com/redis-enterprise/redis-insight/).

Now you can start Spring Boot application as usual. Default configuration that conforms docker-compose started services
is preconfigured for you, see `./src/main/resources/application.properties` for details.

### IDEA HTTP client tests

When your application is started (from steps in chapter above), you can test endpoints using IntelliJ IDEA's build in
HTTP client.

HTTP requests can be found in `./http-tests` directory. You can see that with limited functionality you can create HTTP
tests for you endpoints.

Issues I found:

- [Support shared functions between HTTP Client test sections](https://youtrack.jetbrains.com/issue/IDEA-291084)
- [HTTP Client - show HTTP method in result tree](https://youtrack.jetbrains.com/issue/IDEA-289607)

## Integration testing

Testcontainers are used to do proper integration tests. When we do not create integration tests, and we have some
business logic hidden behind external systems, our only solution is to mock those external systems. This is in some
situations sufficient, but in this case our tests are testing correct mock behavior, and not exact business logic
dependent on external system.

Our application uses two external systems:

1. PostgreSQL database
    1. We persist our application's data
    2. We do statistics calculation using SQL statements
2. Redis Cache
    1. We cache statistics calculation for some time, to not to overwhelm database

### Shared containers integration tests without caching

You can find CRUD integration tests in `PersonControllerTests` and `CountryControllerTests` classes.
In `StatisticsControllerTests` class you can find integration tests for statistics calculation.

Mind that they have base class `BaseTestcontainersTests` that starts containers
using [Singleton container pattern](https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers)
. This is because we do not want to start/stop containers with every test run.

Also mind that for this kind of test we disabled Spring Boot's caching facility by setting `spring.cache.type=none`
property.

With every test run we clean DB state with `containerZoo.truncateDb()` call.

For more details see `ContainerZoo` class description below.

### Integration tests with cache enabled

To test if caching is correctly enabled/implemented we have `CacheStatisticsControllerTests` integration test.

It enabled Redis cache by setting property `spring.cache.type=redis`, and also sets up `spring.redis.host=localhost`
and `spring.cache.redis.time-to-live=2s` properties to connect to Redis instance and have fixed TTL of cache keys.

We use [Awaitality](https://github.com/awaitility/awaitility) for pause before cache expiration during test run.

With every test run we clean DB state with `containerZoo.truncateDb()` method call. Also, we truncate Cache state by
calling `containerZoo.truncateCache()` method.

### Container ZOO

Because we have much Integration test classes, and we do not want to share every use-case configuration scenario with
each test class, we create `ContainerZoo` class that contains common logic for Docker containers lifecycle and
configuration, and is used as Singleton container pattern (mind that started containers are automatically stopped by
Testcontainers [Ryuk](https://github.com/testcontainers/moby-ryuk) sidecar container).

Container ZOO starts 3 containers:

- PostgreSQL database container
- Liquibase DB migration container
- _(Optional)_ Redis Cache container

To have test runs as isolated as possible we clear DB and Cache state with every test run using `.truncateDb()`
and `.truncateCache()` methods. Interesting feature of them is that they run commands inside containers to do their job.

Because in Integration tests we run Spring Boot application and also containers that exposes services on random ports,
we need to configure Spring's `ApplicationContext`
with [dynamic properties feature](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/DynamicPropertySource.html)
in `.setupDynamicProperties(..)` method.
