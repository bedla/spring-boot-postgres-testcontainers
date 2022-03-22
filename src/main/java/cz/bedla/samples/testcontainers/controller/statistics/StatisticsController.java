package cz.bedla.samples.testcontainers.controller.statistics;

import cz.bedla.samples.testcontainers.controller.country.dto.CountryDto;
import cz.bedla.samples.testcontainers.controller.statistics.dto.TopCountriesStatisticsRequest;
import cz.bedla.samples.testcontainers.controller.statistics.dto.TopCountriesStatisticsResponse;
import cz.bedla.samples.testcontainers.service.TopCountriesStatisticsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.Validate.notNull;

@RestController
@RequestMapping("/api/v1")
public class StatisticsController {
    private final TopCountriesStatisticsService topCountriesStatisticsService;

    public StatisticsController(TopCountriesStatisticsService topCountriesStatisticsService) {
        this.topCountriesStatisticsService = notNull(topCountriesStatisticsService, "statisticsService cannot be null");
    }

    @PostMapping("/statistics/top-countries")
    public TopCountriesStatisticsResponse calculateTopCountries(
            @RequestBody @Validated TopCountriesStatisticsRequest request
    ) {
        var result = topCountriesStatisticsService.calculate(request.top());
        return new TopCountriesStatisticsResponse(result.items().stream()
                .map(it -> new TopCountriesStatisticsResponse.CountryPersons(
                        CountryDto.from(it.country()),
                        it.personsCount()))
                .toList());
    }
}
