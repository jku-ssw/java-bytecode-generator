package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class FieldVarTypeTest {

    private static Stream<Arguments> compatibleTypesProvider() {
        return Stream.of(
                arguments(BYTE, BYTE, true),
                arguments(SHORT, BYTE, true),
                arguments(SHORT, SHORT, true),
                arguments(INT, BYTE, true),
                arguments(INT, SHORT, true),
                arguments(INT, INT, true),
                arguments(LONG, INT, true),
                arguments(DOUBLE, FLOAT, true),
                arguments(STRING, STRING, true),
                arguments(DATE, DATE, true)
        );
    }

    private static Stream<Arguments> incompatibleTypesProvider() {
        return Stream.of(
                arguments(BYTE, SHORT, false),
                arguments(SHORT, INT, false),
                arguments(INT, STRING, false),
                arguments(FLOAT, DOUBLE, false),
                arguments(DOUBLE, INT, false),
                arguments(STRING, DATE, false),
                arguments(VOID, DATE, false),
                arguments(VOID, VOID, false)
        );
    }

    private static Stream<Arguments> arrayTypeProvider() {
        return Stream.of(
                arguments(classType(String.class), 1, arrayType(String[].class, STRING)),
                arguments(classType(Date.class), 3, arrayType(Date[][][].class, DATE)),
                arguments(type(byte.class, Kind.BYTE), 19, arrayType(byte[][][][][][][][][][][][][][][][][][][].class, BYTE)),
                arguments(type(short.class, Kind.SHORT), 2, arrayType(short[][].class, SHORT)),
                arguments(type(int.class, Kind.INT), 10, arrayType(int[][][][][][][][][][].class, INT)),
                arguments(type(long.class, Kind.LONG), 5, arrayType(long[][][][][].class, LONG)),
                arguments(type(float.class, Kind.FLOAT), 2, arrayType(float[][].class, FLOAT)),
                arguments(type(double.class, Kind.DOUBLE), 4, arrayType(double[][][][].class, DOUBLE)),
                arguments(type(char.class, Kind.CHAR), 7, arrayType(char[][][][][][][].class, CHAR)),
                arguments(type(boolean.class, Kind.BOOLEAN), 1, arrayType(boolean[].class, BOOLEAN))
        );
    }

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
        assertThat(arrayTypeOf(componentType, dim), is(arrayType));
    }

    private static <T> FieldVarType<T> type(Class<T> type, Kind kind) {
        return new FieldVarType<>(type, kind);
    }

    private static <T> FieldVarType<T> classType(Class<T> type) {
        return new FieldVarType<>(type, Kind.INSTANCE);
    }

    private static <T> FieldVarType<T> arrayType(Class<T> type, FieldVarType<?> componentType) {
        assert type.isArray();

        return new FieldVarType<>(
                type,
                ClassUtils.dimensions(type),
                componentType
        );
    }

}
