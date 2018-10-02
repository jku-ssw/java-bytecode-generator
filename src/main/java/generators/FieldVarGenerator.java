package generators;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.FieldVarType;

class FieldVarGenerator extends Generator {

    public FieldVarGenerator(ClazzFileContainer clazzContainer) {
        super(clazzContainer);
    }

    //===========================================FIELD GENERATION=======================================================

    private void generateField(String name, FieldVarType type, int modifiers, String value) {
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

    public void generateField() {
        FieldVarType ft = this.getRandomSupplier().getFieldVarType();
        String value = null;
        if (this.RANDOM.nextBoolean()) { //50% chance to be initialized
            value = this.getRandomSupplier().getRandomCastedValue(ft);
        }
        this.generateField(getRandomSupplier().getVarName(), ft, this.getRandomSupplier().getFieldModifiers(), value);
    }

    //==========================================LOCAL VARIABLE GENERATION===============================================

    private void generateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        String src = srcGenerateLocalVariable(name, type, method, value);
        if (value != null) insertIntoMethodBody(method, src);
    }

    public void generateLocalVariable(MethodLogger method) {
        FieldVarType ft = getRandomSupplier().getFieldVarType();
        String value = this.getRandomSupplier().getRandomCastedValue(ft);
        String name = getRandomSupplier().getVarName();
        this.generateLocalVariable(name, ft, method, value);
    }

    private String srcGenerateLocalVariable(String name, FieldVarType type, MethodLogger method, String value) {
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.addLocalVariable(name, type.getClazzType());
            String src = name + " = " + value + ";";
            method.logVariable(name, type, 0, true);
            return src;
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    //=============================================PRINT VARIABLES======================================================

    private String srcGeneratePrintStatement(FieldVarLogger variable) {
        return "System.out.println(\"" + variable.getName() + " = \" + " + variable.getName() + ");";
    }

    public void generatePrintStatement(MethodLogger method) {
        String src = this.srcGeneratePrintStatement(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGeneratePrintStatement(MethodLogger method) {
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

    //==========================================SET VARIABLE VALUES=====================================================

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

    public void setFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) {
            return;
        } else {
            FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (f == null) {
                return;
            } else {
                setVarValue(f, method, getRandomSupplier().getRandomCastedValue(f.getType()));
            }
        }
    }

    public String srcSetFieldValue(MethodLogger method) {
        if (!getClazzLogger().hasVariables()) {
            return null;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f == null) {
            return null;
        } else {
            return this.srcSetVarValue(f, getRandomSupplier().getRandomCastedValue(f.getType()));
        }
    }

    public void setLocalVarValue(MethodLogger method) {
        if (!method.hasVariables()) {
            return;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) {
            return;
        } else {
            setVarValue(f, method, getRandomSupplier().getRandomCastedValue(f.getType()));
        }
    }

    public String srcSetLocalVarValue(MethodLogger method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) {
            return null;
        } else {
            return srcSetVarValue(f, getRandomSupplier().getRandomCastedValue(f.getType()));
        }
    }

    //=======================================ASSIGN VARIABLE TO VARIABLE================================================

    public void assignFieldToField(MethodLogger method) {
        String src = srcAssignFieldToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignFieldToField(MethodLogger method) {
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

    public void assignLocalVarToField(MethodLogger method) {
        String src = srcAssignLocalVarToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignLocalVarToField(MethodLogger method) {
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

    public void assignLocalVarToLocalVar(MethodLogger method) {
        String src = srcAssignLocalVarToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignLocalVarToLocalVar(MethodLogger method) {
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

    public void assignFieldToLocalVar(MethodLogger method) {
        String src = srcAssignFieldToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignFieldToLocalVar(MethodLogger method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) {
            return null;
        }
        FieldVarLogger f2 = this.getClazzLogger().
                getNonFinalInitializedCompatibleFieldUsableInMethod(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    private String srcAssignVariableToVariable(FieldVarLogger var1, FieldVarLogger var2) {
        var1.setInitialized();
        return var1.getName() + " = " + var2.getName() + ";";
    }
}




