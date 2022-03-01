package cz.bedla.samples.testcontainers.service;

import cz.bedla.samples.testcontainers.service.exception.CountryNotFoundException;
import cz.bedla.samples.testcontainers.entity.Person;
import cz.bedla.samples.testcontainers.entity.Tables;
import cz.bedla.samples.testcontainers.entity.tables.records.CountryRecord;
import cz.bedla.samples.testcontainers.entity.tables.records.PersonRecord;
import cz.bedla.samples.testcontainers.repository.CountryRepository;
import cz.bedla.samples.testcontainers.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.Validate.notNull;

@Service
@Transactional
public class PersonService {
    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    public PersonService(PersonRepository personRepository, CountryRepository countryRepository) {
        this.personRepository = notNull(personRepository, "personRepository cannot be null");
        this.countryRepository = notNull(countryRepository, "countryRepository cannot be null");
    }

    public List<Person> listPersons() {
        var persons = personRepository.findPersons();
        if (persons.isEmpty()) {
            return List.of();
        } else {
            var countryIndex = findCountryIndex(persons.stream()
                    .map(PersonRecord::getCountryId)
                    .toList());

            return persons.stream().map(it -> Person.from(it, countryIndex)).toList();
        }
    }

    public Optional<Person> findPerson(int personId) {
        return personRepository.findPersonById(personId)
                .map(it -> {
                    var countryId = it.getCountryId();
                    var countryOpt = countryRepository.findCountryById(countryId);
                    return Person.from(it, countryOpt.orElseThrow(() -> new CountryNotFoundException(countryId)));
                });
    }

    public int createPerson(String firstName, String lastName, int countryId) {
        return personRepository.createPerson(firstName, lastName, countryId);
    }

    public void updatePerson(int personId, String firstName, String lastName, int countryId) {
        personRepository.updatePerson(personId, firstName, lastName, countryId);
    }

    public void deletePerson(int PersonId) {
        personRepository.deletePerson(PersonId);
    }

    private Map<Integer, CountryRecord> findCountryIndex(List<Integer> countryIds) {
        return countryRepository.findCountriesByIds(countryIds).intoMap(Tables.COUNTRY.ID);
    }
}
