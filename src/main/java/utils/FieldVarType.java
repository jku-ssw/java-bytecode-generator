package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum FieldVarType {
    BYTE(CtClass.byteType),
    SHORT(CtClass.shortType),
    INT(CtClass.intType),
    LONG(CtClass.longType),
    FLOAT(CtClass.floatType),
    DOUBLE(CtClass.doubleType),
    BOOLEAN(CtClass.booleanType),
    CHAR(CtClass.charType),
    STRING(getCtClassString()),
    DATE(getCtClassType("java.util.Date")),
    VOID(CtClass.voidType);

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


    private CtClass clazzType;

    private static CtClass getCtClassString() {
        try {
            return ClassPool.getDefault().get("java.lang.String");
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static CtClass getCtClassType(String classname) {
        try {
            return ClassPool.getDefault().get(classname);
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    FieldVarType(CtClass clazzType) {
        this.clazzType = clazzType;
    }

    public CtClass getClazzType() {
        return this.clazzType;
    }

    @Override
    public String toString() {
        switch(this) {
            case STRING:
                return "String";
            case DATE:
                return "java.util.Date";
            default:
                return super.toString().toLowerCase();
        }
    }

    public static List<FieldVarType> getNumericTypes() {
        return NUMERIC_TYPES;
    }

    public static List<FieldVarType> getCompatibleTypes(FieldVarType type) {
        switch (type) {
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
            case STRING:
                return Collections.singletonList(FieldVarType.STRING);
            case DATE:
                return Collections.singletonList(FieldVarType.DATE);
            default:
                return Collections.singletonList(type);
        }
    }
}
