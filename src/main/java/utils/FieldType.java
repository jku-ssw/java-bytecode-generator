package utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public final class FieldType {

    public enum FieldTypeName {
        Byte,
        Short,
        Int,
        Long,
        Float,
        Double,
        Boolean,
        Char,
        String;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public final static FieldType byte_ = new FieldType(FieldType.FieldTypeName.Byte);
    public final static FieldType short_ = new FieldType(FieldType.FieldTypeName.Short);
    public final static FieldType int_ = new FieldType(FieldType.FieldTypeName.Int);
    public final static FieldType long_ = new FieldType(FieldType.FieldTypeName.Long);
    public final static FieldType Float_ = new FieldType(FieldType.FieldTypeName.Float);
    public final static FieldType double_ = new FieldType(FieldType.FieldTypeName.Double);
    public final static FieldType boolean_ = new FieldType(FieldType.FieldTypeName.Boolean);
    public final static FieldType char_ = new FieldType(FieldType.FieldTypeName.Char);
    public final static FieldType string_ = new FieldType(FieldType.FieldTypeName.String);


    public FieldTypeName getName() {
        return name;
    }

    private final FieldTypeName name;

    private final CtClass clazzType;

    private FieldType(FieldTypeName name) {
        this.name = name;
        CtClass c = null;
        switch (name) {
            case Byte:
                c = CtClass.byteType;
                break;
            case Short:
                c = CtClass.shortType;
                break;
            case Int:
                c = CtClass.intType;
                break;
            case Long:
                c = CtClass.longType;
                break;
            case Float:
                c = CtClass.floatType;
                break;
            case Double:
                c = CtClass.doubleType;
                break;
            case Boolean:
                c = CtClass.booleanType;
                break;
            case Char:
                c = CtClass.charType;
                break;
            case String:
                try {
                    c = ClassPool.getDefault().get("java.lang.String");
                } catch (NotFoundException e) {
                    System.err.println("Cannot create FieldType of class String");
                    e.printStackTrace();
                }
        }
        this.clazzType = c;
    }

    public CtClass getClazzType() {
        return clazzType;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}