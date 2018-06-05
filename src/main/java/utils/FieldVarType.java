package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.List;

/**
 * Enum-Class of all possible Variable- and FieldTypes in the jb_generator.jb_generator
 */
public enum FieldVarType {
    BYTE(CtClass.byteType),
    SHORT(CtClass.shortType),
    INT(CtClass.intType),
    LONG(CtClass.longType),
    FLOAT(CtClass.floatType),
    DOUBLE(CtClass.doubleType),
    BOOLEAN(CtClass.booleanType),
    CHAR(CtClass.charType),
    STRING(getStringCtType()),
    VOID(CtClass.voidType);

    private static final List<FieldVarType> COMP_WITH_SHORT =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR);
    private static final List<FieldVarType> COMP_WITH_INT =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR, FieldVarType.INT);
    private static final List<FieldVarType> COMP_WITH_LONG =
            Arrays.asList(FieldVarType.BYTE, FieldVarType.SHORT, FieldVarType.CHAR, FieldVarType.INT, FieldVarType.LONG);
    private static final List<FieldVarType> COMP_WITH_DOUBLE =
            Arrays.asList(FieldVarType.FLOAT, FieldVarType.DOUBLE);

    private CtClass clazzType;

    private static CtClass getStringCtType() {
        try {
            return ClassPool.getDefault().get("java.lang.String");
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

    public String getName() {
        if(this == STRING) {
            return "String";
        } else {
            return this.toString().toLowerCase();
        }
    }

    public static List<FieldVarType> getCompatibleTypes(FieldVarType type) {
        switch (type) {
            case BYTE:
                return Arrays.asList(FieldVarType.BYTE);
            case SHORT:
                return COMP_WITH_SHORT;
            case INT:
                return COMP_WITH_INT;
            case LONG:
                return COMP_WITH_LONG;
            case FLOAT:
                return Arrays.asList(FieldVarType.FLOAT);
            case DOUBLE:
                return COMP_WITH_DOUBLE;
            case BOOLEAN:
                return Arrays.asList(FieldVarType.BOOLEAN);
            case CHAR:
                return Arrays.asList(FieldVarType.CHAR);
            case STRING:
                return Arrays.asList(FieldVarType.STRING);
            default:
                return Arrays.asList(type);
        }
    }
}