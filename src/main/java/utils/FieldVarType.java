package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.List;

/**
 * Enum-Class of all possible Variable- and FieldTypes in the ByteCodeGenerator
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

    private static final List<FieldVarType> compWithShort = Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char);
    private static final List<FieldVarType> compWithInt = Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char, FieldVarType.Int);
    private static final List<FieldVarType> compWithLong = Arrays.asList(FieldVarType.Byte, FieldVarType.Short, FieldVarType.Char, FieldVarType.Int, FieldVarType.Long);
    private static final List<FieldVarType> compWithDouble = Arrays.asList(FieldVarType.Float, FieldVarType.Double);

    private CtClass clazzType;
    private final String name;

    FieldVarType(java.lang.String name) {
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
                    System.err.println("Cannot create FieldVarType of class String");
                    e.printStackTrace();
                }
                break;
            case "void":
                this.clazzType = CtClass.voidType;
        }
    }

    public static List<FieldVarType> getCompWithShort() {
        return compWithShort;
    }

    public static List<FieldVarType> getCompWithInt() {
        return compWithInt;
    }


    public static List<FieldVarType> getCompWithLong() {
        return compWithLong;
    }

    public static List<FieldVarType> getCompWithDouble() {
        return compWithDouble;
    }

    /**
     * @return returns the CtClass of this FieldVarType
     */
    public CtClass getClazzType() {
        return this.clazzType;
    }

    /**
     * @return returns the name of this FieldVarType
     */
    public String getName() {
        return this.name;
    }

}