package at.jku.ssw.java.bytecode.generator.types;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.BOOLEAN;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.INT;
import static at.jku.ssw.java.bytecode.generator.types.RefType.*;
import static at.jku.ssw.java.bytecode.generator.types.ObjectType.OBJECT;
import static at.jku.ssw.java.bytecode.generator.types.VoidType.VOID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class MetaTypeTest {

    //-------------------------------------------------------------------------
    // region Test cases

    @ParameterizedTest(name = "''{0}'' is not assignable from ''{1}''")
    @MethodSource("incompatibleTypesProvider")
    public void testIsNotAssignableFrom(MetaType<?> that, MetaType<?> other) {
        assertThat(that.isAssignableFrom(other), is(false));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Argument providers

    private static Stream<Arguments> incompatibleTypesProvider() {
        return Stream.of(
                arguments(INT, STRING),
                arguments(VOID, DATE),
                arguments(VOID, VOID),
                arguments(OBJECT, INT),
                arguments(OBJECT, VOID),
                arguments(VOID, OBJECT),
                arguments(VOID, INT),
                arguments(BOOLEAN, VOID)
        );
    }
    // endregion
    //-------------------------------------------------------------------------

}
