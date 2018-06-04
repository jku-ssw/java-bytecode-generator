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
    Byte("byte"),
    Short("short"),
    Int("int"),
    Long("long"),
    Float("float"),
    Double("double"),
    Boolean("boolean"),
    Char("char"),
    String("String"),
    Void("void");

    private static final List<FieldVarType> compWithShort =
            Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char);
    private static final List<FieldVarType> compWithInt =
            Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char, FieldVarType.Int);
    private static final List<FieldVarType> compWithLong =
            Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char, FieldVarType.Int, FieldVarType.Long);
    private static final List<FieldVarType> compWithDouble =
            Arrays.asList(FieldVarType.Float, FieldVarType.Double);

    private CtClass clazzType;
    private final String name;

    FieldVarType(String name) {
        this.name = name;
        switch (name) {
            case "byte":
                this.clazzType = CtClass.byteType;
                break;
            case "short":
                this.clazzType = CtClass.shortType;
                break;
            case "int":
                this.clazzType = CtClass.intType;
                break;
            case "long":
                this.clazzType = CtClass.longType;
                break;
            case "float":
                this.clazzType = CtClass.floatType;
                break;
            case "double":
                this.clazzType = CtClass.doubleType;
                break;
            case "boolean":
                this.clazzType = CtClass.booleanType;
                break;
            case "char":
                this.clazzType = CtClass.charType;
                break;
            case "String":
                try {
                    this.clazzType = ClassPool.getDefault().get("java.lang.String");
                } catch (NotFoundException e) {
                    throw new AssertionError(e);
                }
                break;
            case "void":
                this.clazzType = CtClass.voidType;
        }
    }

    public CtClass getClazzType() {
        return this.clazzType;
    }

    public String getName() {
        return this.name;
    }

    public static List<FieldVarType>
    getCompatibleTypes(FieldVarType type) {
        switch (type) {
            case Byte:
                return Arrays.asList(FieldVarType.Byte);
            case Short:
                return compWithShort;
            case Int:
                return compWithInt;
            case Long:
                return compWithLong;
            case Float:
                return Arrays.asList(FieldVarType.Float);
            case Double:
                return compWithDouble;
            case Boolean:
                return Arrays.asList(FieldVarType.Boolean);
            case Char:
                return Arrays.asList(FieldVarType.Char);
            case String:
                return Arrays.asList(FieldVarType.String);
            default:
                return Arrays.asList(type);
        }
    }

    @Override
    public java.lang.String toString() {
        return getName();
    }

}