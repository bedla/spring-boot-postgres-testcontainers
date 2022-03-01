package cz.bedla.samples.testcontainers.controller.person.dto;

public record UpdatePersonRequest(String firstName, String lastName, int countryId) {
}
