package cz.bedla.samples.testcontainers.controller.person.dto;

import cz.bedla.samples.testcontainers.controller.country.dto.CountryDto;
import cz.bedla.samples.testcontainers.entity.Person;

import static org.apache.commons.lang3.Validate.notNull;

public record PersonDto(int id, String firstName, String lastName, CountryDto country) {
    public PersonDto {
        notNull(firstName, "firstName cannot be null");
        notNull(lastName, "lastName cannot be null");
        notNull(country, "country cannot be null");
    }

    public static PersonDto from(Person person) {
        notNull(person, "person cannot be null");
        return new PersonDto(person.id(), person.firstName(), person.lastName(), CountryDto.from(person.country()));
    }
}
