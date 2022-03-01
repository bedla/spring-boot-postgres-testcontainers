package cz.bedla.samples.testcontainers;

import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

public final class ApiUtils {
    private ApiUtils() {
    }

    public static int apiCreateCountry(String name) {
        var location = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name
                ))
                .post("/country")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header(HttpHeaders.LOCATION, startsWith("/api/v1/country/"))
                .extract()
                .header(HttpHeaders.LOCATION);
        return NumberUtils.toInt(StringUtils.substringAfterLast(location, "/"));
    }

    public static int apiCreatePerson(String firstName, String lastName, int countryId) {
        var location = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "countryId", countryId
                ))
                .post("/person")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header(HttpHeaders.LOCATION, startsWith("/api/v1/person/"))
                .extract()
                .header(HttpHeaders.LOCATION);
        return NumberUtils.toInt(StringUtils.substringAfterLast(location, "/"));
    }

}
