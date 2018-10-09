package at.jku.ssw.java.bytecode.generator.utils;

import java.util.*;
import java.util.function.Supplier;

/**
 * Provides functions to randomize code generation.
 */
public class Randomizer {

    /**
     * Executes the given number of suppliers in any order until a non-null
     * result is found. Each supplier is thereby executed at most once.
     *
     * @param suppliers The functions that are executed
     * @param <T>       The expected return type
     * @return The first result of the calls that is not null
     * or {@link Optional#EMPTY} if all results are null
     */
    @SafeVarargs
    public static <T> Optional<T> shuffledUntilNotNull(Supplier<T>... suppliers) {
        List<Supplier<T>> l = Arrays.asList(suppliers);

        Collections.shuffle(l);

        return Arrays.stream(suppliers)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny();
    }
}
