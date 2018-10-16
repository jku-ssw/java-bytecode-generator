package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RandomizerTest {

    private static final int REPETITIONS = 20;
    private Random rand;

    @BeforeEach
    public void setUp() {
        rand = new Random();
    }

    @AfterEach
    public void tearDown() {
        rand = new Random();
    }


    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNullSequences() {
        assertThat(new Randomizer(rand).oneNotNullOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForEmptySequences() {
        assertThat(new Randomizer(rand).oneNotNullOf(), is(opt(null)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNonNullValue() {
        assertThat(new Randomizer(rand).oneNotNullOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForNonNullValues() {
        assertThat(new Randomizer(rand).oneNotNullOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForMixedValues() {
        assertThat(
                new Randomizer(rand).<Object>oneNotNullOf(ret(0), ret(null)),
                is(opt(0))
        );
        assertThat(
                new Randomizer(rand).<Object>oneNotNullOf(ret(null), ret(1), ret(null)),
                is(opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(new Randomizer(rand).
                        oneNotNullOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2))
        );
        assertThat(modifications.size(), isOneOf(1, 2, 3));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNoValues() {
        assertThat(new Randomizer(rand).oneOf(new Object[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullValue() {
        assertThat(new Randomizer(rand).oneOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNotNullValues() {
        assertThat(new Randomizer(rand).oneOf(0, 1, 2, 3), isOneOf(opt(0), opt(1), opt(2), opt(3)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForStatefulProcedures() {
        Set<Integer> modifications = new HashSet<>();

        new Randomizer(rand).oneOf(getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullResults() {
        assertThat(new Randomizer(rand).oneOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNoFunctions() {
        assertThat(new Randomizer(rand).oneOf(new Supplier[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNonNullResult() {
        assertThat(new Randomizer(rand).oneOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNonNullResults() {
        assertThat(new Randomizer(rand).oneOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                new Randomizer(rand).oneOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2), empty())
        );
        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfZeroOptions() {
        Set<Integer> modifications = new HashSet<>();

        new Randomizer(rand).doOneOfOptions(0, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(0));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfTooManyOptions() {
        Set<Integer> modifications = new HashSet<>();

        new Randomizer(rand).doOneOfOptions(10, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfTooFewOptions() {
        Set<Integer> modifications = new HashSet<>();

        new Randomizer(rand).doOneOfOptions(2, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
        assertThat(modifications, either(contains(0)).or(contains(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfZeroOptions() {
        assertThat(new Randomizer(rand).oneOfOptions(0, 1, 2, 3), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfTooManyOptions() {
        assertThat(new Randomizer(rand).oneOfOptions(10, 1, 2, 3), isOneOf(opt(1), opt(2), opt(3)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfTooFewOptions() {
        assertThat(new Randomizer(rand).oneOfOptions(2, 1, 2, 3), isOneOf(opt(1), opt(2)));
    }

    @SuppressWarnings("unchecked")
    @RepeatedTest(value = REPETITIONS)
    public void testWithProbabilities() {
        assertThat(new Randomizer(rand).
                        withProbabilities(new int[]{1, 2}, ret(1), ret(2)),
                isOneOf(opt(1), opt(2))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testWithProbabilitiesAndTooFewValues() {
        assertThat(new Randomizer(rand).withProbabilities(new int[]{1, 2}, ret(1)), isOneOf(opt(1), empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testWithProbabilitiesAndTooManyValues() {
        assertThat(new Randomizer(rand).
                        withProbabilities(new int[]{1, 2, 3}, ret(1), ret(2), ret(3), ret(4)),
                isOneOf(opt(1), opt(2), opt(3))
        );
    }

    @SuppressWarnings("unchecked")
    private Supplier<Object>[] getStatefulSuppliers(Set<Integer> modifications) {
        return new Supplier[]{
                () -> {
                    modifications.add(0);
                    return 0;
                },
                () -> {
                    modifications.add(1);
                    return 1;
                },
                () -> {
                    modifications.add(2);
                    return 2;
                },
                () -> {
                    modifications.add(3);
                    return null;
                },
                () -> {
                    modifications.add(4);
                    return null;
                }
        };
    }

    private Runnable[] getStatefulRunnables(Set<Integer> modifications) {
        return new Runnable[]{
                () -> modifications.add(0),
                () -> modifications.add(1),
                () -> modifications.add(2),
                () -> modifications.add(3)
        };
    }

    private <T> Supplier<T> ret(T value) {
        return () -> value;
    }

    private <T> Optional<T> opt(T value) {
        return Optional.ofNullable(value);
    }
}
