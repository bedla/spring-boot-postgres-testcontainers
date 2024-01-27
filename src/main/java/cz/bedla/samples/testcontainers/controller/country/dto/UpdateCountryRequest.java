package cz.bedla.samples.testcontainers.controller.country.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateCountryRequest(
        @NotEmpty
        @Size(max = 64)
        String name
) {
}
