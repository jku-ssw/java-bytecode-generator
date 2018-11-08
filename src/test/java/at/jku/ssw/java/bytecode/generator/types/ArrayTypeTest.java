package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.ArrayType.resultingTypeOf;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.BOOLEAN;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.BYTE;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.CHAR;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.DOUBLE;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.FLOAT;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.INT;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.LONG;
import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.SHORT;
import static at.jku.ssw.java.bytecode.generator.types.RefType.*;
import static at.jku.ssw.java.bytecode.generator.types.RootType.OBJECT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ArrayTypeTest {

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


    @ParameterizedTest(name = "array type of ''{0}'' with {1} dimension(s) is ''{2}''")
    @MethodSource("arrayTypeProvider")
    public void testCreateArrayType(FieldVarType<?> componentType, int dim, FieldVarType<?> arrayType) {
        assertThat(ArrayType.of(componentType, dim), is(arrayType));
    }

    @ParameterizedTest(name = "resulting type of ''{0}'' being accessed at dimension ''{2}'' is ''{3}''")
    @MethodSource("arrayTypeAndAccessProvider")
    public void testResultingTypeOf(Class<?> arrayType, FieldVarType<?> inner, int nParams, FieldVarType<?> expected) {
        assertThat(
                resultingTypeOf(arrayOf(arrayType, inner), nParams),
                is(expected)
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Test utilities

    private static FieldVarLogger arrayOf(Class<?> type, FieldVarType<?> componentType) {
        return new FieldVarLogger("", "", 0, arrayType(type, componentType), false, false);
    }

    private static <T> RefType<T> refType(Class<T> type) {
        return RefType.of(type);
    }

    private static <T> FieldVarType<T> arrayType(Class<T> type, FieldVarType<?> componentType) {
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
