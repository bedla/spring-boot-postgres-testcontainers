package cz.bedla.samples.testcontainers.repository;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static cz.bedla.samples.testcontainers.entity.Tables.COUNTRY;
import static cz.bedla.samples.testcontainers.entity.Tables.PERSON;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class StatisticsRepository {
    private final DSLContext dslContext;

    public StatisticsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Result<Record2<Integer, Integer>> calculateTopCountriesWithMostPersons(int topCount) {
        return dslContext.select(COUNTRY.ID, count(PERSON.ID).as(name("personCount")))
                .from(COUNTRY)
                .leftOuterJoin(PERSON).on(PERSON.COUNTRY_ID.eq(COUNTRY.ID))
                .groupBy(COUNTRY.ID)
                .orderBy(field(name("personCount")).desc(), COUNTRY.NAME.asc())
                .limit(topCount)
                .fetch();
    }
}
