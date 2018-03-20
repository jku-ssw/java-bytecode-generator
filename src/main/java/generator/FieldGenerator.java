package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldType;

public class FieldGenerator extends Generator {


    public FieldGenerator(String filename) {
        super(filename);
    }

    public FieldGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public FieldGenerator() {
        super();
    }

    /**
     * generates a static global field with optional default value
     *
     * @param name the name of the generated field
     * @param type the datatype of the generated field
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateGlobalField(String name, FieldType type, Object value, int[] modifier) {
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
                return true;
            } else if (!isAssignable(value, type)) {
                System.err.println("Incompatible type and value for primitive global field " + name);
                return false;
            }

            switch (type.getName()) {
                case Byte:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((byte) value));
                    break;
                case Short:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((short) value));
                    break;
                case Int:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((int) value));
                    break;
                case Long:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((long) value));
                    break;
                case Float:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((float) value));
                    break;
                case Double:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((double) value));
                    break;
                case Boolean:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((boolean) value));
                    break;
                case Char:
                    this.getClazzFile().addField(f, CtField.Initializer.constant((char) value));
                    break;
                case String:
                    this.getClazzFile().addField(f, "\"" + value + "\"");
                default:
                    System.err.println("Generation of primitive static field " + name + " failed\n" +
                            "Type is not primitive");
                    return false;
            }

            for (int m : modifier) {
                if (Modifier.isEnum(m))
                    f.setModifiers(m);
            }
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of primitive static field " + name + " failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean generateLocalFieldInMain((String name, FieldType type, Object value) {
        return this.generateLocalField(name, type, value, this.getMain());
    }

    //TODO add boolean for final modifier and add finial in bytecode at Field Initialization
    public boolean generateLocalField(String name, FieldType type, Object value, CtMethod meth) {
        try {
            meth.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                if (isAssignable(value, type)) {
                    meth.insertBefore("y = " + value + ";");
                } else {
                    System.err.println("Value not assignable to Field " + name + " of type " + type.toString());
                    return false;
                }
            }
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of primitive local field " + name + "  failed");
            e.printStackTrace();
            return false;
        }
    }

    //TODO
    public boolean setFieldValue(String fieldName) {
        try {
            CtField f = this.getClazzFile().getDeclaredField(fieldName);
        } catch (NotFoundException e) {
            System.err.println("Cannot access global field " + fieldName);
            e.printStackTrace();
        }
        return false;
    }

    //TODO
    public boolean assignFieldToField(String fieldName1, String fieldName2) {
        return false;
    }

    //TODO Remove
//    public boolean generateStaticStringField(String name, String value) {
//        try {
//            CtClass string_clazz = ClassPool.getDefault().get("java.lang.String");
//            CtField f = new CtField(string_clazz, name, this.getClazzContainer().getClazzFile());
//            if (value != null) {
//                this.getClazzFile().addField(f, "\"" + value + "\"");
//            } else {
//                this.getClazzFile().addField(f);
//            }
//        } catch (NotFoundException | CannotCompileException e) {
//            System.err.println("Generation of static string field " + name + "  failed");
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    /**
//     * generates a local field in Method main with optional default value
//     *
//     * @param name the name of the generated field
//     * @param type the datatype of the generated field
//     * @return {@code true} if the field was generated successfully, otherwise {@code false}
//     */

//    public boolean generateLocalField(String name, FieldType type, Object value, CtMethod meth, boolean isFinal) {
//        try {
//            StringBuilder b = new StringBuilder();
//            if (isFinal) b.append("final ");
//            switch (type.getName()) {
//                case Byte:
//                    b.append("byte " + name);
//                case Short:
//                    b.append("short " + name);
//                case Int:
//                    b.append("int" + name);
//                case Long:
//                    b.append("long " + name);
//                case Float:
//                    b.append("float" + name);
//                case Double:
//                    b.append("double " + name);
//                case Boolean:
//                    b.append("boolean" + name);
//                case Char:
//                    b.append("char " + name);
//                case String:
//                    b.append("String " + name);
//                default:
//                    return false;
//            }
//            if(value != null && isAssignable(value, type)) b.append(" = " + value + ";");
//            meth.insertBefore(b.toString());
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public boolean generateLocalFieldInMain(String name, FieldType type, Object value, boolean isFinal) {
//        return this.generateLocalField(name, type, value, this.getMain(), isFinal);
//    }

}

//TODO track created Fields in clazzfilecontainer
//TODO maybe add final modifier for local Variables
//TODO assignments of variables of compatible types to each other
