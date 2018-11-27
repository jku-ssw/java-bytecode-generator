package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.types.base.*;
import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.types.specializations.DateType.DATE;
import static at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType.OBJECT;
import static at.jku.ssw.java.bytecode.generator.types.specializations.StringType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TypeCacheTest {

    @BeforeEach
    void setUp() {
        CACHE.reset();
    }

    @Test
    public void testContainsTypes() {
        assertThat(
                CACHE.types().collect(Collectors.toList()),
                Matchers.<MetaType>containsInAnyOrder(
                        BYTE,
                        SHORT,
                        INT,
                        LONG,
                        FLOAT,
                        DOUBLE,
                        CHAR,
                        BOOLEAN,
                        STRING,
                        DATE,
                        OBJECT,
                        BoxedType.BYTE,
                        BoxedType.SHORT,
                        BoxedType.INT,
                        BoxedType.LONG,
                        BoxedType.FLOAT,
                        BoxedType.DOUBLE,
                        BoxedType.BOOLEAN,
                        BoxedType.CHAR
                )
        );
    }

    @Test
    public void testContainsRefTypes() {
        assertThat(
                CACHE.refTypes,
                Matchers.<RefType>containsInAnyOrder(
                        OBJECT,
                        DATE,
                        STRING,
                        BoxedType.BYTE,
                        BoxedType.SHORT,
                        BoxedType.INT,
                        BoxedType.LONG,
                        BoxedType.FLOAT,
                        BoxedType.DOUBLE,
                        BoxedType.BOOLEAN,
                        BoxedType.CHAR
                )
        );
    }

    @Test
    public void testContainsPrimitiveTypes() {
        assertThat(
                CACHE.primitiveTypes,
                containsInAnyOrder(
                        BYTE,
                        SHORT,
                        INT,
                        LONG,
                        FLOAT,
                        DOUBLE,
                        CHAR,
                        BOOLEAN
                )
        );
    }

    @Test
    public void testContainsPrimitiveType() {
        assertTrue(CACHE.contains(INT));
    }

    @Test
    public void testContainsRefType() {
        assertTrue(CACHE.contains(STRING));
    }

    @Test
    public void testContainsObjectType() {
        assertTrue(CACHE.contains(OBJECT));
    }

    @Test
    public void testContainsArrayType() {
        ArrayType<Object[]> objectArray1dType = ArrayType.of(Object[].class, OBJECT);
        assertFalse(CACHE.contains(objectArray1dType));
    }

    @ParameterizedTest(name = "Meta type ''{1}'' is inferred from ''{0}''")
    @MethodSource("inferredTypeProvider")
    public void testFind(Class<?> type, MetaType<?> expected) {
        assertThat(CACHE.find(type), is(Optional.of(expected)));
    }

    private static Stream<Arguments> inferredTypeProvider() {
        return Stream.of(
                arguments(String.class, STRING),
                arguments(int.class, PrimitiveType.INT),
                arguments(Integer.class, BoxedType.INT),
                arguments(Object.class, ObjectType.OBJECT),
                arguments(Date.class, DATE),
                arguments(Void.TYPE, VoidType.VOID),
                arguments(Character[][].class, ArrayType.of(BoxedType.CHAR, 2)),
                arguments(String[].class, ArrayType.of(STRING, 1)),
                arguments(boolean[][][][][].class, ArrayType.of(PrimitiveType.BOOLEAN, 5)),
                arguments(Date[][].class, ArrayType.of(DATE, 2))
        );
    }

}
