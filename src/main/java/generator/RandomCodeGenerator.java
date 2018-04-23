package generator;

import utils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCodeGenerator {
    private enum Context {
        programContext,
        methodContext;
        //controlContext;
        private int lengthWeighting;
        private MethodLogger contextMethod;

        private void setContextMethod(MethodLogger contextMethod) {
            this.contextMethod = contextMethod;
        }

        private void setLengthWeighting(int lengthWeighting) {
            this.lengthWeighting = lengthWeighting;
        }

        public int getLengthWeighting() {
            return lengthWeighting;
        }

        public MethodLogger getContextMethod() {
            return contextMethod;
        }

    }

    private final FieldVarGenerator fv_generator;
    private final MethodGenerator m_generator;
    private final RandomSupplier randomSupplier;
    private final Random random = new Random();
    private final GenerationController controller;
//    private ControlFlowGenerator cf_generator;
//    private MathGenerator math_generator;


    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.randomSupplier = new RandomSupplier();
        this.fv_generator = new FieldVarGenerator(fileName);
        this.m_generator = new MethodGenerator(fv_generator.getClazzContainer());
        //cf_generator
        //math_generator
        this.controller = controller;
        Context.programContext.setLengthWeighting(controller.getProgramLengthWeighting());
        Context.programContext.setContextMethod(getClazzLogger().getMain());
        Context.methodContext.setLengthWeighting(controller.getMethodLengthWeighting());
    }


    private ClazzLogger getClazzLogger() {
        return fv_generator.getClazzLogger();
    }

    public void generate() {
        generate(Context.programContext);
    }

    private void generate(Context context) {
        for (int i = 0; i < context.getLengthWeighting(); i++) {
            int r = 1 + random.nextInt(100);
            if (context == Context.programContext && r <= controller.getFieldProbability()) generateField();
            if (r <= controller.getVariableProbability()) generateLocalVariable(context);
            if (context == Context.programContext && r <= controller.getMethodProbability()) generateMethod();

            if (r <= controller.getGlobalAssignProbability()) {
                if (random.nextBoolean()) setFieldToReturnValue(context);
                else setFieldValue(context);
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

            if (r <= controller.getMethodCallProbability()) generateMethodCall(context);
            if (context == Context.programContext && r <= controller.getMethodOverloadPropability()) {
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
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateField(randomSupplier.getVarName(), ft, randomSupplier.getModifiers(), value);
    }

    private void generateLocalVariable(Context context) {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateLocalVariable(randomSupplier.getVarName(), ft, context.contextMethod, value);
    }

    private void setFieldValue(Context context) {
        if (getClazzLogger().hasVariables()) {
            FieldVarLogger f = getClazzLogger().getRandomField();
            fv_generator.setFieldVarValue(f, context.getContextMethod(), RandomSupplier.getValue(f.getType()));
        } else return;
    }

    private void setLocalVariableValue(Context context) {
        if (getClazzLogger().hasLocals(context.getContextMethod())) {
            FieldVarLogger f = getClazzLogger().getRandomVariable(context.getContextMethod());
            if (f == null) return;
            fv_generator.setFieldVarValue(f, context.getContextMethod(), RandomSupplier.getValue(f.getType()));
        }
    }

    private void assignFieldToField(Context context) {
        //assign value of a field to another field
        FieldVarLogger f1 = getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    private void assignVarToField(Context context) {
        //assign value of a variable to a field
        FieldVarLogger f1 = getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = getClazzLogger().getRandomCompatibleVariable(f1.getType(), context.getContextMethod());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    private void assignVarToVar(Context context) {
        //assign value of a variable to another variable
        FieldVarLogger f1 = getClazzLogger().getRandomVariable(context.getContextMethod());
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = getClazzLogger().getRandomCompatibleVariable(f1.getType(), context.getContextMethod());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    private void assignFieldToVar(Context context) {
        //assign value of a field to a variable
        FieldVarLogger f1 = getClazzLogger().getRandomVariable(context.contextMethod);
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
            }
        }
    }

    private void generateMethod() {
        String methodName = RandomSupplier.getMethodName();
        MethodLogger method = m_generator.generateMethod(methodName, RandomSupplier.getReturnType(),
                RandomSupplier.getParameterTypes(controller.getMaximumMethodParamters()), RandomSupplier.getModifiers());
        Context.methodContext.setContextMethod(method);
        this.generate(Context.methodContext);
    }

    public void overloadMethod() {
        MethodLogger methodToOverload = getClazzLogger().getRandomMethod();
        if (methodToOverload == null) return;
        boolean equalParamTypes = true;
        FieldVarType[] paramTypes = null;
        while (equalParamTypes) {
            paramTypes = RandomSupplier.getParameterTypes(controller.getMaximumMethodParamters());
            equalParamTypes = m_generator.compareParametersForEquality(paramTypes, methodToOverload.getParamsTypes());
        }
        MethodLogger method = m_generator.generateMethod(methodToOverload.getName(), RandomSupplier.getReturnType(),
                paramTypes, RandomSupplier.getModifiers());
        Context.methodContext.setContextMethod(method);
        this.generate(Context.methodContext);
    }

    private void generateMethodCall(Context context) {
        if (getClazzLogger().hasMethods()) {
            MethodLogger calledMethod = getClazzLogger().getRandomMethod(context.contextMethod);
            if (calledMethod == null) return;
            if (calledMethod.isStatic() || !context.contextMethod.isStatic()) {
                FieldVarType[] paramTypes = calledMethod.getParamsTypes();
                List<Object> values = getParamValues(paramTypes, context.contextMethod);
                m_generator.generateMethodCall(calledMethod, context.getContextMethod(), values.toArray());
            } else return;
        } else return;
    }

    private List<Object> getParamValues(FieldVarType[] paramTypes, MethodLogger method) {
        List<Object> values = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            if(random.nextBoolean()) { //add global variable
                if(!addFieldToParamValues(values, method, t)) {
                    //add local variable if no global variable available
                    if(!addLocalVariableToParamValues( values, method, t)) {
                        //add random value if no variables available
                        values.add(RandomSupplier.getValue(t));
                    }
                }
            } else { //add local variable
                if(!addLocalVariableToParamValues( values, method, t)) {
                    //add global variable if no local variable available
                    if(!addFieldToParamValues(values, method, t)){
                        //add random value if no variables available
                        values.add(RandomSupplier.getValue(t));
                    }
                }
            }
        }
        return values;
    }

    private boolean addFieldToParamValues(List<Object> values, MethodLogger method, FieldVarType t) {
        FieldVarLogger fvl = getClazzLogger().getRandomVariableOfType(t);
        if (fvl != null && (fvl.isStatic() || !method.isStatic())) {
            values.add(fvl);
            return true;
        } else return false;
    }

    private boolean addLocalVariableToParamValues(List<Object> values, MethodLogger method, FieldVarType t) {
        FieldVarLogger fvl = method.getRandomVariableOfType(t);
        if (fvl != null) {
            values.add(fvl);
            return true;
        } else return false;
    }


    public void writeFile() {
        fv_generator.writeFile();
    }

    private void generatePrintStatement(Context context) {
        if (random.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = getClazzLogger().getRandomVariable(context.getContextMethod());
            if (fvl != null)
                fv_generator.generatePrintLocalVariableStatement(fvl, context.getContextMethod());
        } else { //print global Variable
            FieldVarLogger fvl = getClazzLogger().getRandomField();
            if (fvl != null) fv_generator.generatePrintFieldStatement(fvl, context.getContextMethod());
        }
    }

    private void setFieldToReturnValue(Context context) {
        if (m_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = getClazzLogger().getRandomField();
            MethodLogger calledMethod = getClazzLogger().getMethodWithReturnType(f.getType());
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getParamValues(paramTypes, context.contextMethod);
            m_generator.setFieldVarToReturnValue(
                    f, calledMethod, context.getContextMethod(), values.toArray());
        } else return;
    }

    private void setVariableToReturnValue(Context context) {
        if (context.contextMethod.hasVariables()) {
            FieldVarLogger f = context.contextMethod.getRandomVariable();
            MethodLogger calledMethod = getClazzLogger().getMethodWithReturnType(f.getType());
            if (calledMethod == null) return;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            List<Object> values = getParamValues(paramTypes, context.contextMethod);
            m_generator.setFieldVarToReturnValue(
                    f, calledMethod, context.getContextMethod(), values.toArray());
        }
    }
}

//TODO: take arbitrary functions instead of "main"
