package cz.bedla.samples.testcontainers;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.stream.Stream;

import static cz.bedla.samples.testcontainers.ApiUtils.apiCreateCountry;
import static cz.bedla.samples.testcontainers.ApiUtils.apiCreatePerson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class StatisticsControllerTests extends BaseTestcontainersTests {
    @Test
    void emptyStatisticsTopCountries() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "top", 999
                ))
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("top", empty());
    }

    @Test
    void statisticsTopCountries() {
        var czId = apiCreateCountry("CZ");
        var deId = apiCreateCountry("DE");
        var usId = apiCreateCountry("US");

        apiCreatePerson("Kristine", "Kochanski", czId);
        apiCreatePerson("Arnold", "Rimmer", czId);
        apiCreatePerson("David", "Lister", czId);

        apiCreatePerson("Friedrich", "Nietzsche", deId);
        apiCreatePerson("Martin", "Heidegger", deId);

        apiCreatePerson("Butch", "Coolidge", usId);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "top", 2
                ))
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("top", hasSize(2))
                .body("top[0].country.id", equalTo(czId))
                .body("top[0].country.name", equalTo("CZ"))
                .body("top[0].personCount", equalTo(3))
                .body("top[1].country.id", equalTo(deId))
                .body("top[1].country.name", equalTo("DE"))
                .body("top[1].personCount", equalTo(2));
    }

    @Test
    void statisticsTopCountriesSameCountOrder() {
        // creation order creates PK oposite order
        var bbbId = apiCreateCountry("BBB");
        var aaaId = apiCreateCountry("AAA");

        apiCreatePerson("Kristine", "Kochanski", aaaId);
        apiCreatePerson("Arnold", "Rimmer", aaaId);
        apiCreatePerson("David", "Lister", bbbId);
        apiCreatePerson("The", "Cat", bbbId);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "top", 2
                ))
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("top", hasSize(2))
                .body("top[0].country.id", equalTo(aaaId))
                .body("top[0].country.name", equalTo("AAA"))
                .body("top[0].personCount", equalTo(2))
                .body("top[1].country.id", equalTo(bbbId))
                .body("top[1].country.name", equalTo("BBB"))
                .body("top[1].personCount", equalTo(2));
    }

    @Test
    void statisticsTopCountriesCountryWithoutPerson() {
        var xxId = apiCreateCountry("XX");
        var yyId = apiCreateCountry("YY");

        apiCreatePerson("Kristine", "Kochanski", xxId);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "top", 2
                ))
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("top", hasSize(2))
                .body("top[0].country.id", equalTo(xxId))
                .body("top[0].country.name", equalTo("XX"))
                .body("top[0].personCount", equalTo(1))
                .body("top[1].country.id", equalTo(yyId))
                .body("top[1].country.name", equalTo("YY"))
                .body("top[1].personCount", equalTo(0));
    }

    @ParameterizedTest
    @MethodSource("invalidTopProvider")
    void invalidStatisticsTopCountriesTop(Integer top) {
        given()
                .contentType(ContentType.JSON)
                .body(top == null
                        ? Map.of()
                        : Map.of(
                        "top", top
                ))
                .post("/statistics/top-countries")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", equalTo("Bad Request"))
                .body("path", equalTo("/api/v1/statistics/top-countries"));
    }

    private static Stream<Arguments> invalidTopProvider() {
        return Stream.of(
                Arguments.arguments((Integer) null),
                Arguments.arguments(-1),
                Arguments.arguments(0)
        );
    }
}
