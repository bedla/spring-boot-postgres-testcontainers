package cz.bedla.samples.testcontainers.controller.person.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreatePersonRequest(
        @NotEmpty
        @Size(max = 64)
        String firstName,
        @NotEmpty
        @Size(max = 64)
        String lastName,
        int countryId
) {
}
