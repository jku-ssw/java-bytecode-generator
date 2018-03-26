package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.Field;
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


    // TODO maybe add boolean for final modifier and add final in bytecode at Field Initialization

    /**
     * @param name       the name of the generated field
     * @param type       the datatype of the generated field
     * @param value      the value to be assigned to the field, null if the field should be initialized
     * @param methodName the method in which the variable is generated
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateVariable(String name, FieldType type, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        try {
            method.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                if (isAssignable(value, type)) {
                    method.insertBefore(name + " = " + value + ";");
                } else {
                    System.err.println("Value not assignable to Field " + name + " of type " + type.toString());
                    return false;
                }
            }
            this.getClazzContainer().getClazzLogger().logVariable(name, type, method.getName());
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
                this.getMain().insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
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
            if (this.getClazzLogger().hasVariable(fieldName, methodName)) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                m.insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
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

    public boolean setFieldValue(String fieldName, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        Field field = this.getClazzLogger().getField(fieldName);
        if (method == null || field == null) {
            return false;
        } else {
            return assign(field, value, this.getMethod(methodName));
        }
    }

    public boolean setVariableValue(String varName, Object value, String methodName) {
        Field field = this.getClazzLogger().getVariable(varName, methodName);
        CtMethod method = this.getMethod(methodName);
        if (method == null || field == null) {
            return false;
        } else {
            return assign(field, value, this.getMethod(methodName));
        }
    }

    private boolean assign(Field field, Object value, CtMethod method) {
        if (isAssignable(value, field.getType()) && !field.isFinal()) {
            try {
                if (field.getType() == FieldType.String) {
                    method.insertAfter(field.getName() + " = " + "\"" + value + "\"" + ";");
                } else {
                    method.insertAfter(field.getName() + " = " + value + ";");
                }
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot access global field " + field.getName());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }


    //TODO
//    public boolean assignFieldToField(String fieldName1, String fieldName2) {
//        return false;
//    }

}

//TODO track created Fields in clazzfilecontainer
//TODO maybe add final modifier for local Variables
//TODO assignments of variables of compatible types to each other



