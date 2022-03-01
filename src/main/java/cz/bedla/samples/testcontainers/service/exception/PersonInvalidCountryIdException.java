package cz.bedla.samples.testcontainers.service.exception;

public class PersonInvalidCountryIdException extends RuntimeException {
    private final int countryId;

    public PersonInvalidCountryIdException(int countryId, Throwable cause) {
        super("Unable to find country.id=" + countryId + " for person", cause);
        this.countryId = countryId;
    }

    public int getCountryId() {
        return countryId;
    }
}
