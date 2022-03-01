package cz.bedla.samples.testcontainers.entity;

import cz.bedla.samples.testcontainers.entity.tables.records.CountryRecord;

import static org.apache.commons.lang3.Validate.notNull;

public record Country(int id, String name) {
    public Country {
        notNull(name, "name cannot be null");
    }

    public static Country from(CountryRecord record) {
        notNull(record, "record cannot be null");
        return new Country(record.getId(), record.getName());
    }
}
