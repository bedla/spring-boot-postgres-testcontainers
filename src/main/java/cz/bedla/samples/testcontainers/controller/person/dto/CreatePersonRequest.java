package cz.bedla.samples.testcontainers.controller.person.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

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
