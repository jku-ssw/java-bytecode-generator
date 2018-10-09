package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static at.jku.ssw.java.bytecode.generator.utils.Randomizer.shuffledUntilNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class RandomizerTest {

    @Test
    public void testShuffleUntilNonNullForNullSequences() {
        assertThat(shuffledUntilNotNull(() -> null), is(Optional.empty()));
    }

    @Test
    public void testShuffleUntilNonNullForEmptySequences() {
        assertThat(shuffledUntilNotNull(), is(Optional.empty()));
    }

    @Test
    public void testShuffleUntilNonNullForNonNullValue() {
        assertThat(shuffledUntilNotNull(() -> 0), is(Optional.of(0)));
    }

    @Test
    public void testShuffleUntilNonNullForNonNullValues() {
        assertThat(shuffledUntilNotNull(() -> 0, () -> 1), is(isOneOf(Optional.of(0), Optional.of(1))));
    }

    @Test
    public void testShuffleUntilNonNullForMixedValues() {
        assertThat(Randomizer.<Object>shuffledUntilNotNull(() -> 0, () -> null), is(Optional.of(0)));
        assertThat(Randomizer.<Object>shuffledUntilNotNull(() -> null, () -> 1, () -> null), is(Optional.of(1)));
    }
}
