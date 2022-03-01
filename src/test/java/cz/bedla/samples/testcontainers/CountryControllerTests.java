package cz.bedla.samples.testcontainers;

import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.stream.Stream;

import static cz.bedla.samples.testcontainers.ApiUtils.apiCreateCountry;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class CountryControllerTests extends BaseTestcontainersTests {
    @Test
    void emptyCountries() {
        get("/country")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", empty());
    }

    @Test
    void createCountryAndFindOneInList() {
        var countryId = apiCreateCountry("Country1");

        get("/country")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(countryId))
                .body("[0].name", equalTo("Country1"));
    }

    @Test
    void updateAndGetCountry() {
        var countryId = apiCreateCountry("Country-update");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Country-changed"
                ))
                .put("/country/{id}", countryId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        get("/country/{id}", countryId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(countryId))
                .body("name", equalTo("Country-changed"));
    }

    @Test
    void deleteAndDoNotFoundCountry() {
        var countryId = apiCreateCountry("Country-delete");

        given()
                .delete("/country/{id}", countryId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        get("/country/{id}", countryId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteNotFound() {
        given()
                .delete("/country/{id}", 999_999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void listCountries() {
        var countryId1 = apiCreateCountry("C1");
        var countryId2 = apiCreateCountry("C2");
        get("/country")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].id", equalTo(countryId1))
                .body("[0].name", equalTo("C1"))
                .body("[1].id", equalTo(countryId2))
                .body("[1].name", equalTo("C2"));
    }

    @Test
    void countryNotFound() {
        given()
                .get("/country/{id}", 999_999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @ParameterizedTest
    @MethodSource("countryInvalidNameProvider")
    void createCountryInvalidName(String name) {
        given()
                .contentType(ContentType.JSON)
                .body(name == null
                        ? Map.of()
                        : Map.of(
                        "name", name
                ))
                .post("/country")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", equalTo("Bad Request"))
                .body("path", equalTo("/api/v1/country"));
    }

    @ParameterizedTest
    @MethodSource("countryInvalidNameProvider")
    void updateCountryInvalidName(String name) {
        given()
                .contentType(ContentType.JSON)
                .body(name == null
                        ? Map.of()
                        : Map.of(
                        "name", name
                ))
                .put("/country/{id}", 123456)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", equalTo("Bad Request"))
                .body("path", equalTo("/api/v1/country/123456"));
    }

    private static Stream<Arguments> countryInvalidNameProvider() {
        return Stream.of(
                Arguments.arguments((String) null),
                Arguments.arguments(""),
                Arguments.arguments(StringUtils.repeat('x', 64) + "-more")
        );
    }
}
