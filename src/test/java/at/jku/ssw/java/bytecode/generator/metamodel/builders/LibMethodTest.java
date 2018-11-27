package at.jku.ssw.java.bytecode.generator.metamodel.builders;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.metamodel.builders.LibMethod.infer;
import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LibMethodTest {

    @BeforeEach
    void setUp() {
        CACHE.reset();
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @MethodSource("inferredMethodProvider")
    public void testInfer(Method method, String inferred) {
        assertThat(
                infer(method).map(LibMethod::toString),
                is(Optional.ofNullable(inferred))
        );
    }

    // public protected private abstract static final transient volatile synchronized native strictfp interface

    private static Stream<Arguments> inferredMethodProvider() throws NoSuchMethodException {
        return Stream.of(
                arguments(
                        method(String.class, "valueOf", Integer.TYPE),
                        "method public static java.lang.String java.lang.String.valueOf(int)"
                ),
                arguments(
                        method(Integer.class, "parseInt", String.class, Integer.TYPE),
                        "method public static int java.lang.Integer.parseInt(java.lang.String, int)"
                ),
                arguments(
                        method(Integer.class, "byteValue"),
                        "method public byte java.lang.Integer.byteValue()"
                ),
                arguments(
                        method(Date.class, "wait", Long.TYPE, Integer.TYPE),
                        null
                ),
                arguments(
                        method(Object.class, "wait", Long.TYPE, Integer.TYPE),
                        null
                )
        );
    }

    private static Method method(Class<?> clazz, String method, Class<?>... args) throws NoSuchMethodException {
        return clazz.getMethod(method, args);
    }
}
