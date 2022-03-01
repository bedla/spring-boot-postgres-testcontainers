package cz.bedla.samples.testcontainers.controller.statistics.dto;

import cz.bedla.samples.testcontainers.controller.country.dto.CountryDto;

import java.util.List;

public record TopCountriesStatisticsResponse(List<CountryPersons> top) {
    public record CountryPersons(CountryDto country, int personCount) {
    }
}
