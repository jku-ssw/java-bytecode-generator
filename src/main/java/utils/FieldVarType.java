package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.ArrayList;
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

    /**
     * constructs a new FieldVarType with given name and ClazzType
     *
     * @param name the name of this FieldVarType
     */
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
                    System.err.println("Cannot create FieldVarType of class String");
                    e.printStackTrace();
                }
                break;
            case "void":
                this.clazzType = CtClass.voidType;
        }
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

    /**
     * @param type
     * @return a random FieldVarType that is compatible to type
     */
    public static List<FieldVarType> getCompatibleTypes(FieldVarType type) {
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


    //TODO restliche types not only for Math => ev. wieder in Math Generator geben
    public static FieldVarType getType(char t) {
        switch (t) {
            case 'D':
                return FieldVarType.Double;
            case 'I':
                return FieldVarType.Int;
            case 'F':
                return FieldVarType.Float;
            case 'J':
                return FieldVarType.Long;
            default:
                return null;
        }
    }

    public static FieldVarType[] getParamTypes(String methodSignature) {
        List<FieldVarType> paramTypes = new ArrayList<>();
        for (int i = 1; i < methodSignature.length() - 2; i++) {
            paramTypes.add(FieldVarType.getType(methodSignature.charAt(i)));
        }
        FieldVarType[] paramTypesArray = new FieldVarType[paramTypes.size()];
        return paramTypes.toArray(paramTypesArray);
    }
}