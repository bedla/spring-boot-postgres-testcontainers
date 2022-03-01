package cz.bedla.samples.testcontainers.controller.country.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public record CreateCountryRequest(
        @NotEmpty
        @Size(max = 64)
        String name
) {
}
