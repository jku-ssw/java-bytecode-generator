package generators;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.FieldVarType;

public class FieldVarGenerator extends Generator {

    public FieldVarGenerator(ClazzFileContainer clazzContainer) {
        super(clazzContainer);
    }

    //===========================================field generation=======================================================

    public void generateField(String name, FieldVarType type, int modifiers, String value) {
        try {
            CtField f = new CtField(type.getClazzType(), name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
            } else {
                this.getClazzFile().addField(f, value);
            }
            f.setModifiers(modifiers);
            this.getClazzContainer().getClazzLogger().logVariable(name, type, modifiers, true);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void generateRandomField() {
        FieldVarType ft = this.getRandomSupplier().getFieldVarType();
        String value = null;
        if (this.RANDOM.nextBoolean()) { //50% chance to be initialized
            value = this.getRandomSupplier().getRandomValue(ft);
        }
        this.generateField(getRandomSupplier().getVarName(), ft, this.getRandomSupplier().getModifiers(), value);
    }

    //==========================================local variable generation===============================================

    public void generateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        String src = srcGenerateLocalVariable(name, type, method, value);
        if (src.equals("")) {
            return;
        } else {
            insertIntoMethodBody(method, src);
        }
    }

    public void generateRandomLocalVariable(MethodLogger method) {
        FieldVarType ft = getRandomSupplier().getFieldVarType();
        String value;
        //TODO fix bug local return value assignment in controlflow
        //if (RANDOM.nextBoolean()) { //50% chance to be initialized
        value = getRandomSupplier().getRandomValue(ft);
        //}
        this.generateLocalVariable(getRandomSupplier().getVarName(), ft, method, value);
    }

    private String srcGenerateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        boolean initialized = false;
        CtMethod ctMethod = this.getCtMethod(method);
        String src = null;
        try {
            ctMethod.addLocalVariable(name, type.getClazzType());
            if (value != null) {
                src = name + " = " + value + ";";
                initialized = true;
            }
            method.logVariable(name, type, 0, initialized);
            return src;
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    //=============================================print variables======================================================

    private String srcGeneratePrintStatement(FieldVarLogger variable) {
        return "System.out.println(\"" + variable.getName() + " = \" + " + variable.getName() + ");";
    }

    public void generateRandomPrintStatement(MethodLogger method) {
        String src = this.srcGenerateRandomPrintStatement(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGenerateRandomPrintStatement(MethodLogger method) {
        if (RANDOM.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = method.getVariableWithPredicate(v -> v.isInitialized());
            if (fvl == null) {
                return null;
            } else {
                return this.srcGeneratePrintStatement(fvl);
            }
        } else { //print global Variable
            FieldVarLogger fvl;
            if (method.isStatic()) {
                fvl = getClazzLogger().getVariableWithPredicate(v -> v.isInitialized() && v.isStatic());
            } else {
                fvl = getClazzLogger().getVariableWithPredicate(v -> v.isInitialized());
            }
            if (fvl != null) {
                return this.srcGeneratePrintStatement(fvl);
            } else {
                return null;
            }
        }
    }
    //==========================================set variable values=====================================================

    private void setVarValue(FieldVarLogger fieldVar, MethodLogger method, String value) {
        String src = srcSetVarValue(fieldVar, value);
        fieldVar.setInitialized();
        insertIntoMethodBody(method, src);
    }

    private String srcSetVarValue(FieldVarLogger fieldVar, String value) {
        if (value != null) {
            fieldVar.setInitialized();
            return fieldVar.getName() + " = " + value + ";";
        } else return null;
    }

    public void setRandomFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) {
            return;
        } else {
            FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (f == null) {
                return;
            } else {
                setVarValue(f, method, getRandomSupplier().getRandomValue(f.getType()));
            }
        }
    }

    public String srcSetRandomFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) {
            return null;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f == null) {
            return null;
        } else {
            return this.srcSetVarValue(f, getRandomSupplier().getRandomValue(f.getType()));
        }
    }

    public void setRandomLocalVariableValue(MethodLogger method) {
        if (!method.hasVariables()) {
            return;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) {
            return;
        } else {
            setVarValue(f, method, getRandomSupplier().getRandomValue(f.getType()));
        }
    }

    public String srcSetRandomLocalVariableValue(MethodLogger method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) {
            return null;
        } else {
            return srcSetVarValue(f, getRandomSupplier().getRandomValue(f.getType()));
        }
    }

    //=======================================assign variable to variable================================================

    //field to field
    public void randomlyAssignFieldToField(MethodLogger method) {
        String src = srcRandomlyAssignFieldToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignFieldToField(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) {
            return null;
        }
        FieldVarLogger f1, f2;
        f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f1 != null) {
            f2 = this.getClazzLogger().getNonFinalInitializedCompatibleFieldUsableInMethod(method, f1.getType());
            if (f2 != null) return srcAssignVariableToVariable(f1, f2);
            else return null;
        } else {
            return null;
        }
    }

    //local variable to field
    public void randomlyAssignLocalVarToField(MethodLogger method) {
        String src = srcRandomlyAssignLocalVarToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignLocalVarToField(MethodLogger method) {
        if (!getClazzLogger().hasVariables() || !method.hasVariables()) {
            return null;
        }
        FieldVarLogger f1, f2;
        f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f1 == null) {
            return null;
        }
        f2 = this.getClazzLogger().getInitializedCompatibleLocalVar(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    //local variable to local variable
    public void randomlyAssignLocalVarToLocalVar(MethodLogger method) {
        String src = srcRandomlyAssignLocalVarToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignLocalVarToLocalVar(MethodLogger method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) {
            return null;
        }
        FieldVarLogger f2 = this.getClazzLogger().getInitializedCompatibleLocalVar(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    public void randomlyAssignFieldToLocalVar(MethodLogger method) {
        String src = srcRandomlyAssignFieldToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcRandomlyAssignFieldToLocalVar(MethodLogger method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) {
            return null;
        }
        FieldVarLogger f2 = this.getClazzLogger().getNonFinalInitializedCompatibleFieldUsableInMethod(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    public String srcAssignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2) {
        var1.setInitialized();
        return var1.getName() + " = " + var2.getName() + ";";
    }
}




