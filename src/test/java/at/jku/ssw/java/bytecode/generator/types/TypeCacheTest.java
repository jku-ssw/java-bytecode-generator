package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.types.specializations.DateType.DATE;
import static at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType.OBJECT;
import static at.jku.ssw.java.bytecode.generator.types.specializations.StringType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeCacheTest {

    @BeforeEach
    void setUp() {
        CACHE.reset();
    }

    @Test
    public void testTypes() {
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
    public void testRefTypes() {
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
    public void testPrimitiveTypes() {
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
    public void testFindPrimitiveType() {
        assertTrue(CACHE.contains(INT));
    }

    @Test
    public void testFindRefType() {
        assertTrue(CACHE.contains(STRING));
    }

    @Test
    public void testFindObjectType() {
        assertTrue(CACHE.contains(OBJECT));
    }

    @Test
    public void testFindArrayType() {
        ArrayType<Object[]> objectArray1dType = ArrayType.of(Object[].class, OBJECT);
        assertFalse(CACHE.contains(objectArray1dType));
    }
}
