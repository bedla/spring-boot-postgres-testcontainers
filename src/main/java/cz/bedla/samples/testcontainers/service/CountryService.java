package cz.bedla.samples.testcontainers.service;

import cz.bedla.samples.testcontainers.entity.Country;
import cz.bedla.samples.testcontainers.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CountryService {
    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> listCountries() {
        return countryRepository.findCountries()
                .stream()
                .map(Country::from)
                .toList();
    }

    public Optional<Country> getCountry(int countryId) {
        return countryRepository.findCountryById(countryId)
                .map(Country::from);
    }

    public int createCountry(String name) {
        return countryRepository.createCountry(name);
    }

    public void updateCountry(int countryId, String name) {
        countryRepository.updateCountry(countryId, name);
    }

    public void deleteCountry(int countryId) {
        countryRepository.deleteCountry(countryId);
    }
}
