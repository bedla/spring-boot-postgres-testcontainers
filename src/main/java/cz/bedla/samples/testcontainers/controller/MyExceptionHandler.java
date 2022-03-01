package cz.bedla.samples.testcontainers.controller;

import cz.bedla.samples.testcontainers.service.exception.CountryNotFoundException;
import cz.bedla.samples.testcontainers.service.exception.PersonInvalidCountryIdException;
import cz.bedla.samples.testcontainers.service.exception.PersonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<Object> countryNotFoundHandler(CountryNotFoundException e) {
        log.error("Country.id={} not found", e.getCountryId(), e);
        return ResponseEntity.notFound()
                .build();
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<Object> personNotFoundHandler(PersonNotFoundException e) {
        log.error("Person.id={} not found", e.getPersonId(), e);
        return ResponseEntity.notFound()
                .build();
    }

    @ExceptionHandler(PersonInvalidCountryIdException.class)
    public ResponseEntity<Object> personInvalidCountryIdHandler(PersonInvalidCountryIdException e) {
        log.error("Invalid country.id={} for Person", e.getCountryId(), e);
        return ResponseEntity.badRequest()
                .build();
    }
}
