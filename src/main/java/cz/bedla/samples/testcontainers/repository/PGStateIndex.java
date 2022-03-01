package cz.bedla.samples.testcontainers.repository;

import org.postgresql.util.PSQLState;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public final class PGStateIndex {
    private PGStateIndex() {
    }

    private static final Map<String, PSQLState> index = Arrays.stream(PSQLState.values())
            .collect(toMap(PSQLState::getState, Function.identity()));

    public static Optional<PSQLState> findByStateId(String id) {
        return Optional.ofNullable(index.get(id));
    }
}
