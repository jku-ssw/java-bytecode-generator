package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Enum-Class of all possible Variable- and FieldTypes for ByteCodeGenerator
 */
public enum FieldType {
    Byte("byte"),
    Short("short"),
    Int("int"),
    Long("long"),
    Float("float"),
    Double("double"),
    Boolean("boolean"),
    Char("char"),
    String("String");

    private CtClass clazzType;
    private final String name;

    FieldType(java.lang.String name) {
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
                    System.err.println("Cannot create FieldType of class String");
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * @return returns the CtType of this FieldType
     */
    public CtClass getClazzType() {
        return this.clazzType;
    }

    /**
     * @return returns the name of this FieldType
     */
    public String getName() {
        return this.name;
    }

}