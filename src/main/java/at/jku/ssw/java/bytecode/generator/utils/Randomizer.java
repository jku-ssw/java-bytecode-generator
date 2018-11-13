package at.jku.ssw.java.bytecode.generator.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

/**
 * Provides functions to randomize code generation.
 */
public final class Randomizer {

    private final Random rand;

    public Randomizer(Random rand) {
        this.rand = rand;
    }

    /**
     * Executes the given number of suppliers in any order until a
     * non-{@code null} result is found.
     * Each supplier is thereby executed at most once.
     *
     * @param suppliers The functions that are executed
     * @param <T>       The expected return type
     * @return The first result of the calls that is not {@code null}
     * or {@link Optional#EMPTY} if all results are {@code null}
     */
    @SafeVarargs
    public final <T> Optional<T> oneNotNullOf(Supplier<T>... suppliers) {
        return stream(suppliers)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * @see #oneOf(Object[])
     */
    public final <T> Optional<T> oneOf(Collection<T> values) {
        return values.isEmpty()
                ? Optional.empty()
                : values.stream()
                .skip(rand.nextInt(values.size()))
                .findFirst();
    }

    /**
     * @see #oneOf(Object[])
     */
    public final <T> Optional<T> oneOf(Stream<T> stream) {
        return oneOf(stream.collect(Collectors.toList()));
    }

    /**
     * Returns one of the given values.
     * If the picked values is {@code null}, a {@link NullPointerException} is
     * thrown.
     *
     * @param values The values
     * @param <T>    The type of the values
     * @return one of the given values, or {@link Optional#EMPTY} if
     * no values are given
     */
    @SafeVarargs
    public final <T> Optional<T> oneOf(T... values) {
        return skipRandom(values)
                .findFirst();
    }

    /**
     * Executes one of the given procedures and returns the result.
     *
     * @param suppliers The functions that are executed
     * @param <T>       The expected return type
     * @return The result of the first executed supplier
     */
    @SafeVarargs
    public final <T> Optional<T> oneOf(Supplier<T>... suppliers) {
        return skipRandom(suppliers)
                .map(Supplier::get)
                .map(Optional::ofNullable)
                .findFirst()
                .flatMap(Function.identity());
    }

    /**
     * Executes one of the given procedures.
     *
     * @param runnables The functions that are executed
     */
    public void oneOf(Runnable... runnables) {
        skipRandom(runnables)
                .findFirst()
                .ifPresent(Runnable::run);
    }

    /**
     * Executes one of the given procedures but uses the given number of
     * potential options to calculate the probability.
     * If the defined number of options exceeds the actually passed arguments,
     * the last argument is repeated to increase its chances.
     * If the defined number of options is lower than the passed arguments,
     * only the given number of functions are considered.
     *
     * @param options   The number of potential options
     * @param runnables The functions that are executed
     */
    public final void doOneOfOptions(int options, Runnable... runnables) {
        oneOfOptions(options, runnables).ifPresent(Runnable::run);
    }

    /**
     * Returns one of the given values but uses the given number of potential
     * options to calculate the probability.
     * If the defined number of options exceeds the actually passed arguments,
     * the last argument is repeated to increase its chances.
     * If the defined number of options is less than the number of passed
     * arguments, only the given number of functions is considered.
     *
     * @param options The number of potential options
     * @param values  The selection of values
     * @param <T>     The type of the values
     * @return one of the values or none, if none are given
     */
    @SafeVarargs
    public final <T> Optional<T> oneOfOptions(int options, T... values) {
        if (values.length <= 0 || options <= 0)
            return Optional.empty();

        return range(0, options - values.length)
                .mapToObj(__ -> Stream.of(values[values.length - 1]))
                .reduce(Arrays.stream(values), Stream::concat)
                .limit(options)
                .skip(rand.nextInt(options))
                .findFirst();
    }

    /**
     * Executes the given number of suppliers using the specified number of
     * options but skipping over {@code null} values.
     * If the number of options is less than the number of passed
     * arguments, only the given number of options is considered.
     * If the number of options exceeds the actually passed arguments,
     * the last argument's chance is increased
     * (e.g. {@code options == 3} for 2 actual parameters results in argument
     * 1 having a chance of 1/3 and argument 2 having a chance of 2/3.
     *
     * @param options The number of potential options
     * @param values  The selection of values
     * @param <T>     The type of the values
     * @return the first non-{@code null} result of one of the suppliers;
     * returns nothing if no suppliers are given, the number of options is
     * not strictly positive or if no supplier returns a non-{@code null} value
     */
    @SafeVarargs
    public final <T> Optional<T> oneNotNullOfOptions(int options, Supplier<T>... values) {
        if (values.length <= 0 || options <= 0)
            return Optional.empty();

        return shuffle(
                range(0, options - values.length)
                        .mapToObj(__ -> Stream.of(values[values.length - 1]))
                        .reduce(Arrays.stream(values), Stream::concat)
                        .limit(options))
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * Returns a random stream of the given values.
     *
     * @param args The values to stream
     * @param <T>  The type of the elements
     * @return a random stream containing the given values
     */
    @SafeVarargs
    public final <T> Stream<T> stream(T... args) {
        return shuffle(Arrays.stream(args));
    }

    /**
     * Shuffles the elements of the given stream and returns a new one.
     * Warning: This method inevitable has to force the whole stream
     * and may therefore perform poorly on large streams.
     *
     * @param stream The stream to shuffle
     * @param <T>    The types of elements in the stream
     * @return a new stream containing the previous streams' elements
     * in a random order
     */
    public final <T> Stream<T> shuffle(Stream<T> stream) {
        List<T> l = stream.collect(Collectors.toList());
        Collections.shuffle(l, rand);

        return l.stream();
    }

    /**
     * Skips a random number of the given values and returns a stream
     *
     * @param args The values to stream
     * @param <T>  The type of the elements
     * @return a random stream that skips a random amount of the given elements
     */
    @SafeVarargs
    public final <T> Stream<T> skipRandom(T... args) {
        return args.length == 0
                ? Stream.empty()
                : Arrays.stream(args).skip(rand.nextInt(args.length));
    }

    /**
     * Executes one of the given functions where the probability for each
     * is given by the caller.
     *
     * @param p         The probabilities to execute a function
     * @param suppliers The functions to execute
     * @param <T>       The type of the returned value
     * @return the result of one of the functions or nothing, if no
     * functions are given
     */
    @SafeVarargs
    public final <T> Optional<T> withProbabilities(int[] p, Supplier<T>... suppliers) {
        assert Arrays.stream(p).allMatch(i -> i > 0) : "Probabilities must be greater than zero";

        if (p.length == 0)
            return Optional.empty();

        int i = 0;

        for (int r = rand.nextInt(IntStream.of(p).sum()); r >= 0; i++)
            r -= p[i];

        return i <= suppliers.length
                ? Optional.ofNullable(suppliers[i - 1].get())
                : Optional.empty();
    }
}
