package cz.bedla.samples.testcontainers.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public record CountryPersonStatistics(Country country, int personsCount) {
}
