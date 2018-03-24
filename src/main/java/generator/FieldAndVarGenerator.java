package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldType;

public class FieldAndVarGenerator extends Generator {

    public FieldAndVarGenerator(String filename) {
        super(filename);
    }

    public FieldAndVarGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public FieldAndVarGenerator() {
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
    public boolean generateField(String name, FieldType type, Object value, int[] modifiers) {
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

    private static boolean validateModifiers(int[] modifiers) {
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
    public boolean generateVariableInMain(String name, FieldType type, Object value) {
        return this.generateVariable(name, type, value, this.getMain());
    }

    // TODO maybe add boolean for final modifier and add final in bytecode at Field Initialization
    /**
     * @param name   the name of the generated field
     * @param type   the datatype of the generated field
     * @param value  the value to be assigned to the field, null if the field should be initialized
     * @param method the method in which the variable is generated
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateVariable(String name, FieldType type, Object value, CtMethod method) {
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
            this.getClazzContainer().getClazzLogger().logLocalVariable(name, type, method.getName());
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of primitive local field " + name + "  failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean generatePrintFieldStatement(String fieldName) {
        try {
            if (this.getClazzLogger().hasField(fieldName)) {
                //System.out.println("\"" + fieldName +)
                this.getMain().insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName +");");
                return true;
            } else {
                System.err.println("Class does not contain a Field " + fieldName);
                return false;
            }
        } catch (CannotCompileException e) {
            System.err.println("Generation of PrintField-Statement failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean generatePrintVariableStatement(String fieldName, String methodName) {
        try {
            if (this.getClazzLogger().hasField(fieldName, methodName)) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                m.insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName +");");
                return true;
            } else {
                System.err.println("Class does not contain a Field " + fieldName);
                return false;
            }
        } catch (CannotCompileException | NotFoundException e) {
            System.err.println("Generation of PrintField-Statement failed");
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



