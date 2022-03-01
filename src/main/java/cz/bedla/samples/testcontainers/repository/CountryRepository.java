package cz.bedla.samples.testcontainers.repository;

import cz.bedla.samples.testcontainers.service.exception.CountryNotFoundException;
import cz.bedla.samples.testcontainers.entity.tables.records.CountryRecord;
import org.apache.commons.lang3.Validate;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static cz.bedla.samples.testcontainers.entity.Tables.COUNTRY;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class CountryRepository {
    private final DSLContext dslContext;

    public CountryRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Result<CountryRecord> findCountries() {
        return dslContext.selectFrom(COUNTRY)
                .orderBy(COUNTRY.NAME)
                .fetch();
    }

    public Result<CountryRecord> findCountriesByIds(List<Integer> ids) {
        Validate.isTrue(!CollectionUtils.isEmpty(ids), "ids cannot be empty");
        return dslContext.selectFrom(COUNTRY)
                .where(COUNTRY.ID.in(ids))
                .orderBy(COUNTRY.NAME)
                .fetch();
    }

    public Optional<CountryRecord> findCountryById(int countryId) {
        return Optional.ofNullable(dslContext.selectFrom(COUNTRY)
                .where(COUNTRY.ID.eq(countryId))
                .fetchOne());
    }

    public int createCountry(String name) {
        var result = dslContext.insertInto(COUNTRY, COUNTRY.NAME)
                .values(name)
                .returningResult(COUNTRY.ID)
                .fetchOne();
        return result.value1();
    }

    public void updateCountry(int countryId, String name) {
        var countryRecord = dslContext.fetchOne(COUNTRY, COUNTRY.ID.eq(countryId));
        if (countryRecord == null) {
            throw new CountryNotFoundException(countryId);
        } else {
            countryRecord.setName(name);
            countryRecord.update();
        }
    }

    public void deleteCountry(int countryId) {
        var deletedCount = dslContext.delete(COUNTRY)
                .where(COUNTRY.ID.eq(countryId))
                .execute();
        if (deletedCount != 1) {
            throw new CountryNotFoundException(countryId);
        }
    }
}
