package generator;

import utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCodeGenerator {
    private enum Context {
        programContext,
        methodContext,
        controlContext;
        private int lengthWeighting;
        private String contextMethod;

        private void setContextMethod(String contextMethod) {
            this.contextMethod = contextMethod;
        }

        private void setLengthWeighting(int lengthWeighting) {
            this.lengthWeighting = lengthWeighting;
        }

        public int getLengthWeighting() {
            return lengthWeighting;
        }

        public String getContextMethod() {
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
        Context.programContext.setContextMethod("main");
        Context.methodContext.setLengthWeighting(controller.getMethodLengthWeighting());
    }

    public void generate() {
        generate(Context.programContext);
    }

    private void generate(Context context) {
        for (int i = 0; i < context.getLengthWeighting(); i++) {
            int r = 1 + random.nextInt(100);
            if (context == Context.programContext && r <= controller.getFieldProbability()) generateField();
            if (context == Context.programContext && r <= controller.getMethodProbability()) generateMethod();
            if (r <= controller.getVariableToVariableAssignProbability()) {
                if (r <= controller.getGlobalAssignProbability()) {
                    if (random.nextBoolean()) assignFieldToField(context);
                    else assignVarToField(context);
                }
                if (r <= controller.getLocalAssignProbability()) {
                    if (random.nextBoolean()) assignVarToVar(context);
                    else assignFieldToVar(context);
                }
            }
            if (r <= controller.getMethodCallProbability()) generateMethodCall(context);
            if (r <= controller.getGlobalAssignProbability()) {
                if (random.nextBoolean()) setFieldValue(context);
                else setFieldToReturnValue(context);
            }
            if (r <= controller.getPrintProbability()) generatePrintStatement(context);
            if (r <= controller.getVariableProbability()) generateLocalVariable(context);
            if (r <= controller.getLocalAssignProbability()) {
                if (random.nextBoolean()) setLocalVariableValue(context);
                else setLocalVariableValueToReturnValue();
            }
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

    public void generateField() {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateField(randomSupplier.getVarName(), ft, randomSupplier.getModifiers(), value);
    }

    public void generateLocalVariable(Context context) {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateLocalVariable(randomSupplier.getVarName(), ft, context.contextMethod, value);
    }

    public void setFieldValue(Context context) {
        if (fv_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomField();
            fv_generator.setFieldVarValue(f, context.getContextMethod(), RandomSupplier.getValue(f.getType()));
        } else return;
    }

    public void setLocalVariableValue(Context context) {
        if (fv_generator.getClazzLogger().hasLocals(context.getContextMethod())) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomVariable(context.getContextMethod());
            if (f == null) return;
            fv_generator.setFieldVarValue(f, context.getContextMethod(), RandomSupplier.getValue(f.getType()));
        }
    }

    public void assignFieldToField(Context context) {
        //assign value of a field to another field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    public void assignVarToField(Context context) {
        //assign value of a variable to a field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), context.getContextMethod());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    public void assignVarToVar(Context context) {
        //assign value of a variable to another variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable(context.getContextMethod());
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), context.getContextMethod());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.getContextMethod());
            }
        }
    }

    public void assignFieldToVar(Context context) {
        //assign value of a field to a variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable(context.contextMethod);
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, context.contextMethod);
            }
        }
    }

    public void generateMethod() {
        String methodName = RandomSupplier.getMethodName();
        m_generator.generateMethod(methodName, RandomSupplier.getReturnType(),
                RandomSupplier.getParameterTypes(controller.getMaximumMethodParamters()), RandomSupplier.getModifiers());
        Context.methodContext.setContextMethod(methodName);
        this.generate(Context.methodContext);
        //m_generator.addReturnStatement(methodName);
    }

    public void generateMethodCall(Context context) {
        if (fv_generator.getClazzLogger().hasMethods()) {
            MethodLogger l = fv_generator.getClazzLogger().getRandomMethod(context.contextMethod);
            if (l == null) return;
            FieldVarType[] paramTypes = l.getParamsTypes();
            List<Object> values = getParamValues(paramTypes);
            m_generator.generateMethodCall(l.getName(), context.getContextMethod(), values.toArray());
        } else return;
    }

    public void writeFile() {
        fv_generator.writeFile();
    }

    public void generatePrintStatement(Context context) {
        if (random.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariable(context.getContextMethod());
            if (fvl != null)
                fv_generator.generatePrintLocalVariableStatement(fvl.getName(), context.getContextMethod());
        } else { //print global Variable
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomField();
            if (fvl != null) fv_generator.generatePrintFieldStatement(fvl.getName(), context.getContextMethod());
        }
    }

    public void setFieldToReturnValue(Context context) {
        if (m_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomField();
            MethodLogger l = fv_generator.getClazzLogger().getMethodWithReturnType(f.getType());
            if (l == null) return;
            FieldVarType[] paramTypes = l.getParamsTypes();
            List<Object> values = getParamValues(paramTypes);
            m_generator.setFieldToReturnValue(f, l.getName(), context.getContextMethod(), values.toArray());
        } else return;
    }

    private List<Object> getParamValues(FieldVarType[] paramTypes) {
        List<Object> values = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariableOfType(t);
            if (fvl != null) values.add(fvl);
            else values.add(RandomSupplier.getValue(t));
        }
        return values;
    }

    //TODO
    public void setLocalVariableValueToReturnValue() {
        //FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariable("main");
    }
}

//TODO: take arbitrary functions instead of "main"
