package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldType;

public class FieldGenerator extends Generator {

    private static final int MAX_LOCALS = 100;
    private static final int MAX_GLOBALS = 100;
    private static final int MAX_METHODS = 100;
    private static final int MAX_METHOD_CALLS = 100;


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
     * @param name      the name of the generated field
     * @param type      the datatype of the generated field
     * @param value     the value to be assigned to the field, null if the field should be initialized
     * @param modifiers array of Modifiers for the new field (from javassist class Modifier).
     *                  For default modifier, set parameter to null.
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateGlobalField(String name, FieldType type, Object value, int[] modifiers) {
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
            } else if (isAssignable(value, type)) {
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
                        break;
                }
            } else {
                System.err.println("Incompatible type and value for primitive global field " + name);
                return false;
            }

            if (modifiers == null) {
                this.getClazzContainer().getClazzLogger().logGlobalField(name, type);
                return true;
            }
            boolean valid = validateModifiers(modifiers);
            if (valid) {
                int merged_modifiers = modifiers[0];
                for (int i = 1; i < modifiers.length; i++) {
                    merged_modifiers |= modifiers[i];
                }
                f.setModifiers(merged_modifiers);
                this.getClazzContainer().getClazzLogger().logGlobalField(name, type, merged_modifiers);
                return true;
            } else {
                System.err.println("Invalid Modifiers for global field " + name);
                return false;
            }
        } catch (CannotCompileException e) {
            System.err.println("Generation of primitive static field " + name + " failed");
            e.printStackTrace();
            return false;
        }
    }

    boolean validateModifiers(int[] modifiers) {
        for (int m : modifiers) {
            switch (m) {
                case Modifier.STATIC:
                    break;
                case Modifier.PUBLIC:
                    break;
                case Modifier.PRIVATE:
                    break;
                case Modifier.PROTECTED:
                    break;
                case Modifier.FINAL:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


    /**
     * @param name  the name of the generated field
     * @param type  the datatype of the generated field
     * @param value the value to be assigned to the field, null if the field should be initialized
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateLocalFieldInMain(String name, FieldType type, Object value) {
        return this.generateLocalField(name, type, value, this.getMain());
    }

    // TODO maybe add boolean for final modifier and add finial in bytecode at Field Initialization

    /**
     * @param name   the name of the generated field
     * @param type   the datatype of the generated field
     * @param value  the value to be assigned to the field, null if the field should be initialized
     * @param method the method in which the local field is generated
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateLocalField(String name, FieldType type, Object value, CtMethod method) {
        try {
            method.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                if (isAssignable(value, type)) {
                    method.insertBefore("y = " + value + ";");
                } else {
                    System.err.println("Value not assignable to Field " + name + " of type " + type.toString());
                    return false;
                }
            }
            this.getClazzContainer().getClazzLogger().logLocalField(name, type, method.getName());
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

}

//TODO track created Fields in clazzfilecontainer
//TODO maybe add final modifier for local Variables
//TODO assignments of variables of compatible types to each other

//    /**
//     * generates a static global field with optional default value
//     *
//     * @param name      the name of the generated field
//     * @param type      the datatype of the generated field
//     * @param value     the value to be assigned to the field, null if the field should be initialized
//     * @param modifiers array of Modifiers for the new field (from javassist class Modifier).
//     *                  For default modifier, set parameter to null.
//     * @return {@code true} if the field was generated successfully, otherwise {@code false}
//     */
//    public boolean generateGlobalField(String name, FieldType type, Object value, int[] modifiers) {
//        try {
//            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
//            if (value == null) {
//                this.getClazzFile().addField(f);
//                return true;
//            } else if (!isAssignable(value, type)) {
//                System.err.println("Incompatible type and value for primitive global field " + name);
//                return false;
//            }
//
//            switch (type.getName()) {
//                case Byte:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((byte) value));
//                    break;
//                case Short:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((short) value));
//                    break;
//                case Int:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((int) value));
//                    break;
//                case Long:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((long) value));
//                    break;
//                case Float:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((float) value));
//                    break;
//                case Double:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((double) value));
//                    break;
//                case Boolean:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((boolean) value));
//                    break;
//                case Char:
//                    this.getClazzFile().addField(f, CtField.Initializer.constant((char) value));
//                    break;
//                case String:
//                    this.getClazzFile().addField(f, "\"" + value + "\"");
//                    break;
//                default:
//                    System.err.println("Generation of primitive static field " + name + " failed\n" +
//                            "Type is not primitive");
//                    return false;
//            }
//
//            if (modifiers == null) return true;
//
//            boolean[] used_modifiers = new boolean[5];
//            boolean access_modifier_used = false;
//            for (int m : modifiers) {
//                switch (m) {
//                    case Modifier.STATIC:
//                        if (used_modifiers[0]) invalidModifier("Static", name);
//                        else {
//                            f.setModifiers(m);
//                            used_modifiers[0] = true;
//                        }
//                        break;
//                    case Modifier.PUBLIC:
//                        if (used_modifiers[1] || access_modifier_used) invalidModifier("Public", name);
//                        else {
//                            f.setModifiers(m);
//                            used_modifiers[1] = true;
//                            access_modifier_used = true;
//                        }
//                        break;
//                    case Modifier.PRIVATE:
//                        if (used_modifiers[2] || access_modifier_used) invalidModifier("Private", name);
//                        else {
//                            f.setModifiers(m);
//                            used_modifiers[2] = true;
//                            access_modifier_used = true;
//                        }
//                        break;
//                    case Modifier.PROTECTED:
//                        if (used_modifiers[3] || access_modifier_used) invalidModifier("Protected", name);
//                        else {
//                            f.setModifiers(m);
//                            used_modifiers[3] = true;
//                            access_modifier_used = true;
//                        }
//                        break;
//                    case Modifier.FINAL:
//                        if (used_modifiers[4]) invalidModifier("Final", name);
//                        else {
//                            f.setModifiers(m);
//                            used_modifiers[4] = true;
//                        }
//                        break;
//                    default:
//                        System.err.println("Invalid Modifier for global field " + name);
//                }
//            }
//            return true;
//        } catch (CannotCompileException e) {
//            System.err.println("Generation of primitive static field " + name + " failed");
//            e.printStackTrace();
//            return false;
//        }
//    }

