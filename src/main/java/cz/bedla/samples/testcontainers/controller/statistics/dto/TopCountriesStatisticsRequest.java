package cz.bedla.samples.testcontainers.controller.statistics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TopCountriesStatisticsRequest(
        @Min(1)
        @Max(1000_000 /* there is around 250 countries in the World */)
        int top
) {
}
