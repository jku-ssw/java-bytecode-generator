package generator;

import utils.*;

import java.util.List;
import java.util.Random;

public class RandomCodeGenerator {
    private enum Context {
        programContext,
        methodContext,
        controlContext;
        private int lengthWeighting;
        private MethodLogger contextMethod;
    }

    private final FieldVarGenerator fv_generator;
    private final MethodGenerator m_generator;
    private final RandomSupplier randomSupplier;
    private final Random random = new Random();
    private final GenerationController controller;
    private MathGenerator math_generator;
    private ControlFlowGenerator cf_generator;


    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.randomSupplier = new RandomSupplier();
        this.fv_generator = new FieldVarGenerator(fileName);
        this.m_generator = new MethodGenerator(fv_generator.getClazzContainer());
        this.math_generator = new MathGenerator(fv_generator.getClazzContainer());
        this.cf_generator = new ControlFlowGenerator(fv_generator.getClazzContainer());

        this.controller = controller;
        Context.programContext.lengthWeighting = controller.getProgramLengthWeighting();
        Context.programContext.contextMethod = getClazzLogger().getMain();
        Context.methodContext.lengthWeighting = controller.getMethodLengthWeighting();
        Context.controlContext.lengthWeighting = controller.getControlLengthWeighting();
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
            if (r <= controller.getLocalVariableProbability() && context != Context.controlContext)
                generateLocalVariable(context);
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
                else if (context != Context.controlContext)
                    setVariableToReturnValue(context); //TODO remove if check controlContext
                if (r <= controller.getVariableToVariableAssignProbability()) {
                    if (random.nextBoolean()) assignVarToVar(context);
                    else assignFieldToVar(context);
                }
            }

            if (r <= controller.getMethodCallProbability() && context != Context.controlContext) {
                generateMethodCall(context); //TODO remove if check controlContext
//                if (r <= controller.getJavaLangMathProbalility()) {
//                    callJavaLangMathMethod(context);
//                }
            }

            //TODO remove if check controlContext
            if (context == Context.programContext && r <= controller.getMethodOverloadProbability()) {
                overloadMethod();
            }
            if (r <= controller.getPrintProbability()) generatePrintStatement(context);

            if (r <= controller.getControlFlowProbability() && cf_generator.getDeepness() < controller.getControlFlowDeepness()) {
                generateControlFlow(context);
            }
        }

        //print all variables
//        for (FieldVarLogger f : fv_generator.getClazzContainer().getClazzLogger().getVariables()) {
//            fv_generator.generatePrintStatement(f, getClazzLogger().getMain());
//        }

//        for (FieldVarLogger f : fv_generator.getClazzContainer().getClazzLogger().getLocals(getClazzLogger().getMain())) {
//            fv_generator.generatePrintStatement(f, getClazzLogger().getMain());
//        }
    }

    private void generateField() {
        FieldVarType ft = randomSupplier.getFieldVarType();
        String value = null;
        if (random.nextBoolean()) { //50% chance to be initialized
            value = randomSupplier.getRandomValueAsString(ft);
        }
        fv_generator.generateField(randomSupplier.getVarName(), ft, randomSupplier.getModifiers(), value);
    }

    private void generateLocalVariable(Context context) {
        FieldVarType ft = randomSupplier.getFieldVarType();
        String value = null;
        if (random.nextBoolean()) { //50% chance to be initialized
            value = randomSupplier.getRandomValueAsString(ft);
        }
        fv_generator.generateLocalVariable(randomSupplier.getVarName(), ft, context.contextMethod, value);
    }


    private void setFieldValue(Context context) {
        if (!getClazzLogger().hasVariables()) return;
        FieldVarLogger f = fetchUsableField(context);
        if (f == null) return;
        setFieldVarValue(f, context);
    }

    private void setFieldVarValue(FieldVarLogger f, Context context) {
        if (context != Context.controlContext) {
            fv_generator.setFieldVarValue(f, context.contextMethod, RandomSupplier.getRandomValueAsString(f.getType()));
        } else {
            cf_generator.addCodeToControlSrc(
                    fv_generator.srcSetFieldVarValue(f, RandomSupplier.getRandomValueAsString(f.getType())));
        }
    }

    private void setLocalVariableValue(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f = fetchUsableLocal(context);
        if (f == null) return;
        setFieldVarValue(f, context);
    }

    private void assignVariableToVariable(FieldVarLogger f1, FieldVarLogger f2, Context context) {
        if (context != Context.controlContext) {
            fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
        } else {
            cf_generator.addCodeToControlSrc(fv_generator.srcAssignVariableToVariable(f1, f2));
        }
    }

    private void assignFieldToField(Context context) {
        if (!getClazzLogger().hasVariables()) return;
        FieldVarLogger f1, f2 = null;
        f1 = fetchUsableField(context);
        if (f1 != null)
            f2 = fetchUsableInitializedFieldOfType(context, f1.getType());
        if (f1 != null && f2 != null) assignVariableToVariable(f1, f2, context);
    }

    private void assignVarToField(Context context) {
        if (!getClazzLogger().hasVariables() || !context.contextMethod.hasVariables()) return;
        FieldVarLogger f1, f2;
        f1 = fetchUsableField(context);
        if (f1 == null) return;
        f2 = fetchUsableInitializedLocalOfType(context, f1.getType());
        if (f2 != null) assignVariableToVariable(f1, f2, context);
    }

    private void assignVarToVar(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f1 = fetchUsableLocal(context);
        if (f1 == null) return;
        FieldVarLogger f2 = fetchUsableInitializedLocalOfType(context, f1.getType());
        if (f2 != null) assignVariableToVariable(f1, f2, context);

    }

    private void assignFieldToVar(Context context) {
        if (!context.contextMethod.hasVariables()) return;
        FieldVarLogger f1 = fetchUsableLocal(context);
        if (f1 == null) return;
        FieldVarLogger f2 = fetchUsableInitializedFieldOfType(context, f1.getType());
        if (f2 != null) assignVariableToVariable(f1, f2, context);
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
        m_generator.overrideReturnStatement(method);
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
            fv_generator.generatePrintStatement(fvl, context.contextMethod);
        } else { //print global Variable
            FieldVarLogger fvl;
            if (context.contextMethod.isStatic()) fvl = getClazzLogger().getVariableWithPredicate(
                    v -> v.isInitialized() && v.isStatic());
            else fvl = getClazzLogger().getVariableWithPredicate(v -> v.isInitialized());
            if (fvl != null) {
                if (context != Context.controlContext) {
                    fv_generator.generatePrintStatement(fvl, context.contextMethod);
                } else {
                    cf_generator.addCodeToControlSrc(fv_generator.srcGeneratePrintStatement(fvl));
                }
            }
        }
    }

    private void setFieldToReturnValue(Context context) {
        if (m_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = fetchUsableField(context);
            if (f == null) return;
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
            FieldVarLogger f = fetchUsableLocal(context);
            if (f == null) return;
            MethodLogger calledMethod = getClazzLogger().getMethodWithReturnType(f.getType());
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getClazzLogger().getParamValues(paramTypes, context.contextMethod);
            m_generator.setFieldVarToReturnValue(
                    f, calledMethod, context.contextMethod, values.toArray());
        }
    }


    //TODO variables to returnvalue of math
    private void callJavaLangMathMethod(Context context) {
        if (random.nextBoolean()) {
            math_generator.callJavaLangMathMethod(context.contextMethod, getClazzLogger(), false);
        } else {
            math_generator.callJavaLangMathMethod(context.contextMethod, getClazzLogger(), true);
        }
    }

    private void generateControlFlow(Context context) {
        //TODO select kind of control flow statement
        cf_generator.openIfStatement(context.contextMethod, getClazzLogger());
        Context.controlContext.contextMethod = context.contextMethod;
        this.generate(Context.controlContext);
        cf_generator.closeStatement();
        if(cf_generator.getDeepness() == 0) {
            cf_generator.insertBlockSrc(context.contextMethod);
        }
    }

    public void writeFile() {
        fv_generator.writeFile();
    }

    //==========================================Variable Fetching======================================================//

    private FieldVarLogger fetchUsableField(Context context) {
        if (context.contextMethod.isStatic()) {
            return getClazzLogger().getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        } else {
            return getClazzLogger().getVariableWithPredicate(v -> !v.isFinal());
        }
    }

    private FieldVarLogger fetchUsableLocal(Context context) {
        return context.contextMethod.getVariableWithPredicate(v -> !v.isFinal());
    }

    private FieldVarLogger fetchUsableInitializedFieldOfType(Context context, FieldVarType type) {
        if (context.contextMethod.isStatic()) {
            return getClazzLogger().getVariableWithPredicate(
                    v -> v.isStatic() && !v.isFinal() && v.isInitialized() && v.getType() == type);
        } else {
            return getClazzLogger().getVariableWithPredicate(
                    v -> !v.isFinal() && v.isInitialized() && v.getType() == type);
        }
    }

    private FieldVarLogger fetchUsableInitializedLocalOfType(Context context, FieldVarType type) {
        return context.contextMethod.getVariableWithPredicate(
                v -> v.isStatic() && !v.isFinal() && v.isInitialized() && v.getType() == type);

    }
}

//TODO add main to functions again?
//TODO generation-conditions in RandomCodeGenerator?
//TODO fetch more precisely?
