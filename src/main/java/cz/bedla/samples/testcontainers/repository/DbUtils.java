package cz.bedla.samples.testcontainers.repository;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbUtils {
    private DbUtils() {
    }

    public static <T, E extends RuntimeException> T dbAction(Supplier<T> action, List<Function<DataIntegrityViolationException, E>> exceptionMappers) {
        try {
            return action.get();
        } catch (DataIntegrityViolationException e) {
            for (Function<DataIntegrityViolationException, E> mapper : exceptionMappers) {
                var newException = mapper.apply(e);
                if (newException != null) {
                    throw newException;
                }
            }
            throw e;
        }
    }
}
