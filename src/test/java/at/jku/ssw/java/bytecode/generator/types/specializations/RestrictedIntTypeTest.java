package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.utils.IntRange.rangeIncl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RestrictedIntTypeTest {

    //-------------------------------------------------------------------------
    // region Test cases

    @ParameterizedTest(name = "''{0}'' is assignable from ''{1}''")
    @MethodSource("compatibleTypesProvider")
    public void testIsAssignableFrom(MetaType<?> that, MetaType<?> other) {
        assertThat(that.isAssignableFrom(other), is(true));
    }

    @ParameterizedTest(name = "''{0}'' is not assignable from ''{1}''")
    @MethodSource("incompatibleTypesProvider")
    public void testIsNotAssignableFrom(MetaType<?> that, MetaType<?> other) {
        assertThat(that.isAssignableFrom(other), is(false));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Argument providers

    private static Stream<Arguments> compatibleTypesProvider() {
        return Stream.of(
                arguments(INT, RestrictedIntType.INT),
                arguments(RestrictedIntType.INT, RestrictedIntType.INT),
                arguments(INT, only(1, 2, 3, 4)),
                arguments(LONG, only(Integer.MAX_VALUE)),
                arguments(INT, only(Integer.MIN_VALUE, Integer.MAX_VALUE)),
                arguments(LONG, range(0, 1)),
                arguments(INT, range(-100, -99)),
                arguments(LONG, range(Integer.MIN_VALUE, Integer.MAX_VALUE)),
                arguments(LONG, range(0, 1_000_000_000)),
                arguments(range(0, 1), only(0, 1)),
                arguments(range(-100_000, 100_000), range(10_000, 20_000)),
                arguments(RestrictedIntType.INT, range(Integer.MIN_VALUE, 0)),
                arguments(only(100, -9, 10, 12, 2), only(2, 12))
        );
    }

    private static Stream<Arguments> incompatibleTypesProvider() {
        return Stream.of(
                arguments(RestrictedIntType.INT, INT),
                arguments(RestrictedIntType.INT, FLOAT),
                arguments(range(0, Integer.MAX_VALUE), range(-1, Integer.MAX_VALUE - 1)),
                arguments(only(1, 2), range(2, 100)),
                arguments(range(-10, 10), range(-11, 9))
        );
    }

    private static RestrictedIntType only(int... inclusions) {
        return RestrictedIntType.of(
                Arrays.stream(inclusions)
                        .boxed()
                        .collect(Collectors.toSet())
        );
    }

    private static RestrictedIntType range(int from, int to) {
        return RestrictedIntType.of(rangeIncl(from, to));
    }

    // endregion
    //-------------------------------------------------------------------------

}
