package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.INSTANCE;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.types.specializations.DateType.DATE;
import static at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType.OBJECT;
import static at.jku.ssw.java.bytecode.generator.types.specializations.StringType.STRING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class TypeCacheTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testTypes() {
        assertThat(
                INSTANCE.types().collect(toList()),
                containsInAnyOrder(
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

    @SuppressWarnings("unchecked")
    @Test
    public void testRefTypes() {
        assertThat(
                INSTANCE.refTypes().collect(toList()),
                containsInAnyOrder(
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
                INSTANCE.primitiveTypes().collect(toList()),
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
        assertThat(INSTANCE.find(int.class), is(Optional.of(INT)));
    }

    @Test
    public void testFindRefType() {
        assertThat(INSTANCE.find(String.class), is(Optional.of(STRING)));
    }

    @Test
    public void testFindObjectType() {
        assertThat(INSTANCE.find(Object.class), is(Optional.of(OBJECT)));
    }

    @Test
    public void testFindArrayType() {
        assertThat(INSTANCE.find(Class[].class), is(Optional.empty()));
    }

    @Test
    public void testFindVoidType() {
        assertThat(INSTANCE.find(Void.class), is(Optional.empty()));
    }
}