package at.jku.ssw.java.bytecode.generator.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.utils.ClassUtils.dimensions;
import static at.jku.ssw.java.bytecode.generator.utils.ClassUtils.nthComponentType;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ClassUtilsTest {

    private static Stream<Arguments> arrayTypesProvider() {
        return Stream.of(
                arguments(Class[][][].class, 3),
                arguments(int[].class, 1),
                arguments(List[][][][][].class, 5),
                arguments(Object[].class, 1),
                arguments(short[][][][].class, 4)
        );
    }

    private static Stream<Arguments> componentTypesProvider() {
        return Stream.of(
                arguments(Class[][][].class, 1, Class[][].class),
                arguments(Class[][][].class, 2, Class[].class),
                arguments(Class[][][].class, 3, Class.class),
                arguments(Class[][][].class, 4, null),
                arguments(Object[][].class, 2, Object.class)
        );
    }

    private static Stream<Class<?>> nonArrayTypesProvider() {
        return Stream.of(
                int.class,
                Object.class,
                List.class,
                Stream.class,
                Class.class,
                Array.class
        );
    }

    @ParameterizedTest(name = "type ''{0}'' has ''{1}'' dimensions")
    @MethodSource("arrayTypesProvider")
    public void testDimensions(Class<?> type, int dim) {
        assertThat(dimensions(type), is(dim));
    }

    @ParameterizedTest(name = "type ''{2}'' is the {1}-th component type of ''{0}''")
    @MethodSource("componentTypesProvider")
    public void testNthComponentType(Class<?> type, int n, Class<?> componentType) {
        assertThat(nthComponentType(n, type), is(Optional.ofNullable(componentType)));
    }

    @ParameterizedTest(name = "type ''{0}'' does not have a component type")
    @MethodSource("nonArrayTypesProvider")
    public void testNthComponentTypeForNonArrayTypes(Class<?> nonArrayType) {
        assertThat(nthComponentType(1, nonArrayType), is(empty()));
    }
}
