package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.base.VoidType;
import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.logger.MethodLogger.infer;
import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.types.specializations.StringType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class MethodLoggerTest {

    @BeforeEach
    void setUp() {
        CACHE.reset();
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @MethodSource("inferredMethodProvider")
    public void testInfer(Method method, MethodLogger<?> inferred) {
        assertThat(infer(method), is(inferred));
    }


    private static Stream<Arguments> inferredMethodProvider() throws NoSuchMethodException {
        return Stream.of(
                arguments(
                        method(String.class, "valueOf", Integer.TYPE),
                        new MethodLogger<>(STRING, "valueOf", Modifier.PUBLIC | Modifier.STATIC, STRING, INT)
                ),
                arguments(
                        method(Integer.class, "parseInt", String.class, Integer.TYPE),
                        new MethodLogger<>(BoxedType.INT, "parseInt", Modifier.PUBLIC | Modifier.STATIC, INT, STRING, INT)
                ),
                arguments(
                        method(Integer.class, "byteValue"),
                        new MethodLogger<>(BoxedType.INT, "byteValue", Modifier.PUBLIC, BYTE)
                ),
                arguments(
                        method(Date.class, "wait", Long.TYPE, Integer.TYPE),
                        new MethodLogger<>(ObjectType.OBJECT, "wait", Modifier.PUBLIC | Modifier.FINAL, VoidType.VOID, LONG, INT)
                )
        );
    }

    private static Method method(Class<?> clazz, String method, Class<?>... args) throws NoSuchMethodException {
        return clazz.getMethod(method, args);
    }
}
