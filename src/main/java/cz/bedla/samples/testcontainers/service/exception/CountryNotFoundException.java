package cz.bedla.samples.testcontainers.service.exception;

public class CountryNotFoundException extends RuntimeException {
    private final int countryId;

    public CountryNotFoundException(int countryId) {
        super("Unable to find country.id=" + countryId);
        this.countryId = countryId;
    }

    public int getCountryId() {
        return countryId;
    }
}
