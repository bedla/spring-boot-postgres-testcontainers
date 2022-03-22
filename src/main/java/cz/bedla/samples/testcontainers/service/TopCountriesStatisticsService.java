package cz.bedla.samples.testcontainers.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.bedla.samples.testcontainers.entity.Country;
import cz.bedla.samples.testcontainers.entity.CountryPersonStatistics;
import cz.bedla.samples.testcontainers.entity.Tables;
import cz.bedla.samples.testcontainers.repository.CountryRepository;
import cz.bedla.samples.testcontainers.repository.StatisticsRepository;
import org.jooq.Record2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

@Service
@Transactional
public class TopCountriesStatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final CountryRepository countryRepository;

    public TopCountriesStatisticsService(
            StatisticsRepository statisticsRepository,
            CountryRepository countryRepository
    ) {
        this.statisticsRepository = notNull(statisticsRepository, "statisticsRepository cannot be null");
        this.countryRepository = notNull(countryRepository, "countryRepository cannot be null");
    }

    @Cacheable("statistics")
    public Result calculate(int top) {
        var list = statisticsRepository.calculateTopCountriesWithMostPersons(top);
        if (list.isNotEmpty()) {
            var countryIds = list.stream()
                    .map(Record2::value1)
                    .filter(Objects::nonNull)
                    .toList();
            validState(!countryIds.isEmpty(), "Unable to find countries by ids from calculated statistics: %s", list);
            var countries = countryRepository.findCountriesByIds(countryIds);
            validState(countries.size() == countryIds.size(),
                    "Unable to find all countries by ids: %s", countryIds);
            var countryIndex = countries.intoMap(Tables.COUNTRY.ID);
            return new Result(list.stream()
                    .map(it -> {
                        var countryId = it.value1();
                        var personCount = it.value2();
                        var countryRecord = countryIndex.get(countryId);
                        validState(countryRecord != null,
                                "Unable to find country.id=%d in %d", countryId, countryIndex);
                        return new CountryPersonStatistics(Country.from(countryRecord), personCount);
                    })
                    .toList());
        } else {
            return new Result(List.of());
        }
    }

    // https://github.com/FasterXML/jackson-databind/issues/1349
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    public record Result(List<CountryPersonStatistics> items) {
    }
}
