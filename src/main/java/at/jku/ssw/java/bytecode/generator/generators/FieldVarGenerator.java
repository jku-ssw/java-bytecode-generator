package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.exceptions.MethodCompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;

import java.util.Random;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;

class FieldVarGenerator extends Generator {

    public FieldVarGenerator(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }

    //===========================================FIELD GENERATION=======================================================

    private void generateField(String name, MetaType<?> type, int modifiers, String value) {
        try {
            CtField f = new CtField(JavassistUtils.toCtClass(type), name, this.getClazzContainer().getClazzFile());
            if (value == null) {
                this.getClazzFile().addField(f);
            } else {
                this.getClazzFile().addField(f, value);
            }
            f.setModifiers(modifiers);
            this.getClazzContainer().getClazzLogger().logVariable(name, clazzContainer.getFileName(), type, modifiers, true, true);
        } catch (CannotCompileException e) {
            throw new CompilationFailedException(e);
        }
    }

    public void generateField() {
        MetaType<?> ft = getRandomSupplier().type();
        String value = rand.nextBoolean() // 50% chance to be initialized
                // TODO replace by context lookup for class context
                ? getRandomSupplier()
                .constantOf(ft)
                .map(c -> clazzContainer.resolver().resolve(c))
                .orElse(null)
                : null;
        this.generateField(getRandomSupplier().getVarName(), ft, getRandomSupplier().getFieldModifiers(), value);
    }

    //==========================================LOCAL VARIABLE GENERATION===============================================

    private void generateLocalVariable(String name, MetaType<?> type, MethodLogger<?> method, String value) {
        String src = srcGenerateLocalVariable(name, type, method, value);
        if (value != null) insertIntoMethodBody(method, src);
    }

    public void generateLocalVariable(MethodLogger<?> method) {
        MetaType<?> ft = getRandomSupplier().type();
        String value = clazzContainer.resolver().resolve(getClazzLogger().valueOf(ft, method));
        String name = getRandomSupplier().getVarName();
        this.generateLocalVariable(name, ft, method, value);
    }

    private String srcGenerateLocalVariable(String name, MetaType<?> type, MethodLogger<?> method, String value) {
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.addLocalVariable(name, JavassistUtils.toCtClass(type));
            String src = name + " = " + value + ";";
            method.logVariable(name, clazzContainer.getFileName(), type, 0, true, false);
            return src;
        } catch (CannotCompileException e) {
            throw new MethodCompilationFailedException(method, e);
        }
    }

    //=============================================PRINT VARIABLES======================================================

    private <T> String srcGeneratePrintStatement(FieldVarLogger<T> variable) {
        return Statement(
                SystemOutPrintln(
                        concat(
                                asStr(variable.name + " = "),
                                variable.getType().getHashCode(variable)
                        )
                )
        );
    }

    public void generatePrintStatement(MethodLogger<?> method) {
        String src = this.srcGeneratePrintStatement(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGeneratePrintStatement(MethodLogger<?> method) {
        if (rand.nextBoolean()) { //print local Variable
            FieldVarLogger<?> fvl = method.getVariableWithPredicate(FieldVarLogger::isInitialized);
            if (fvl == null) {
                return null;
            } else {
                return this.srcGeneratePrintStatement(fvl);
            }
        } else { //print global Variable
            FieldVarLogger<?> fvl;
            if (method.isStatic()) {
                fvl = getClazzLogger().getVariableWithPredicate(v -> v.isInitialized() && v.isStatic());
            } else {
                fvl = getClazzLogger().getVariableWithPredicate(FieldVarLogger::isInitialized);
            }
            if (fvl != null) {
                return this.srcGeneratePrintStatement(fvl);
            } else {
                return null;
            }
        }
    }

    //==========================================SET VARIABLE VALUES=====================================================

    private void setVarValue(FieldVarLogger<?> fieldVar, MethodLogger<?> method, String value) {
        String src = srcSetVarValue(fieldVar, value);
        fieldVar.setInitialized();
        insertIntoMethodBody(method, src);
    }

    private String srcSetVarValue(FieldVarLogger<?> fieldVar, String value) {
        if (value != null) {
            fieldVar.setInitialized();
            return fieldVar.access() + " = " + value + ";";
        } else return null;
    }

    public void setFieldValue(MethodLogger<?> method) {
        if (getClazzLogger().hasVariables()) {
            FieldVarLogger<?> f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (f != null) {
                setVarValue(f, method, clazzContainer.resolver().resolve(getClazzLogger().valueOf(f.getType(), method)));
            }
        }
    }

    public String srcSetFieldValue(MethodLogger<?> method) {
        if (!getClazzLogger().hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f == null) {
            return null;
        } else {
            return this.srcSetVarValue(f, clazzContainer.resolver().resolve(getClazzLogger().valueOf(f.getType(), method)));
        }
    }

    public void setLocalVarValue(MethodLogger<?> method) {
        if (!method.hasVariables()) {
            return;
        }
        FieldVarLogger<?> f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f != null) {
            setVarValue(f, method, clazzContainer.resolver().resolve(getClazzLogger().valueOf(f.getType(), method)));
        }
    }

    public String srcSetLocalVarValue(MethodLogger<?> method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f == null) {
            return null;
        } else {
            return srcSetVarValue(f, clazzContainer.resolver().resolve(getClazzLogger().valueOf(f.getType(), method)));
        }
    }

    //=======================================ASSIGN VARIABLE TO VARIABLE================================================

    public void assignFieldToField(MethodLogger<?> method) {
        String src = srcAssignFieldToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignFieldToField(MethodLogger<?> method) {
        if (!getClazzLogger().hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f1, f2;
        f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
        if (f1 != null) {
            f2 = this.getClazzLogger().getNonFinalInitializedCompatibleFieldUsableInMethod(method, f1.getType());
            if (f2 != null) return srcAssignVariableToVariable(f1, f2);
            else return null;
        } else {
            return null;
        }
    }

    public void assignLocalVarToField(MethodLogger<?> method) {
        String src = srcAssignLocalVarToField(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignLocalVarToField(MethodLogger<?> method) {
        if (!getClazzLogger().hasVariables() || !method.hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f1, f2;
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

    public void assignLocalVarToLocalVar(MethodLogger<?> method) {
        String src = srcAssignLocalVarToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignLocalVarToLocalVar(MethodLogger<?> method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) {
            return null;
        }
        FieldVarLogger<?> f2 = this.getClazzLogger().getInitializedCompatibleLocalVar(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    public void assignFieldToLocalVar(MethodLogger<?> method) {
        String src = srcAssignFieldToLocalVar(method);
        insertIntoMethodBody(method, src);
    }

    public String srcAssignFieldToLocalVar(MethodLogger<?> method) {
        if (!method.hasVariables()) {
            return null;
        }
        FieldVarLogger<?> f1 = this.getClazzLogger().getNonFinalLocalVar(method);
        if (f1 == null) {
            return null;
        }
        FieldVarLogger<?> f2 = this.getClazzLogger().
                getNonFinalInitializedCompatibleFieldUsableInMethod(method, f1.getType());
        if (f2 != null) {
            return srcAssignVariableToVariable(f1, f2);
        } else {
            return null;
        }
    }

    private String srcAssignVariableToVariable(FieldVarLogger<?> var1, FieldVarLogger<?> var2) {
        var1.setInitialized();
        return var1.access() + " = " + var2.access() + ";";
    }
}




