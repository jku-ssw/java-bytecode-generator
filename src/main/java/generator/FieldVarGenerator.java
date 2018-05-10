package generator;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;
import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.MethodLogger;

public class FieldVarGenerator extends Generator {

    public FieldVarGenerator(String filename) {
        super(filename);
    }

    /**
     * generates a global field with given modifiers and optional default value
     *
     * @param name      the name of the generated field
     * @param type      the FieldVarType of the generated field
     * @param value     the value to be assigned to the field, if initialized
     * @param modifiers merged modifiers for the new field (from javassist class Modifier)
     * @return {@code true} if the field was generated successfully, otherwise {@code false}
     */
    public boolean generateField(String name, FieldVarType type, int modifiers, String value) {
        boolean initialized = false;
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
            } else {
                this.getClazzFile().addField(f, value);
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
     * @param name   the name of the generated variable
     * @param type   the FieldVarType of the generated variable
     * @param value  the value to be assigned to the field
     * @param method the logger of the method, in which the variable is generated
     * @return {@code true} if the local variable was generated successfully, otherwise {@code false}
     */
    public boolean generateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        String src = srcGenerateLocalVariable(name, type, method, value);
        if (src == null) return false;
        else if (src.equals("")) return true;
        else {
            CtMethod ctMethod = this.getCtMethod(method);
            if (insertIntoMethod(ctMethod, src)) return true;
            else return false;
        }
    }

    public String srcGenerateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        boolean initialized = false;
        CtMethod ctMethod = this.getCtMethod(method);
        String src;
        try {
            ctMethod.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                src = name + " = " + value + ";";
                initialized = true;
            } else {
                src = type.getName() + " " + name + ";";
            }
            method.logVariable(name, type, 0, initialized);
            return src;
        } catch (CannotCompileException e) {
            System.err.println("Generation of local variable " + name + "  failed");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * generates a System.out.println-Statement in the given method for this field
     *
     * @param variable the logged Variable
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean generatePrintStatement(FieldVarLogger variable, MethodLogger method) {
        try {
            CtMethod ctMethod = this.getCtMethod(method);
            String printStatement = srcGeneratePrintStatement(variable);
            ctMethod.insertAfter(printStatement);
            return true;
        } catch (CannotCompileException e) {
            System.err.println("Generation of System.out.println-Statement for field " + variable.getName() + " failed + Type: " + variable.getType());
            e.printStackTrace();
            return false;
        }
    }

    public String srcGeneratePrintStatement(FieldVarLogger variable) {
        return "System.out.println(\"" + variable.getName() + " = \" + " + variable.getName() + ");";
    }

    /**
     * assigns a value to a field in the given method
     *
     * @param fieldVar the field or variable, which's value is set
     * @param value    the value to be assigned
     * @param method   the logger of the method, in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean setFieldVarValue(FieldVarLogger fieldVar, MethodLogger method, String value) {
        CtMethod ctMethod = this.getCtMethod(method);
        String src = srcSetFieldVarValue(fieldVar, value);
        if (src == null) return false;
        else {
            if (insertIntoMethod(ctMethod, src)) {
                fieldVar.setInitialized();
                return true;
            } else return false;
        }
    }

    private boolean insertIntoMethod(CtMethod ctMethod, String src) {
        try {
            ctMethod.insertAfter(src);
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String srcSetFieldVarValue(FieldVarLogger fieldVar, String value) {
        if (value != null) {
            return fieldVar.getName() + " = " + value + ";";
        } else return null;
    }

    /**
     * @param var1   the field to which the value of field2 is assigned
     * @param var2   the field, which's value is assigned to field1
     * @param method the logger of the method in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    public boolean assignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2, MethodLogger method) {
        String src = srcAssignVariableToVariable(var1, var2);
        CtMethod ctMethod = this.getCtMethod(method);
        if (insertIntoMethod(ctMethod, src)) {
            var1.setInitialized();
            return true;
        } else return false;

    }

    public String srcAssignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2) {
        return var1.getName() + " = " + var2.getName() + ";";
    }
}




