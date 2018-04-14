package generator;

import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.MethodLogger;

public class FieldVarGenerator extends Generator {

    public FieldVarGenerator(String filename) {
        super(filename);
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
    public boolean generateField(String name, FieldVarType type, int modifiers, Object... value) {
        boolean initialized = false;
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value.length == 0) {
                this.getClazzFile().addField(f);
            } else if (value.length == 1 && value[0] == null && type.getClazzType().getName().startsWith("java.lang")) {
                //Objects can be initialized with null
                this.getClazzFile().addField(f, "null");
            } else {
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
            }
            f.setModifiers(modifiers);
            this.getClazzContainer().getClazzLogger().logVariable(name, type, modifiers, initialized);
            return true;
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
            System.err.println("Generation of local variable " + name + "  failed: Method " + methodName + " does not exist");
            return false;
        }
        try {
            method.addLocalVariable(name, type.getClazzType());
            if (value.length == 1 && value[0] == null && type.getClazzType().getName().startsWith("java.lang")) {
                //Objects can be initialized with null
                method.insertBefore(name + " = " + "null" + ";");
            } else if (value.length != 0) {
                if (value.length == 1) {
                    if (type == FieldVarType.String) {
                        method.insertBefore(name + " = " + "\"" + value[0] + "\"" + ";");
                    } else if (type == FieldVarType.Char) {
                        method.insertBefore(name + " = " + "'" + value[0] + "'" + ";");
                    } else method.insertBefore(name + " = " + value[0] + ";");
                }
                initialized = true;
            }
            MethodLogger ml = this.getClazzLogger().getMethodLogger(methodName);
            ml.logVariable(name, type, 0, initialized);
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
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean generatePrintFieldStatement(String fieldName, String methodName) {
        try {
            if (this.getClazzLogger().getVariable(fieldName).isStatic() ||
                    !this.getClazzLogger().getMethodLogger(methodName).isStatic()) {
                CtMethod m = this.getMethod(methodName);
                m.insertAfter("System.out.println(\"" + fieldName + " = \" + " + fieldName + ");");
                return true;
            } else return false;
        } catch (CannotCompileException e) {
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
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean generatePrintLocalVariableStatement(String varName, String methodName) {
        try {
            if (this.getClazzLogger().getVariable(varName, methodName).isInitialized()) {
                CtMethod m = this.getClazzFile().getDeclaredMethod(methodName);
                m.insertAfter("System.out.println(\"" + varName + " = \" + " + varName + ");");
                return true;
            } else return false;
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
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean setFieldVarValue(FieldVarLogger field, String methodName, Object... value) {
        CtMethod method = this.getMethod(methodName);
        return createsetFieldStatement(field, method, value);
    }

    /**
     * assigns the value to the given field or variable
     *
     * @param fieldVar the field or variable to which value is assigned
     * @param value    the value to be assigned
     * @param method   the method in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    private boolean createsetFieldStatement(FieldVarLogger fieldVar, CtMethod method, Object... value) {
        if (!fieldVar.isFinal()) {
            try {
                if (value.length == 1 && value[0] == null && fieldVar.getType().getName().startsWith("java.lang")) {
                    //Objects can be initialized with null
                    method.insertAfter(fieldVar.getName() + " = " + "null" + ";");
                    return true;
                } else if (value.length >= 1) {
                    if (value.length == 1) {
                        if (fieldVar.getType() == FieldVarType.String) {
                            method.insertAfter(fieldVar.getName() + " = " + "\"" + value[0] + "\"" + ";");
                        } else if (fieldVar.getType() == FieldVarType.Char) {
                            method.insertAfter(fieldVar.getName() + " = " + "'" + value[0] + "'" + ";");
                        } else method.insertAfter(fieldVar.getName() + " = " + value[0] + ";");
                        fieldVar.setInitialized();
                        return true;
                    } else return false; //TODO value.length > 1 => Arrays
                } else return false;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value " + value + "to Variable " + fieldVar.getName());
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    /**
     * assigns the value of a field or variable to another field or variable
     *
     * @param fieldVar1 the field or variable to which the value of fieldVar2 is assigned
     * @param fieldVar2 the field or variable, which's value is assigned to fieldVar1
     * @param method    the method in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    private boolean createAssignStatement(FieldVarLogger fieldVar1, FieldVarLogger fieldVar2, CtMethod method) {
        if (!fieldVar1.isFinal()) {
            try {
                method.insertAfter(fieldVar1.getName() + " = " + fieldVar2.getName() + ";");
                fieldVar1.setInitialized();
                return true;
            } catch (CannotCompileException e) {
                System.err.println("Cannot assign value of " + fieldVar2.getName() + "to " + fieldVar1.getName());
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    /**
     * @param field1     the field to which the value of field2is assigned
     * @param field2     the field, which's value is assigned to field1
     * @param methodName the name of the method in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean assignVariableToVariable(FieldVarLogger field1, FieldVarLogger field2, String methodName) {
        return createAssignStatement(field1, field2, this.getMethod(methodName));
    }
}




