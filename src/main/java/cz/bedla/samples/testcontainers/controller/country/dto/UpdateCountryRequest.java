package cz.bedla.samples.testcontainers.controller.country.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public record UpdateCountryRequest(
        @NotEmpty
        @Size(max = 64)
        String name
) {
}
