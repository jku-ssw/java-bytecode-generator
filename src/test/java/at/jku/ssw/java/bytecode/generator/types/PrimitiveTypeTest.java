package at.jku.ssw.java.bytecode.generator.types;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class PrimitiveTypeTest {

    //-------------------------------------------------------------------------
    // region Test cases

    @ParameterizedTest(name = "''{0}'' is assignable from ''{1}''")
    @MethodSource("compatibleTypesProvider")
    public void testIsAssignableFrom(FieldVarType<?> that, FieldVarType<?> other) {
        assertThat(that.isAssignableFrom(other), is(true));
    }

    @ParameterizedTest(name = "''{0}'' is not assignable from ''{1}''")
    @MethodSource("incompatibleTypesProvider")
    public void testIsNotAssignableFrom(FieldVarType<?> that, FieldVarType<?> other) {
        assertThat(that.isAssignableFrom(other), is(false));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Argument providers

    private static Stream<Arguments> compatibleTypesProvider() {
        return Stream.of(
                arguments(BYTE, BYTE),
                arguments(SHORT, BYTE),
                arguments(SHORT, SHORT),
                arguments(INT, BYTE),
                arguments(INT, SHORT),
                arguments(INT, INT),
                arguments(LONG, INT),
                arguments(DOUBLE, FLOAT)
        );
    }

    private static Stream<Arguments> incompatibleTypesProvider() {
        return Stream.of(
                arguments(BYTE, SHORT),
                arguments(SHORT, INT),
                arguments(FLOAT, DOUBLE),
                arguments(DOUBLE, INT)
        );
    }


    // endregion
    //-------------------------------------------------------------------------

}
