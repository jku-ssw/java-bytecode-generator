package at.jku.ssw.java.bytecode.generator.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    public static <T> Optional<T> oneNotNullOf(Supplier<T>... suppliers) {
        return stream(suppliers)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny();
    }

    /**
     * Executes one of the given procedures and returns the result.
     *
     * @param suppliers The functions that are executed
     * @param <T>       The expected return type
     * @return The result of the first executed supplier
     */
    @SafeVarargs
    public static <T> Optional<T> oneOf(Supplier<T>... suppliers) {
        return stream(suppliers)
                .map(Supplier::get)
                .map(Optional::ofNullable)
                .findAny()
                .flatMap(Function.identity());
    }

    /**
     * Executes one of the given procedures.
     *
     * @param suppliers The functions that are executed
     */
    public static void oneOf(Runnable... suppliers) {
        stream(suppliers)
                .findAny()
                .ifPresent(Runnable::run);
    }

    @SafeVarargs
    public static <T> Stream<T> stream(T... args) {
        List<T> l = Arrays.asList(args);

        Collections.shuffle(l);

        return l.stream();
    }

}
