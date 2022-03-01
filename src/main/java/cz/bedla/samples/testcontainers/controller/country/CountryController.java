package cz.bedla.samples.testcontainers.controller.country;

import cz.bedla.samples.testcontainers.controller.country.dto.CountryDto;
import cz.bedla.samples.testcontainers.controller.country.dto.CreateCountryRequest;
import cz.bedla.samples.testcontainers.controller.country.dto.UpdateCountryRequest;
import cz.bedla.samples.testcontainers.service.CountryService;
import cz.bedla.samples.testcontainers.service.exception.CountryNotFoundException;
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
public class CountryController {
    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/country")
    public List<CountryDto> allCountries() {
        return countryService.listCountries().stream()
                .map(CountryDto::from)
                .toList();
    }

    @PostMapping("/country")
    public ResponseEntity<Object> createCountry(
            @Validated @RequestBody CreateCountryRequest request
    ) {
        var newPk = countryService.createCountry(request.name());
        return ResponseEntity.created(URI.create("/api/v1/country/" + newPk))
                .build();
    }

    @PutMapping("/country/{countryId}")
    public ResponseEntity<?> updateCountry(
            @PathVariable("countryId") int countryId,
            @Validated @RequestBody UpdateCountryRequest request
    ) {
        countryService.updateCountry(countryId, request.name());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<CountryDto> getCountry(
            @PathVariable("countryId") int countryId
    ) {
        return countryService.getCountry(countryId)
                .map(CountryDto::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CountryNotFoundException(countryId));
    }

    @DeleteMapping("/country/{countryId}")
    public ResponseEntity<?> deleteCountry(
            @PathVariable("countryId") int countryId
    ) {
        countryService.deleteCountry(countryId);
        return ResponseEntity.noContent().build();
    }
}
