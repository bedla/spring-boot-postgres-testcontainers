package cz.bedla.samples.testcontainers.service.exception;

public class PersonNotFoundException extends RuntimeException {
    private final int personId;

    public PersonNotFoundException(int personId) {
        super("Unable to find person.id=" + personId);
        this.personId = personId;
    }

    public int getPersonId() {
        return personId;
    }
}
