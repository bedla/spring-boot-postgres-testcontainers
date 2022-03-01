package cz.bedla.samples.testcontainers.controller.country.dto;

import cz.bedla.samples.testcontainers.entity.Country;

import static org.apache.commons.lang3.Validate.notNull;

public record CountryDto(int id, String name) {
    public CountryDto {
        notNull(name, "name cannot be null");
    }

    public static CountryDto from(Country country) {
        notNull(country, "country cannot be null");
        return new CountryDto(country.id(), country.name());
    }
}
