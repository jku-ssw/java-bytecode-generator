package generator;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;
import utils.ClazzFileContainer;
import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.MethodLogger;

public class FieldVarGenerator extends Generator {

    public FieldVarGenerator(String filename) {
        super(filename);
    }

    public FieldVarGenerator(ClazzFileContainer clazzContainer) {
        super(clazzContainer);
    }

    //===========================================field generation=======================================================

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

    public void generateRandomField() {
        FieldVarType ft = this.getRandomSupplier().getFieldVarType();
        String value = null;
        if (this.random.nextBoolean()) { //50% chance to be initialized
            value = this.getRandomSupplier().getRandomValueAsString(ft);
        }
        this.generateField(
                getRandomSupplier().getVarName(), ft, this.getRandomSupplier().getModifiers(), value);
    }

    //==========================================local variable generation===============================================

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
        else return insertIntoMethodBody(method, src);
    }

    public void generateRandomLocalVariable(MethodLogger method) {
        FieldVarType ft = getRandomSupplier().getFieldVarType();
        String value = null;
        if (random.nextBoolean()) { //50% chance to be initialized
            value = getRandomSupplier().getRandomValueAsString(ft);
        }
        this.generateLocalVariable(getRandomSupplier().getVarName(), ft, method, value);
    }

    private String srcGenerateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        boolean initialized = false;
        CtMethod ctMethod = this.getCtMethod(method);
        String src = "";
        try {
            ctMethod.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                src = name + " = " + value + ";";
                initialized = true;
            }
            method.logVariable(name, type, 0, initialized);
            return src;
        } catch (CannotCompileException e) {
            System.err.println("Generation of local variable " + name + "  failed");
            e.printStackTrace();
            return null;
        }
    }

    //=============================================print variables======================================================

//    /**
//     * generates a System.out.println-Statement in the given method for this field
//     *
//     * @param variable the logged Variable
//     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
//     */
//    private boolean generatePrintStatement(FieldVarLogger variable, MethodLogger method) {
//        String src = srcGeneratePrintStatement(variable);
//        return insertIntoMethodBody(
//                method, src);
//    }

    private String srcGeneratePrintStatement(FieldVarLogger variable) {
        return "System.out.println(\"" + variable.getName() + " = \" + " + variable.getName() + ");";
    }

    public boolean generateRandomPrintStatement(MethodLogger method) {
        String src = this.srcGenerateRandomPrintStatement(method);
        return insertIntoMethodBody(
                method, src);
    }

    public String srcGenerateRandomPrintStatement(MethodLogger method) {
        if (random.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = method.getVariableWithPredicate(v -> v.isInitialized());
            if (fvl == null) return null;
            return this.srcGeneratePrintStatement(fvl);
        } else { //print global Variable
            FieldVarLogger fvl;
            if (method.isStatic()) fvl = getClazzLogger().getVariableWithPredicate(
                    v -> v.isInitialized() && v.isStatic());
            else fvl = getClazzLogger().getVariableWithPredicate(v -> v.isInitialized());
            if (fvl != null) return this.srcGeneratePrintStatement(fvl);
            else return null;
        }
    }
    //==========================================set variable values=====================================================

    /**
     * assigns a value to a field in the given method
     *
     * @param fieldVar the field or variable, which's value is set
     * @param value    the value to be assigned
     * @param method   the logger of the method, in which the assign-statement is generated
     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
     */
    private boolean setVarValue(FieldVarLogger fieldVar, MethodLogger method, String value) {
        String src = srcSetVarValue(fieldVar, value);
        fieldVar.setInitialized();
        return insertIntoMethodBody(method, src);
    }

    private String srcSetVarValue(FieldVarLogger fieldVar, String value) {
        if (value != null) {
            fieldVar.setInitialized();
            return fieldVar.getName() + " = " + value + ";";
        } else return null;
    }

    public boolean setRandomFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) return false;
        FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f == null) return false;
        else return setVarValue(f, method, getRandomSupplier().getRandomValueAsString(f.getType()));
    }

    public String srcSetRandomFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) return null;
        FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f == null) return null;
        return this.srcSetVarValue(f, getRandomSupplier().getRandomValueAsString(f.getType()));
    }

    public boolean setRandomLocalVariableValue(MethodLogger method) {
        if (!method.hasVariables()) return false;
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) return false;
        return setVarValue(f, method, getRandomSupplier().getRandomValueAsString(f.getType()));
    }

    public String srcSetRandomLocalVariableValue(MethodLogger method) {
        if (!method.hasVariables()) return null;
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) return null;
        return srcSetVarValue(f, getRandomSupplier().getRandomValueAsString(f.getType()));
    }


    //=======================================assign variable to variable================================================

    //field to field
    public boolean randomlyAssignFieldToField(MethodLogger method) {
        String src = srcRandomlyAssignFieldToField(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignFieldToField(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) return null;
        FieldVarLogger f1, f2;
        f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f1 != null) {
            f2 = this.getClazzLogger().getNonFinalInitializedFieldOfTypeUsableInMethod(method, f1.getType());
            if (f2 != null) return srcAssignVariableToVariable(f1, f2);
            else return null;
        } else return null;
    }

    //local variable to field
    public boolean randomlyAssignLocalVarToField(MethodLogger method) {
        String src = srcRandomlyAssignLocalVarToField(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignLocalVarToField(MethodLogger method) {
        if (!getClazzLogger().hasVariables() || !method.hasVariables()) return null;
        FieldVarLogger f1, f2;
        f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f1 == null) return null;
        f2 = this.getClazzLogger().getInitializedLocalVarOfType(method, f1.getType());
        if (f2 != null) return srcAssignVariableToVariable(f1, f2);
        else return null;
    }

    //local variable to local variable
    public boolean randomlyAssignLocalVarToLocalVar(MethodLogger method) {
        String src = srcRandomlyAssignLocalVarToLocalVar(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignLocalVarToLocalVar(MethodLogger method) {
        if (!method.hasVariables()) return null;
        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) return null;
        FieldVarLogger f2 = this.getClazzLogger().getInitializedLocalVarOfType(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else return null;
    }

    public boolean randomlyAssignFieldToLocalVar(MethodLogger method) {
        String src = srcRandomlyAssignFieldToLocalVar(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignFieldToLocalVar(MethodLogger method) {
        if (!method.hasVariables()) return null;
        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) return null;
        FieldVarLogger f2 = this.getClazzLogger().getNonFinalInitializedFieldOfTypeUsableInMethod(method, f1.getType());
        if (f2 != null) return srcAssignVariableToVariable(f1, f2);
        else return null;
    }

//    /**
//     * @param var1   the field to which the value of field2 is assigned
//     * @param var2   the field, which's value is assigned to field1
//     * @param method the logger of the method in which the assign-statement is generated
//     * @return {@code true} if the statement was generated successfully, otherwise {@code false}
//     */
//    private boolean assignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2, MethodLogger method) {
//        String src = srcAssignVariableToVariable(var1, var2);
//        var1.setInitialized();
//        return insertIntoMethodBody(method, src);
//    }

    public String srcAssignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2) {
        var1.setInitialized();
        return var1.getName() + " = " + var2.getName() + ";";
    }

}




