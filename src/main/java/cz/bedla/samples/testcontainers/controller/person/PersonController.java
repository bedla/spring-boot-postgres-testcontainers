package cz.bedla.samples.testcontainers.controller.person;

import cz.bedla.samples.testcontainers.controller.person.dto.CreatePersonRequest;
import cz.bedla.samples.testcontainers.controller.person.dto.PersonDto;
import cz.bedla.samples.testcontainers.controller.person.dto.UpdatePersonRequest;
import cz.bedla.samples.testcontainers.service.PersonService;
import cz.bedla.samples.testcontainers.service.exception.PersonNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PersonController {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/person")
    public List<PersonDto> allPersons() {
        return personService.listPersons().stream()
                .map(PersonDto::from)
                .toList();
    }

    @PostMapping("/person")
    public ResponseEntity<Object> createPerson(
            @Validated @RequestBody CreatePersonRequest request
    ) {
        var newPk = personService.createPerson(request.firstName(), request.lastName(), request.countryId());
        return ResponseEntity.created(URI.create("/api/v1/person/" + newPk))
                .build();
    }

    @PutMapping("/person/{personId}")
    public ResponseEntity<?> updatePerson(
            @PathVariable("personId") int personId,
            @Validated @RequestBody UpdatePersonRequest request
    ) {
        personService.updatePerson(personId, request.firstName(), request.lastName(), request.countryId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<PersonDto> getPerson(
            @PathVariable("personId") int personId
    ) {
        return personService.findPerson(personId)
                .map(PersonDto::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PersonNotFoundException(personId));
    }

    @DeleteMapping("/person/{personId}")
    public ResponseEntity<?> deletePerson(
            @PathVariable("personId") int personId
    ) {
        personService.deletePerson(personId);
        return ResponseEntity.noContent().build();
    }
}
