package generator;

import utils.*;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class RandomCodeGenerator {
    private enum Context {
        programContext,
        methodContext;
        //controlContext;
        private int lengthWeighting;
        private MethodLogger contextMethod;
    }

    private final FieldVarGenerator fv_generator;
    private final MethodGenerator m_generator;
    private final RandomSupplier randomSupplier;
    private final Random random = new Random();
    private final GenerationController controller;
    private MathGenerator math_generator;
//    private ControlFlowGenerator cf_generator;


    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.randomSupplier = new RandomSupplier();
        this.fv_generator = new FieldVarGenerator(fileName);
        this.m_generator = new MethodGenerator(fv_generator.getClazzContainer());
        this.math_generator = new MathGenerator(fv_generator.getClazzContainer());
        //cf_generator
        this.controller = controller;
        Context.programContext.lengthWeighting = controller.getProgramLengthWeighting();
        Context.programContext.contextMethod = getClazzLogger().getMain();
        Context.methodContext.lengthWeighting = controller.getMethodLengthWeighting();
    }


    private ClazzLogger getClazzLogger() {
        return fv_generator.getClazzLogger();
    }

    public void generate() {
        generate(Context.programContext);
    }

    private void generate(Context context) {
        for (int i = 0; i < context.lengthWeighting; i++) {
            int r = 1 + random.nextInt(100);
            if (context == Context.programContext && r <= controller.getFieldProbability()) generateField();
            if (r <= controller.getVariableProbability()) generateLocalVariable(context);
            if (context == Context.programContext && r <= controller.getMethodProbability()) generateMethod();

            if (r <= controller.getGlobalAssignProbability()) {
                if (random.nextBoolean()) setFieldValue(context);
                else setFieldToReturnValue(context);
                if (r <= controller.getVariableToVariableAssignProbability()) {
                    if (random.nextBoolean()) assignFieldToField(context);
                    else assignVarToField(context);
                }
            }

            if (r <= controller.getLocalAssignProbability()) {
                if (random.nextBoolean()) setLocalVariableValue(context);
                else setVariableToReturnValue(context);
                if (r <= controller.getVariableToVariableAssignProbability()) {
                    if (random.nextBoolean()) assignVarToVar(context);
                    else assignFieldToVar(context);
                }
            }

            if (r <= controller.getMethodCallProbability()) {
                generateMethodCall(context);
                if (r <= controller.getJavaLangMathProbalility()) {
                    callJavaLangMathMethod(context);
                }
            }
            if (context == Context.programContext && r <= controller.getMethodOverloadProbability()) {
                overloadMethod();
            }
            if (r <= controller.getPrintProbability()) generatePrintStatement(context);
        }

//        print all variables
//        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
//            fld_generator.generatePrintFieldStatement(f.getName(), "main");
//        }
//
//        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
//            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
//        }
    }

    private void generateField() {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getRandomValue(ft);
        fv_generator.generateField(randomSupplier.getVarName(), ft, randomSupplier.getModifiers(), value);
    }

    private void generateLocalVariable(Context context) {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getRandomValue(ft);
        fv_generator.generateLocalVariable(randomSupplier.getVarName(), ft, context.contextMethod, value);
    }

    private void setFieldValue(Context context) {
        // fieldVar.isStatic() || !method.isStatic()
        if (!getClazzLogger().hasVariables()) return;
        FieldVarLogger f;
        if (context.contextMethod.isStatic())
            f = getClazzLogger().getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        else f = getClazzLogger().getVariableWithPredicate(v -> !v.isFinal());
        if (f == null) return;
        fv_generator.setFieldVarValue(f, context.contextMethod, RandomSupplier.getRandomValue(f.getType()));

    }

    private void setLocalVariableValue(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f = context.contextMethod.getVariableWithPredicate(v -> !v.isFinal());
        if (f == null) return;
        fv_generator.setFieldVarValue(f, context.contextMethod, RandomSupplier.getRandomValue(f.getType()));
    }

    private void assignFieldToField(Context context) {
        if (!getClazzLogger().hasVariables()) return;
        FieldVarLogger f1, f2 = null;
        if (context.contextMethod.isStatic()) {
            f1 = getClazzLogger().getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
            if (f1 != null)
                f2 = getClazzLogger().getCompatibleVariableWithPredicate(v -> v.isStatic() && v.isInitialized(), f1.getType());
        } else {
            f1 = getClazzLogger().getVariableWithPredicate(v -> !v.isFinal());
            if (f1 != null)
                f2 = getClazzLogger().getCompatibleVariableWithPredicate(v -> v.isInitialized(), f1.getType());
        }
        if (f1 != null && f2 != null) fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
    }

    private void assignVarToField(Context context) {
        if (!getClazzLogger().hasVariables() || !context.contextMethod.hasVariables()) return;
        FieldVarLogger f1, f2;
        if (context.contextMethod.isStatic())
            f1 = getClazzLogger().getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        else f1 = getClazzLogger().getVariableWithPredicate(v -> !v.isFinal());
        if (f1 == null) return;
        f2 = context.contextMethod.getCompatibleVariableWithPredicate(v -> v.isInitialized(), f1.getType());
        if (f2 != null) fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
    }

    private void assignVarToVar(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f1 = context.contextMethod.getVariableWithPredicate(v -> !v.isFinal());
        if (f1 == null) return;
        FieldVarLogger f2 = context.contextMethod.getCompatibleVariableWithPredicate(v -> v.isInitialized(), f1.getType());
        if (f2 != null) fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
    }

    private void assignFieldToVar(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f1 = context.contextMethod.getVariableWithPredicate(v -> !v.isFinal());
        if (f1 == null) return;
        FieldVarLogger f2;
        if (context.contextMethod.isStatic()) {
            f2 = getClazzLogger().getCompatibleVariableWithPredicate(v -> v.isStatic(), f1.getType());
        } else {
            f2 = getClazzLogger().getCompatibleVariable(f1.getType());
        }
        if (f2 != null) fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
    }

    private void generateMethod() {
        String methodName = RandomSupplier.getMethodName();
        MethodLogger method = m_generator.generateMethod(methodName, RandomSupplier.getReturnType(),
                RandomSupplier.getParameterTypes(controller.getMaximumMethodParameters()), RandomSupplier.getModifiers());
        Context.methodContext.contextMethod = method;
        this.generate(Context.methodContext);
        m_generator.overrideReturnStatement(method);
    }

    private void overloadMethod() {
        MethodLogger methodToOverload = getClazzLogger().getRandomMethod();
        if (methodToOverload == null) return;
        boolean equalParamTypes = true;
        FieldVarType[] paramTypes = null;
        while (equalParamTypes) {
            paramTypes = RandomSupplier.getParameterTypes(controller.getMaximumMethodParameters());
            equalParamTypes = m_generator.compareParametersForEquality(paramTypes, methodToOverload.getParamsTypes());
        }
        MethodLogger method = m_generator.generateMethod(methodToOverload.getName(), RandomSupplier.getReturnType(),
                paramTypes, RandomSupplier.getModifiers());
        Context.methodContext.contextMethod = method;
        this.generate(Context.methodContext);
    }

    private void generateMethodCall(Context context) {
        if (getClazzLogger().hasMethods()) {
            MethodLogger calledMethod = getClazzLogger().getRandomCallableMethod(context.contextMethod);
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getClazzLogger().getParamValues(paramTypes, context.contextMethod);
            m_generator.generateMethodCall(calledMethod, context.contextMethod, values.toArray());
        } else return;
    }

    private void generatePrintStatement(Context context) {
        if (random.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = context.contextMethod.getVariableWithPredicate(v -> v.isInitialized());
            if (fvl == null) return;
            fv_generator.generatePrintLocalVariableStatement(fvl, context.contextMethod);
        } else { //print global Variable
            FieldVarLogger fvl = getClazzLogger().getVariable();
            if (fvl != null && fvl.isInitialized() && fvl.isStatic() || !context.contextMethod.isStatic()) {
                fv_generator.generatePrintFieldStatement(fvl, context.contextMethod);
            }
        }
    }

    private void setFieldToReturnValue(Context context) {
        if (m_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = getClazzLogger().getVariable();
            double d = 0;
            MethodLogger calledMethod = getClazzLogger().getMethodWithReturnType(f.getType());
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getClazzLogger().getParamValues(paramTypes, context.contextMethod);
            m_generator.setFieldVarToReturnValue(
                    f, calledMethod, context.contextMethod, values.toArray());
        } else return;
    }

    private void setVariableToReturnValue(Context context) {
        if (context.contextMethod.hasVariables()) {
            FieldVarLogger f = context.contextMethod.getVariable();
            MethodLogger calledMethod = getClazzLogger().getMethodWithReturnType(f.getType());
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getClazzLogger().getParamValues(paramTypes, context.contextMethod);
            m_generator.setFieldVarToReturnValue(
                    f, calledMethod, context.contextMethod, values.toArray());
        }
    }

    private void callJavaLangMathMethod(Context context) {
        if (random.nextBoolean()) {
            math_generator.callJavaLangMathMethod(context.contextMethod, getClazzLogger(), false);
        } else {
            math_generator.callJavaLangMathMethod(context.contextMethod, getClazzLogger(), true);
        }
    }

    public void writeFile() {
        fv_generator.writeFile();
    }
}

//TODO add main to functions again?
//TODO generation-conditions in RandomCodeGenerator?
//TODO fetch more precisely?
