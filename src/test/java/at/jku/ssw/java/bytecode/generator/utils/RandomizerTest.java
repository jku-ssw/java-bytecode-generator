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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class RandomizerTest {

    @RepeatedTest(value = 10)
    public void testOneNotNullOfForNullSequences() {
        assertThat(oneNotNullOf(() -> null), is(empty()));
    }

    @RepeatedTest(value = 10)
    public void testOneNotNullOfForEmptySequences() {
        assertThat(oneNotNullOf(), is(opt(null)));
    }

    @RepeatedTest(value = 10)
    public void testOneNotNullOfForNonNullValue() {
        assertThat(oneNotNullOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = 10)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForNonNullValues() {
        assertThat(oneNotNullOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = 10)
    public void testOneNotNullOfForMixedValues() {
        assertThat(Randomizer.<Object>oneNotNullOf(ret(0), ret(null)), is(opt(0)));
        assertThat(Randomizer.<Object>oneNotNullOf(ret(null), ret(1), ret(null)), is(opt(1)));
    }

    @RepeatedTest(value = 10)
    @SuppressWarnings("unchecked")
    public void testOneNotNullOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                Randomizer.oneNotNullOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2))
        );
        assertThat(modifications.size(), isOneOf(1, 2, 3));
    }

    @RepeatedTest(value = 10)
    public void testOneOfForStatefulProcedures() {
        Set<Integer> modifications = new HashSet<>();

        Randomizer.oneOf(
                () -> modifications.add(0),
                () -> modifications.add(1),
                () -> modifications.add(2)
        );

        assertThat(modifications.size(), is(1));
    }

    @RepeatedTest(value = 10)
    public void testOneOfForNullSequences() {
        assertThat(oneOf(() -> null), is(empty()));
    }

    @RepeatedTest(value = 10)
    @SuppressWarnings("unchecked")
    public void testOneOfForEmptySequences() {
        assertThat(oneOf(new Supplier[0]), is(empty()));
    }

    @RepeatedTest(value = 10)
    public void testOneOfForNonNullValue() {
        assertThat(oneOf(ret(0)), is(opt(0)));
    }

    @RepeatedTest(value = 10)
    @SuppressWarnings("unchecked")
    public void testOneOfForNonNullValues() {
        assertThat(oneOf(ret(0), ret(1)), isOneOf(opt(0), opt(1)));
    }

    @RepeatedTest(value = 10)
    @SuppressWarnings("unchecked")
    public void testOneOfForStatefulFunctions() {
        Set<Integer> modifications = new HashSet<>();

        assertThat(
                Randomizer.oneOf(getStatefulSuppliers(modifications)),
                isOneOf(opt(0), opt(1), opt(2), empty())
        );
        assertThat(modifications.size(), is(1));
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

    private <T> Supplier<T> ret(T value) {
        return () -> value;
    }

    private <T> Optional<T> opt(T value) {
        return Optional.ofNullable(value);
    }
}
