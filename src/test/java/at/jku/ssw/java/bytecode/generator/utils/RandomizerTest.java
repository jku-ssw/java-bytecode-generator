package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static at.jku.ssw.java.bytecode.generator.utils.Randomizer.oneNotNullOf;
import static at.jku.ssw.java.bytecode.generator.utils.Randomizer.oneOf;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RandomizerTest {

    private static final int REPETITIONS = 10;

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNullSequences() {
        assertThat(oneNotNullOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForEmptySequences() {
        assertThat(oneNotNullOf(), is(opt(null)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForNonNullValue() {
        assertThat(oneNotNullOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForNonNullValues() {
        assertThat(oneNotNullOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneNotNullOfForMixedValues() {
        assertThat(
                Randomizer.<Object>oneNotNullOf(ret(0), ret(null)),
                is(opt(0))
        );
        assertThat(
                Randomizer.<Object>oneNotNullOf(ret(null), ret(1), ret(null)),
                is(opt(1))
        );
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                oneNotNullOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2))
        );
        assertThat(modifications.size(), isOneOf(1, 2, 3));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNoValues() {
        assertThat(oneOf(new Object[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullValue() {
        assertThat(oneOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNotNullValues() {
        assertThat(oneOf(0, 1, 2, 3), isOneOf(opt(0), opt(1), opt(2), opt(3)));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForStatefulProcedures() {
        Set<Integer> modifications = new HashSet<>();

        oneOf(getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNullResults() {
        assertThat(oneOf(ret(null)), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNoFunctions() {
        assertThat(oneOf(new Supplier[0]), is(empty()));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfForNonNullResult() {
        assertThat(oneOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForNonNullResults() {
        assertThat(oneOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = REPETITIONS)
    @SuppressWarnings("unchecked")
    public void testOneOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                oneOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2), empty())
        );
        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfZeroOptions() {
        Set<Integer> modifications = new HashSet<>();

        Randomizer.oneOfOptions(0, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(0));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfTooManyOptions() {
        Set<Integer> modifications = new HashSet<>();

        Randomizer.oneOfOptions(10, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = REPETITIONS)
    public void testOneOfTooFewOptions() {
        Set<Integer> modifications = new HashSet<>();

        Randomizer.oneOfOptions(2, getStatefulRunnables(modifications));

        assertThat(modifications.size(), is(1));
        assertThat(modifications, either(contains(0)).or(contains(1)));
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
