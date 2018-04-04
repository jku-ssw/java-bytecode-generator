package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldVarContainer;
import utils.FieldVarType;

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
     * generates a global field with optional default value
     *
     * @param name      the name of the generated field
     * @param type      the dataType of the generated field
     * @param value     the value to be assigned to the field, null if the field should not be initialized
     * @param modifiers array of Modifiers for the new field (from javassist class Modifier)
     *                  null for no modifiers
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateField(String name, FieldVarType type, int[] modifiers, Object... value) {
        boolean initialized = false;
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value.length == 0) {
                this.getClazzFile().addField(f);
            } else if (value.length == 1 && value[0] == null && type.getClazzType().getName().startsWith("java.lang")) {
                this.getClazzFile().addField(f, "null");
            } else if (isAssignable(value, type)) {
                if (value.length == 1) {
                    switch (type) {
                        case Byte:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((byte) value[0]));
                            break;
                        case Short:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((short) value[0]));
                            break;
                        case Int:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((int) value[0]));
                            break;
                        case Long:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((long) value[0]));
                            break;
                        case Float:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((float) value[0]));
                            break;
                        case Double:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((double) value[0]));
                            break;
                        case Boolean:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((boolean) value[0]));
                            break;
                        case Char:
                            this.getClazzFile().addField(f, CtField.Initializer.constant((char) value[0]));
                            break;
                        case String:
                            this.getClazzFile().addField(f, "\"" + value[0] + "\"");
                            break;
                    }
                }
                initialized = true;
            } else {
                System.err.println("Incompatible type and value for primitive global field " + name);
                return false;
            }
            if (modifiers == null) {
                this.getClazzContainer().getClazzLogger().logVariable(name, type, 0, initialized);
                return true;
            }
            boolean valid = validateModifiers(modifiers);
            if (valid) {
                int merged_modifiers = mergeModifiers(modifiers);
                f.setModifiers(merged_modifiers);
                this.getClazzContainer().getClazzLogger().logVariable(name, type, merged_modifiers, initialized);
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


    /**
     * @param name       the name of the generated variable
     * @param type       the dataType of the generated variable
     * @param value      the value to be assigned to the field, null if the field should not be initialized
     * @param methodName the method in which the variable is generated
     * @return {@code true} if the variable was generated successfully, otherwise {@code false}
     */
    public boolean generateLocalVariable(String name, FieldVarType type, String methodName, Object... value) {
        boolean initialized = false;
        CtMethod method = this.getMethod(methodName);
        if (method == null) {
            System.err.println("Method " + methodName + " does not exist");
            return false;
        }
        try {
            method.addLocalVariable(name, type.getClazzType());
            //Objects can be initialized with null
            if (value.length == 1 && value[0] == null && type.getClazzType().getName().startsWith("java.lang")) {
                method.insertBefore(name + " = " + "null" + ";");
            } else if (value.length != 0) {
                if (isAssignable(value, type)) {
                    if (value.length == 1) {
                        if (type == FieldVarType.String) {
                            method.insertBefore(name + " = " + "\"" + value[0] + "\"" + ";");
                        } else if (type == FieldVarType.Char) {
                            method.insertBefore(name + " = " + "'" + value[0] + "'" + ";");
                        } else method.insertBefore(name + " = " + value[0] + ";");
                    }
                    initialized = true;
                } else {
                    System.err.println("Value not assignable to Variable " + name + " of type " + type.toString());
                    return false;
                }
            }
            this.getClazzContainer().getClazzLogger().logVariable(name, type, method.getName(), initialized);
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of local variable " + name + "  failed");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * generates a System.out.println-Statement in the given Method for this field
     *
     * @param fieldName the name of the field
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean generatePrintFieldStatement(String fieldName, String methodName) {
        try {
            if (this.getClazzLogger().hasVariable(fieldName) && this.getClazzLogger().hasMethod(methodName) &&
                    (this.getClazzLogger().getVariable(fieldName).isStatic() ||
                            !this.getClazzLogger().getMethodLogger(fieldName).isStatic())) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                m.insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
                return true;
            } else {
                System.err.println("Class does not contain a Field " + fieldName);
                return false;
            }
        } catch (CannotCompileException | NotFoundException e) {
            System.err.println("Generation of PrintField-Statement for Field + " + fieldName + " failed");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * generates a System.out.println-Statement in the given Method for this variable
     *
     * @param varName    the name of the variable
     * @param methodName the method in which the variable is declared and where the statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean generatePrintLocalVariableStatement(String varName, String methodName) {
        try {
            if (this.getClazzLogger().hasVariable(varName, methodName) &&
                    this.getClazzLogger().getVariable(varName, methodName).isInitialized()) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                m.insertAfter("System.out.println(\"" + varName + " = \" + " + varName + ");");
                return true;
            } else {
                System.err.println("Method " + methodName + " does not contain a Variable " + varName);
                return false;
            }
        } catch (CannotCompileException | NotFoundException e) {
            System.err.println("Generation of Print-Statement for Variable/Field " + varName + "failed");
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
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean setFieldValue(FieldVarContainer field, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        if (method == null || field == null || !this.getClazzLogger().hasVariable(field.getName())) {
            return false;
        } else {
            return assign(field, value, method);
        }
    }

    /**
     * assigns a value to a variable in the given method
     *
     * @param variable   the variable, which's value is set
     * @param value      the value to be assigned
     * @param methodName the method in which the variable exists and the assign-statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean setLocalVariableValue(FieldVarContainer variable, Object value, String methodName) {
        CtMethod method = this.getMethod(methodName);
        if (method == null || variable == null || !this.getClazzLogger().hasVariable(variable.getName(), methodName)) {
            return false;
        } else {
            return assign(variable, value, method);
        }
    }

    /**
     * assigns the value to the given field or variable
     *
     * @param fieldVar the field or variable to which value is assigned
     * @param value    the value to be assigned
     * @param method   the method in which the assign-statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    private boolean assign(FieldVarContainer fieldVar, Object value, CtMethod method) {
        //later: if value is of type Array -> cast to Object[]
        if (isAssignable(new Object[]{value}, fieldVar.getType()) && !fieldVar.isFinal()) {
            try {
                if (fieldVar.getType() == FieldVarType.String) {
                    method.insertAfter(fieldVar.getName() + " = " + "\"" + value + "\"" + ";");
                } else if (fieldVar.getType() == FieldVarType.Char) {
                    method.insertAfter(fieldVar.getName() + " = " + "'" + value + "'" + ";");
                } else method.insertAfter(fieldVar.getName() + " = " + value + ";");
                fieldVar.setInitialized();
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value " + value + "to Variable " + fieldVar.getName());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * assigns the value of a field or variable to another field or variable
     *
     * @param fieldVar1 the field or variable to which the value of fieldVar2 is assigned
     * @param fieldVar2 the field or variable, which's value is assigned to fieldVar1
     * @param method    the method in which the assign-statement is generated
     * @return
     */
    private boolean assign(FieldVarContainer fieldVar1, FieldVarContainer fieldVar2, CtMethod method) {
        if (isCompatibleTo(fieldVar1.getType(), fieldVar2.getType()) && !fieldVar1.isFinal() && method != null) {
            try {
                method.insertAfter(fieldVar1.getName() + " = " + fieldVar2.getName() + ";");
                fieldVar1.setInitialized();
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value of " + fieldVar2.getName() + "to " + fieldVar1.getName());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @param field1     the field to which the value of field2is assigned
     * @param field2     the field, which's value is assigned to field1
     * @param methodName the name of the method in which the assign-statement is generated
     * @return
     */
    public boolean assignFieldToField(FieldVarContainer field1, FieldVarContainer field2, String methodName) {
        if (!this.getClazzLogger().hasVariable(field1.getName()) || !this.getClazzLogger().hasVariable(field1.getName())) {
            return false;
        }
        return assign(field1, field2, this.getMethod(methodName));
    }

    /**
     * @param variable   the variable to which the value of field is assigned
     * @param field      the field, which's value is assigned to variable
     * @param methodName the name of the method in which the assign-statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean assignFieldToVar(FieldVarContainer variable, FieldVarContainer field, String methodName) {
        if (!this.getClazzLogger().hasVariable(variable.getName()) || !this.getClazzLogger().hasVariable(field.getName(), methodName)) {
            return false;
        }
        return assign(variable, field, this.getMethod(methodName));
    }

    /**
     * @param field      the field to which the value of variable is assigned
     * @param variable   the variable, which's value is assigned to field
     * @param methodName the name of the method in which the assign-statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean assignVarToField(FieldVarContainer field, FieldVarContainer variable, String methodName) {
        if (!this.getClazzLogger().hasVariable(field.getName(), methodName) || !this.getClazzLogger().hasVariable(variable.getName())) {
            return false;
        }
        return assign(field, variable, this.getMethod(methodName));
    }

    /**
     * @param variable1  the variable to which the value of variable2 is assigned
     * @param variable2  the variable, which's value is assigned to variable1
     * @param methodName the name of the method in which the assign-statement is generated
     * @return {@code true} if the the statement was generated successfully, otherwise {@code false}
     */
    public boolean assignVarToVar(FieldVarContainer variable1, FieldVarContainer variable2, String methodName) {
        if (!this.getClazzLogger().hasVariable(variable1.getName(), methodName) ||
                !this.getClazzLogger().hasVariable(variable2.getName(), methodName)) {
            return false;
        }
        return assign(variable1, variable2, this.getMethod(methodName));
    }
}



