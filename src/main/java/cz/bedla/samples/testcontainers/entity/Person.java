package cz.bedla.samples.testcontainers.entity;

import cz.bedla.samples.testcontainers.entity.tables.records.CountryRecord;
import cz.bedla.samples.testcontainers.entity.tables.records.PersonRecord;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

public record Person(int id, String firstName, String lastName, Country country) {
    public Person {
        notNull(firstName, "firstName cannot be null");
        notNull(lastName, "lastName cannot be null");
        notNull(country, "country cannot be null");
    }

    public static Person from(PersonRecord personRecord, Map<Integer, CountryRecord> countryIndex) {
        notNull(personRecord, "personRecord cannot be null");
        notNull(countryIndex, "countryIndex cannot be null");
        return from(personRecord, countryIndex.get(personRecord.getCountryId()));
    }

    public static Person from(PersonRecord personRecord, CountryRecord countryRecord) {
        notNull(personRecord, "personRecord cannot be null");
        notNull(countryRecord, "countryRecord cannot be null");
        return new Person(
                personRecord.getId(),
                personRecord.getFirstname(),
                personRecord.getLastname(),
                Country.from(countryRecord));
    }
}
