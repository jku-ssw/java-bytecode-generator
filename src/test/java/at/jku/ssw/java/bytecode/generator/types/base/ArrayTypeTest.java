package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.types.specializations.DynamicRefType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.ArrayType.resultingTypeOf;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.types.specializations.DateType.DATE;
import static at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType.OBJECT;
import static at.jku.ssw.java.bytecode.generator.types.specializations.StringType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ArrayTypeTest {

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


    @ParameterizedTest(name = "array type of ''{0}'' with {1} dimension(s) is ''{2}''")
    @MethodSource("arrayTypeProvider")
    public void testCreateArrayType(MetaType<?> componentType, int dim, MetaType<?> arrayType) {
        assertThat(ArrayType.of(componentType, dim), is(arrayType));
    }

    @ParameterizedTest(name = "resulting type of ''{0}'' being accessed at dimension ''{2}'' is ''{3}''")
    @MethodSource("arrayTypeAndAccessProvider")
    public void testResultingTypeOf(Class<?> arrayType, MetaType<?> inner, int nParams, MetaType<?> expected) {
        assertThat(
                resultingTypeOf(arrayOf(arrayType, inner), nParams),
                is(expected)
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Test utilities

    private static FieldVarLogger<?> arrayOf(Class<?> type, MetaType<?> componentType) {
        return new FieldVarLogger<>("", "", 0, arrayType(type, componentType), false, false);
    }

    private static <T> RefType<T> refType(Class<T> type) {
        return new DynamicRefType<>(type);
    }

    private static <T> MetaType<T> arrayType(Class<T> type, MetaType<?> componentType) {
        assert type.isArray();

        return ArrayType.of(type, componentType);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Argument providers

    private static Stream<Arguments> compatibleTypesProvider() {
        return Stream.of(
                arguments(arrayType(Object[].class, OBJECT), arrayType(Object[].class, OBJECT)),
                arguments(OBJECT, arrayType(Object[].class, OBJECT)),
                arguments(arrayType(int[][][].class, INT), arrayType(int[][][].class, INT))
        );
    }

    private static Stream<Arguments> incompatibleTypesProvider() {
        return Stream.of(
                arguments(BYTE, arrayType(short[][].class, SHORT)),
                arguments(arrayType(int[].class, INT), STRING),
                arguments(arrayType(String[].class, STRING), STRING)
        );
    }

    private static Stream<Arguments> arrayTypeProvider() {
        return Stream.of(
                arguments(refType(String.class), 1, arrayType(String[].class, STRING)),
                arguments(refType(Date.class), 3, arrayType(Date[][][].class, DATE)),
                arguments(BYTE, 19, arrayType(byte[][][][][][][][][][][][][][][][][][][].class, BYTE)),
                arguments(SHORT, 2, arrayType(short[][].class, SHORT)),
                arguments(INT, 10, arrayType(int[][][][][][][][][][].class, INT)),
                arguments(LONG, 5, arrayType(long[][][][][].class, LONG)),
                arguments(FLOAT, 2, arrayType(float[][].class, FLOAT)),
                arguments(DOUBLE, 4, arrayType(double[][][][].class, DOUBLE)),
                arguments(CHAR, 7, arrayType(char[][][][][][][].class, CHAR)),
                arguments(BOOLEAN, 1, arrayType(boolean[].class, BOOLEAN)),
                arguments(OBJECT, 7, arrayType(Object[][][][][][][].class, OBJECT))
        );
    }

    private static Stream<Arguments> arrayTypeAndAccessProvider() {
        return Stream.of(
                arguments(Object[][][][].class, OBJECT, 1, ArrayType.of(Object[][][].class, OBJECT)),
                arguments(int[].class, INT, 1, INT),
                arguments(boolean[][][].class, INT, 2, ArrayType.of(boolean[].class, BOOLEAN))
        );
    }

    // endregion
    //-------------------------------------------------------------------------

}
