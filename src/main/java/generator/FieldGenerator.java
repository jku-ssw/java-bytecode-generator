package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.Field;
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
                this.getClazzContainer().getClazzLogger().logVariable(name, type, 0);
                return true;
            }
            boolean valid = validateModifiers(modifiers);
            if (valid) {
                int merged_modifiers = modifiers[0];
                for (int i = 1; i < modifiers.length; i++) {
                    merged_modifiers |= modifiers[i];
                }
                f.setModifiers(merged_modifiers);
                this.getClazzContainer().getClazzLogger().logVariable(name, type, merged_modifiers);
                return true;
            } else {
                System.err.println("Invalid Modifiers for global field " + name);
                return false;
            }
        } catch (CannotCompileException e) {
            System.err.println("Generation of static field " + name + " failed");
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
    public boolean generateLocalVariable(String name, FieldType type, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        try {
            method.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                if (isAssignable(value, type)) {
                    if (type == FieldType.String) {
                        method.insertBefore(name + " = " + "\"" + value + "\"" + ";");
                    } else if (type == FieldType.Char) {
                        method.insertBefore(name + " = " + "'" + value + "'" + ";");
                    } else method.insertBefore(name + " = " + value + ";");
                } else {
                    System.err.println("Value not assignable to Field " + name + " of type " + type.toString());
                    return false;
                }
            }
            this.getClazzContainer().getClazzLogger().logVariable(name, type, method.getName());
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of local field " + name + "  failed");
            e.printStackTrace();
            return false;
        }
    }


    public boolean generatePrintFieldStatement(String fieldName) {
        try {
            if (this.getClazzLogger().hasVariable(fieldName)) {
                System.out.println("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
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

    public boolean generatePrintLocalVariableStatement(String fieldName, String methodName) {
        try {
            if (this.getClazzLogger().hasVariable(fieldName, methodName)) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                System.out.println("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
                m.insertAfter("System.out.println(\""+fieldName+"\");");
                return true;
            } else {
                System.err.println("Method " + methodName + " does not contain a Field " + fieldName);
                return false;
            }
        } catch (CannotCompileException | NotFoundException e) {
            Field f = this.getClazzLogger().getVariable(fieldName, methodName);
            System.out.println(f.getName() + " " + f.getType());
            System.err.println("Generation of PrintField-Statement failed");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * assigns a value to a field in the given method
     *
     * @param field      the field, which's value is set
     * @param value      the value to be assigned
     * @param methodName the method in which the assign-statement is generated
     * @return
     */
    public boolean setFieldValue(Field field, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        if (method == null || field == null || !this.getClazzLogger().hasVariable(field.getName())) {
            return false;
        } else {
            return assign(field, value, this.getMethod(methodName));
        }
    }

    /**
     * assigns a value to a variable in the given method
     *
     * @param field      the field, which's value is set
     * @param value      the value to be assigned
     * @param methodName the method in which the variable exists and the assign-statement is generated
     * @return
     */
    public boolean setLocalVariableValue(Field field, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        if (method == null || field == null || !this.getClazzLogger().hasVariable(field.getName(), methodName)) {
            return false;
        } else {
            return assign(field, value, this.getMethod(methodName));
        }
    }

    /**
     * generates an assign-statement
     *
     * @param field  the field to which value is assigned
     * @param value  the value to be assigned
     * @param method the method in which the assign-statement is generated
     * @return
     */
    private boolean assign(Field field, Object value, CtMethod method) {
        if (isAssignable(value, field.getType()) && !field.isFinal()) {
            try {
                if (field.getType() == FieldType.String) {
                    method.insertAfter(field.getName() + " = " + "\"" + value + "\"" + ";");
                } else if (field.getType() == FieldType.Char) {
                    method.insertAfter(field.getName() + " = " + "'" + value + "'" + ";");
                } else method.insertAfter(field.getName() + " = " + value + ";");
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value " + value + "to Variable " + field.getName());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }


    private boolean assign(Field field1, Field field2, CtMethod method) {
        if (isCompatibleTo(field1.getType(), field2.getType()) && !field1.isFinal()) {
            try {
                method.insertAfter(field1.getName() + " = " + field2.getName() + ";");
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value of " + field2.getName() + "to " + field1.getName());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean assignFieldToField(Field field1, Field field2, String methodName) {
        if (!this.getClazzLogger().hasVariable(field1.getName()) || !this.getClazzLogger().hasVariable(field1.getName())) {
            return false;
        }
        return assign(field1, field2, this.getMethod(methodName));
    }

    public boolean assignFieldToVar(Field field1, Field field2, String methodName) {
        if (!this.getClazzLogger().hasVariable(field1.getName()) || !this.getClazzLogger().hasVariable(field2.getName(), methodName)) {
            return false;
        }
        return assign(field1, field2, this.getMethod(methodName));
    }

    public boolean assignVarToField(Field field1, Field field2, String methodName) {
        if (!this.getClazzLogger().hasVariable(field1.getName(), methodName) || !this.getClazzLogger().hasVariable(field2.getName())) {
            return false;
        }
        return assign(field1, field2, this.getMethod(methodName));
    }

    public boolean assignVarToVar(Field field1, Field field2, String methodName) {
        if (!this.getClazzLogger().hasVariable(field1.getName(), methodName) ||
                !this.getClazzLogger().hasVariable(field2.getName(), methodName)) {
            return false;
        }
        return assign(field1, field2, this.getMethod(methodName));
    }

}

//TODO assignments of variables of compatible types to each other



