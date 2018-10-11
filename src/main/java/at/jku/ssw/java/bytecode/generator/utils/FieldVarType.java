package at.jku.ssw.java.bytecode.generator.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FieldVarType<T> {
    public enum Kind {
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        CHAR,
        INSTANCE,
        VOID
    }

    public static final FieldVarType BYTE = new FieldVarType<>(byte.class, CtClass.byteType, Kind.BYTE);
    public static final FieldVarType SHORT = new FieldVarType<>(short.class, CtClass.shortType, Kind.SHORT);
    public static final FieldVarType INT = new FieldVarType<>(int.class, CtClass.intType, Kind.INT);
    public static final FieldVarType LONG = new FieldVarType<>(long.class, CtClass.longType, Kind.LONG);
    public static final FieldVarType FLOAT = new FieldVarType<>(float.class, CtClass.floatType, Kind.FLOAT);
    public static final FieldVarType DOUBLE = new FieldVarType<>(double.class, CtClass.doubleType, Kind.DOUBLE);
    public static final FieldVarType BOOLEAN = new FieldVarType<>(boolean.class, CtClass.booleanType, Kind.BOOLEAN);
    public static final FieldVarType CHAR = new FieldVarType<>(char.class, CtClass.charType, Kind.CHAR);
    public static final FieldVarType STRING = new FieldVarType<>(String.class, Kind.INSTANCE);
    public static final FieldVarType DATE = new FieldVarType<>(Date.class, Kind.INSTANCE);
    public static final FieldVarType VOID = new FieldVarType<>(Void.class, CtClass.voidType, Kind.VOID);

    public static final FieldVarType[] values = {
            BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR, STRING, DATE, VOID
    };

    private static final List<FieldVarType> NUMERIC_TYPES =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.CHAR,
                    FieldVarType.DOUBLE, FieldVarType.FLOAT, FieldVarType.INT, FieldVarType.LONG, FieldVarType.SHORT);
    private static final List<FieldVarType> COMP_WITH_SHORT =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR);
    private static final List<FieldVarType> COMP_WITH_INT =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR, FieldVarType.INT);
    private static final List<FieldVarType> COMP_WITH_LONG =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR, FieldVarType.INT, FieldVarType.LONG);
    private static final List<FieldVarType> COMP_WITH_DOUBLE =
            Arrays.asList(FieldVarType.FLOAT, FieldVarType.DOUBLE);

    public final Kind kind;
    public final Class<T> clazz;
    private final CtClass clazzType;
    public final int dim;

    private static CtClass getCtClassType(Class<?> clazz) {
        return getCtClassType(clazz.getCanonicalName());
    }

    private static CtClass getCtClassType(String classname) {
        try {
            return ClassPool.getDefault().get(classname);
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private FieldVarType(Class<T> clazz, Kind kind) {
        this(clazz, getCtClassType(clazz), kind);
    }

    private FieldVarType(Class<T> clazz, CtClass clazzType, Kind kind) {
        this(clazz, clazzType, kind, 0);
    }

    private FieldVarType(Class<T> clazz, CtClass clazzType, Kind kind, int dim) {
        this.kind = kind;
        this.clazz = clazz;
        this.clazzType = clazzType;
        this.dim = dim;
    }

    public CtClass getClazzType() {
        return clazzType;
    }

    @Override
    public String toString() {
        if (kind == Kind.VOID)
            return "void";
        return clazz.getCanonicalName();
    }

    public static List<FieldVarType> getNumericTypes() {
        return NUMERIC_TYPES;
    }

    public static List<FieldVarType> getCompatibleTypes(FieldVarType type) {
        switch (type.kind) {
            case BYTE:
                return Collections.singletonList(FieldVarType.BYTE);
            case SHORT:
                return COMP_WITH_SHORT;
            case INT:
                return COMP_WITH_INT;
            case LONG:
                return COMP_WITH_LONG;
            case FLOAT:
                return Collections.singletonList(FieldVarType.FLOAT);
            case DOUBLE:
                return COMP_WITH_DOUBLE;
            case BOOLEAN:
                return Collections.singletonList(FieldVarType.BOOLEAN);
            case CHAR:
                return Collections.singletonList(FieldVarType.CHAR);
            default:
                return Collections.singletonList(type);
        }
    }
}
