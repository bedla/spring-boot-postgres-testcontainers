package cz.bedla.samples.testcontainers;

import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.stream.Stream;

import static cz.bedla.samples.testcontainers.ApiUtils.apiCreateCountry;
import static cz.bedla.samples.testcontainers.ApiUtils.apiCreatePerson;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class PersonControllerTests extends BaseTestcontainersTests {
    @Test
    void emptyPersons() {
        get("/person")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", empty());
    }

    @Test
    void createPersonAndFindOneInList() {
        var countryId = apiCreateCountry("Country-person1");
        var personId = apiCreatePerson("Ivo", "Smid", countryId);

        get("/person")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(personId))
                .body("[0].firstName", equalTo("Ivo"))
                .body("[0].lastName", equalTo("Smid"))
                .body("[0].country.id", equalTo(countryId))
                .body("[0].country.name", equalTo("Country-person1"));
    }

    @Test
    void updateAndGetPerson() {
        var countryId = apiCreateCountry("Country-person2");
        var personId = apiCreatePerson("Ivo", "Smid", countryId);

        get("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(personId))
                .body("firstName", equalTo("Ivo"))
                .body("lastName", equalTo("Smid"))
                .body("country.id", equalTo(countryId))
                .body("country.name", equalTo("Country-person2"));

        var countryIdNew = apiCreateCountry("Country-person2 update");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Ivo update",
                        "lastName", "Smid update",
                        "countryId", countryIdNew
                ))
                .put("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        get("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(personId))
                .body("firstName", equalTo("Ivo update"))
                .body("lastName", equalTo("Smid update"))
                .body("country.id", equalTo(countryIdNew))
                .body("country.name", equalTo("Country-person2 update"));
    }

    @Test
    void deleteAndDoNotFoundPerson() {
        var countryId = apiCreateCountry("Country-delete");
        var personId = apiCreatePerson("Ivo", "Smid", countryId);

        given()
                .delete("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        get("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteNotFound() {
        given()
                .delete("/person/{id}", 999_999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void listPersons() {
        var countryId1 = apiCreateCountry("Country-person1");
        var personId1 = apiCreatePerson("Ivo", "Smid 1", countryId1);
        var countryId2 = apiCreateCountry("Country-person2");
        var personId2 = apiCreatePerson("Ivo", "Smid 2", countryId2);

        get("/person")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].id", equalTo(personId1))
                .body("[0].firstName", equalTo("Ivo"))
                .body("[0].lastName", equalTo("Smid 1"))
                .body("[0].country.id", equalTo(countryId1))
                .body("[0].country.name", equalTo("Country-person1"))
                .body("[1].id", equalTo(personId2))
                .body("[1].firstName", equalTo("Ivo"))
                .body("[1].lastName", equalTo("Smid 2"))
                .body("[1].country.id", equalTo(countryId2))
                .body("[1].country.name", equalTo("Country-person2"));
    }

    @Test
    void personNotFound() {
        given()
                .get("/person/{id}", 999_999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void createPersonWithInvalidCountry() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Ivo",
                        "lastName", "Smid 2",
                        "countryId", 999999
                ))
                .post("/person")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updatePersonWithInvalidCountry() {
        var countryId = apiCreateCountry("Country-person3");
        var personId = apiCreatePerson("Ivo", "Smid", countryId);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Ivo update",
                        "lastName", "Smid update",
                        "countryId", 999999
                ))
                .put("/person/{id}", personId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }


    @ParameterizedTest
    @MethodSource("personInvalidNamesProvider")
    void createPersonWithInvalidNames(String firstName, String lastName) {
        var countryId = apiCreateCountry("Country-person2");

        var payloadBuilder = ImmutableMap.<String, Object>builder()
                .put("countryId", countryId);
        if (firstName != null) {
            payloadBuilder.put("firstName", firstName);
        }
        if (lastName != null) {
            payloadBuilder.put("lastName", lastName);
        }

        given()
                .contentType(ContentType.JSON)
                .body(payloadBuilder.build())
                .post("/person")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", equalTo("Bad Request"))
                .body("path", equalTo("/api/v1/person"));
    }

    private static Stream<Arguments> personInvalidNamesProvider() {
        var moreCharacters = StringUtils.repeat('x', 64) + "-more";
        return Stream.of(
                Arguments.arguments(null, null),
                Arguments.arguments(null, "Smid"),
                Arguments.arguments("Ivo", null),
                Arguments.arguments("", ""),
                Arguments.arguments("", "Smid"),
                Arguments.arguments("Ivo", ""),
                Arguments.arguments(moreCharacters, moreCharacters),
                Arguments.arguments(moreCharacters, "Smid"),
                Arguments.arguments("Ivo", moreCharacters)
        );
    }
}
