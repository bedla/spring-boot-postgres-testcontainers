package cz.bedla.samples.testcontainers.service;

import cz.bedla.samples.testcontainers.entity.Country;
import cz.bedla.samples.testcontainers.entity.CountryPersonStatistics;
import cz.bedla.samples.testcontainers.entity.Tables;
import cz.bedla.samples.testcontainers.repository.CountryRepository;
import cz.bedla.samples.testcontainers.repository.StatisticsRepository;
import org.jooq.Record2;
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

    public List<CountryPersonStatistics> calculate(int top) {
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
            return list.stream()
                    .map(it -> {
                        var countryId = it.value1();
                        var personCount = it.value2();
                        var countryRecord = countryIndex.get(countryId);
                        validState(countryRecord != null,
                                "Unable to find country.id=%d in %d", countryId, countryIndex);
                        return new CountryPersonStatistics(Country.from(countryRecord), personCount);
                    })
                    .toList();
        } else {
            return List.of();
        }
    }
}
