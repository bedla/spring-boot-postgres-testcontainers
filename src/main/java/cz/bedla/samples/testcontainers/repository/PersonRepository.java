package cz.bedla.samples.testcontainers.repository;

import cz.bedla.samples.testcontainers.service.exception.PersonInvalidCountryIdException;
import cz.bedla.samples.testcontainers.service.exception.PersonNotFoundException;
import cz.bedla.samples.testcontainers.entity.tables.records.PersonRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static cz.bedla.samples.testcontainers.entity.Tables.PERSON;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class PersonRepository {
    private final DSLContext dslContext;

    public PersonRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Result<PersonRecord> findPersons() {
        return dslContext.selectFrom(PERSON)
                .orderBy(PERSON.LASTNAME, PERSON.FIRSTNAME)
                .fetch();
    }

    public Optional<PersonRecord> findPersonById(int personId) {
        return Optional.ofNullable(dslContext.selectFrom(PERSON)
                .where(PERSON.ID.eq(personId))
                .fetchOne());
    }

    public int createPerson(String firstName, String lastName, int countryId) {
        return dbAction(countryId, () -> {
            var result = dslContext.insertInto(PERSON, PERSON.FIRSTNAME, PERSON.LASTNAME, PERSON.COUNTRY_ID)
                    .values(firstName, lastName, countryId)
                    .returningResult(PERSON.ID)
                    .fetchOne();
            return result.value1();
        });
    }

    public void updatePerson(int personId, String firstName, String lastName, int countryId) {
        var personRecord = dslContext.fetchOne(PERSON, PERSON.ID.eq(personId));
        if (personRecord == null) {
            throw new IllegalArgumentException("Unable to update, person.id=" + personId + " not found");
        } else {
            personRecord.setFirstname(firstName);
            personRecord.setLastname(lastName);
            personRecord.setCountryId(countryId);
            dbAction(countryId, personRecord::update);
        }
    }

    public void deletePerson(int personId) {
        var deletedCount = dslContext.delete(PERSON)
                .where(PERSON.ID.eq(personId))
                .execute();
        if (deletedCount != 1) {
            throw new PersonNotFoundException(personId);
        }
    }

    private <T> T dbAction(int countryId, Supplier<T> action) {
        return DbUtils.dbAction(action, List.of(
                (e) -> {
                    var isInvalidCountryId = isInvalidFKViolationException(e, "person", "fk_person_country");
                    if (isInvalidCountryId) {
                        return new PersonInvalidCountryIdException(countryId, e);
                    } else {
                        return null;
                    }
                }
        ));
    }

    private boolean isInvalidFKViolationException(DataIntegrityViolationException e, String table, String constraint) {
        var rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof PSQLException pgsqlE) {
            return pgsqlE.getServerErrorMessage() != null &&
                    PGStateIndex.findByStateId(pgsqlE.getSQLState()).filter(it -> it == PSQLState.FOREIGN_KEY_VIOLATION).isPresent() &&
                    table.equals(pgsqlE.getServerErrorMessage().getTable()) &&
                    constraint.equals(pgsqlE.getServerErrorMessage().getConstraint());
        }

        return false;
    }
}
