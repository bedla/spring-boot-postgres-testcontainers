package cz.bedla.samples.testcontainers.controller.statistics.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public record TopCountriesStatisticsRequest(
        @Min(1)
        @Max(1000_000 /* there is around 250 countries in the World */)
        int top
) {
}
