package generator;

import javassist.*;
import utils.ClazzFileContainer;

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
    public boolean generateStaticPrimitiveField(String name, FieldType type, Object value) {
        try {
            CtField f = new CtField(type.clazzType, name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
                return true;
            } else if (!isAssignable(value, type)) {
                System.err.println("Incompatible type and value for primitive global field " + name);
                return false;
            }
            switch (type) {
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
                default:
                    System.err.println("Generation of primitive static field " + name + " failed\n" +
                            "Type is not primitive");
                    return false;
            }
            f.setModifiers(Modifier.STATIC);
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of primitive static field " + name + " failed");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * generates a local field in Method main with optional default value
     *
     * @param name the name of the generated field
     * @param type the datatype of the generated field
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateLocalPrimitiveField(String name, FieldType type, Object value) {
        try {
            CtMethod main = this.getClazzFile().getDeclaredMethod("main");
            if (value == null) {
                main.addLocalVariable(name, type.clazzType);
            } else {
                if (isAssignable(value, type)) {
                    main.addLocalVariable(name, type.clazzType);
                    main.insertBefore("y = " + value + ";");
                } else {
                    System.err.println("Value not assignable to Field " + name + " of type " + type.toString());
                    return false;
                }
            }
            return true;
        } catch (CannotCompileException | NotFoundException e) {
            System.err.println("Generation of primitive local field " + name + "  failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean generateStaticStringField(String name, String value) {
        try {
            CtClass string_clazz = ClassPool.getDefault().get("java.lang.String");
            CtField f = new CtField(string_clazz, name, this.getClazzContainer().getClazzFile());
            if (value != null) {
                this.getClazzFile().addField(f, "\"" + value + "\"");
            } else {
                this.getClazzFile().addField(f);
            }
        } catch (NotFoundException | CannotCompileException e) {
            System.err.println("Generation of static string field " + name + "  failed");
            e.printStackTrace();
        }
        return false;
    }


    public void generateGlobalFieldAccess(String fieldName) {
        try {
            CtField f = this.getClazzFile().getDeclaredField(fieldName);
        } catch (NotFoundException e) {
            System.err.println("Cannot access global field " + fieldName);
            e.printStackTrace();
        }
    }

}

//TODO generate local and global vars of all primitive types or java.lang.object
