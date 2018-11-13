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
    private Randomizer randomizer;

    @BeforeEach
    public void setUp() {
        rand = new Random();
        randomizer = new Randomizer(rand);
    }

    @AfterEach
    public void tearDown() {
        randomizer = null;
        rand = null;
    }


    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNullSequences() {
        assertThat(randomizer.oneNotNullOf(none()), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForEmptySequences() {
        assertThat(randomizer.oneNotNullOf(), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNonNullValue() {
        assertThat(randomizer.oneNotNullOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForNonNullValues() {
        assertThat(
                randomizer.oneNotNullOf(ret(0), ret(1)),
                isOneOf(opt(0), opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForMixedValues() {
        assertThat(
                randomizer.<Object>oneNotNullOf(ret(0), none()),
                is(opt(0))
        );
        assertThat(
                randomizer.<Object>oneNotNullOf(none(), ret(1), none()),
                is(opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(randomizer.
                        oneNotNullOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2))
        );
        assertThat(modifications.size(), isOneOf(1, 2, 3));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNoValues() {
        assertThat(randomizer.oneOf(new Object[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullValue() {
        assertThat(randomizer.oneOf(none()), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNotNullValues() {
        assertThat(
                randomizer.oneOf(0, 1, 2, 3),
                isOneOf(opt(0), opt(1), opt(2), opt(3))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForStatefulProcedures() {
        Set<Integer> modifications = new HashSet<>();

        randomizer.oneOf(getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullResults() {
        assertThat(randomizer.oneOf(none()), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNoFunctions() {
        assertThat(randomizer.oneOf(new Supplier[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNonNullResult() {
        assertThat(randomizer.oneOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNonNullResults() {
        assertThat(randomizer.oneOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                randomizer.oneOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2), empty())
        );
        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfZeroOptions() {
        Set<Integer> modifications = new HashSet<>();

        randomizer.doOneOfOptions(0, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(0));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfTooManyOptions() {
        Set<Integer> modifications = new HashSet<>();

        randomizer.doOneOfOptions(10, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testDoOneOfTooFewOptions() {
        Set<Integer> modifications = new HashSet<>();

        randomizer.doOneOfOptions(2, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
        assertThat(modifications, either(contains(0)).or(contains(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfNegativeOptions() {
        assertThat(randomizer.oneOfOptions(-99, 1, 2, 3), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfZeroOptions() {
        assertThat(randomizer.oneOfOptions(0, 1, 2, 3), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfTooManyOptions() {
        assertThat(
                randomizer.oneOfOptions(10, 1, 2, 3),
                isOneOf(opt(1), opt(2), opt(3))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfTooFewOptions() {
        assertThat(
                randomizer.oneOfOptions(2, 1, 2, 3),
                isOneOf(opt(1), opt(2))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void tetOneNotNullOfZeroOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(0, ret(1), ret(2), ret(3)),
                is(empty())
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void tetOneNotNullOfNegativeOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(-99, ret(1), ret(2), ret(3)),
                is(empty())
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfTooManyOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(10, ret(1), ret(2), ret(3)),
                isOneOf(opt(1), opt(2), opt(3))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfTooFewOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(2, ret(1), ret(2), ret(3)),
                isOneOf(opt(1), opt(2))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfOnlyNullOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(1, none()),
                is(empty())
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfNoOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(1000),
                is(empty())
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfNonNullOption() {
        assertThat(
                randomizer.oneNotNullOfOptions(2, ret(0)),
                is(opt(0))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfNonNullOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(3, ret(0), ret(1)),
                isOneOf(opt(0), opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfMixedOptions() {
        assertThat(
                randomizer.oneNotNullOfOptions(5, ret(0), none()),
                is(opt(0))
        );
        assertThat(
                randomizer.oneNotNullOfOptions(3, none(), ret(1), none()),
                is(opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                randomizer.oneNotNullOfOptions(
                        10,
                        getStatefulSuppliers(modifications)
                ),
                isOneOf(opt(0), opt(1), opt(2))
        );
        assertThat(modifications.size(), isOneOf(1, 2, 3));
    }

    @SuppressWarnings("unchecked")
    @RepeatedTest(value = REPETITIONS)
    public void testWithProbabilities() {
        assertThat(randomizer.
                        withProbabilities(new int[]{1, 2}, ret(1), ret(2)),
                isOneOf(opt(1), opt(2))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testWithProbabilitiesAndTooFewValues() {
        assertThat(
                randomizer.withProbabilities(new int[]{1, 2}, ret(1)),
                isOneOf(opt(1), empty())
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testWithProbabilitiesAndTooManyValues() {
        assertThat(
                randomizer.withProbabilities(
                        new int[]{1, 2, 3},
                        ret(1), ret(2), ret(3), ret(4)
                ),
                isOneOf(opt(1), opt(2), opt(3))
        );
    }

    @SuppressWarnings("unchecked")
    private Supplier<Object>[] getStatefulSuppliers(Set<Integer> mods) {
        return new Supplier[]{
                () -> {
                    mods.add(0);
                    return 0;
                },
                () -> {
                    mods.add(1);
                    return 1;
                },
                () -> {
                    mods.add(2);
                    return 2;
                },
                () -> {
                    mods.add(3);
                    return null;
                },
                () -> {
                    mods.add(4);
                    return null;
                }
        };
    }

    private Runnable[] getStatefulRunnables(Set<Integer> mods) {
        return new Runnable[]{
                () -> mods.add(0),
                () -> mods.add(1),
                () -> mods.add(2),
                () -> mods.add(3)
        };
    }

    private static final Supplier<?> NONE = () -> null;

    @SuppressWarnings("unchecked")
    private <T> Supplier<T> none() {
        return (Supplier<T>) NONE;
    }

    private <T> Supplier<T> ret(T value) {
        return () -> value;
    }

    private <T> Optional<T> opt(T value) {
        return Optional.ofNullable(value);
    }
}
