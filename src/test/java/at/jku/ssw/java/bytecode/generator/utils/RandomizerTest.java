package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static at.jku.ssw.java.bytecode.generator.utils.Randomizer.shuffledUntilNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class RandomizerTest {

    @Test
    public void testShuffledUntilNonNullForNullSequences() {
        assertThat(shuffledUntilNotNull(() -> null), is(opt(null)));
    }

    @Test
    public void testShuffledUntilNonNullForEmptySequences() {
        assertThat(shuffledUntilNotNull(), is(opt(null)));
    }

    @Test
    public void testShuffledUntilNonNullForNonNullValue() {
        assertThat(shuffledUntilNotNull(ret(0)), is(opt(0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShuffledUntilNonNullForNonNullValues() {
        assertThat(shuffledUntilNotNull(ret(0), ret(1)), is(isOneOf(opt(0), opt(1))));
    }

    @Test
    public void testShuffledUntilNonNullForMixedValues() {
        assertThat(Randomizer.<Object>shuffledUntilNotNull(ret(0), ret(null)), is(opt(0)));
        assertThat(Randomizer.<Object>shuffledUntilNotNull(ret(null), ret(1), ret(null)), is(opt(1)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShuffledUntilNonNullForStatefulProcedures() {
        Set<Integer> modifications = new HashSet<>();

        Supplier[] suppliers = new Supplier[] {
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
                }
        };

        assertThat(Randomizer.shuffledUntilNotNull(suppliers), is(isOneOf(opt(0), opt(1), opt(2))));
        assertThat(modifications.size(), is(1));
    }

    private <T> Supplier<T> ret(T value) {
        return () -> value;
    }

    private <T> Optional<T> opt(T value) {
        return Optional.ofNullable(value);
    }
}
