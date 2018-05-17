package generators;

import utils.*;
import utils.cli.GenerationController;
import utils.logger.ClazzLogger;
import utils.logger.MethodLogger;

import java.util.Random;

public class RandomCodeGenerator {
    enum Context {
        programContext,
        methodContext,
        controlContext;

        private int lengthWeighting;
        private MethodLogger contextMethod;

        public void setContextMethod(MethodLogger contextMethod) {
            this.contextMethod = contextMethod;
        }
    }

    private final Random random = new Random();
    private final GenerationController controller;

    //Generators
    private final FieldVarGenerator fieldVar_generator;
    private final MethodGenerator method_generator;
    private MathGenerator math_generator;
    private ControlFlowGenerator controlFlow_generator;


    public RandomCodeGenerator(String fileName, GenerationController controller) {
        ClazzFileContainer container = new ClazzFileContainer(fileName);
        this.fieldVar_generator = new FieldVarGenerator(container);
        this.method_generator = new MethodGenerator(this);
        this.math_generator = new MathGenerator(container);
        this.controlFlow_generator = new ControlFlowGenerator(this);

        this.controller = controller;
        Context.programContext.lengthWeighting = controller.getProgramLengthWeighting();
        Context.programContext.contextMethod = getClazzLogger().getMain();
        Context.methodContext.lengthWeighting = controller.getMethodLengthWeighting();
        Context.controlContext.lengthWeighting = controller.getControlLengthWeighting();
    }

    public ClazzFileContainer getClazzFileContainer() {
        return fieldVar_generator.getClazzContainer();
    }


    private ClazzLogger getClazzLogger() {
        return fieldVar_generator.getClazzLogger();
    }

    public void generate() {
        generate(Context.programContext);
    }

    void generate(Context context) {
        for (int i = 0; i < context.lengthWeighting; i++) {
            int r = 1 + random.nextInt(100);

            if (context == Context.programContext && r <= controller.getFieldProbability())
                fieldVar_generator.generateRandomField();

            //TODO maybe add local variable declaration in controlContext
            if (r <= controller.getLocalVariableProbability() && context != Context.controlContext) {
                fieldVar_generator.generateRandomLocalVariable(context.contextMethod);
            }

            if (r <= controller.getGlobalAssignProbability()) {
                String src = null;
                int assignKind = random.nextInt(3);
                switch (assignKind) {
                    case 0: //set field to random value
                        if (context == Context.controlContext)
                            src = fieldVar_generator.srcSetRandomFieldValue(context.contextMethod);
                        else fieldVar_generator.setRandomFieldValue(context.contextMethod);
                        break;
                    case 1: //assign field to field
                        if (context == Context.controlContext)
                            src = fieldVar_generator.srcRandomlyAssignFieldToField(context.contextMethod);
                        else fieldVar_generator.randomlyAssignFieldToField(context.contextMethod);
                        break;
                    case 2: // assign local var to field
                        if (context == Context.controlContext)
                            src = fieldVar_generator.srcRandomlyAssignLocalVarToField(context.contextMethod);
                        else fieldVar_generator.randomlyAssignLocalVarToField(context.contextMethod);
                        break;
                }
                if (src != null) controlFlow_generator.addCodeToControlSrc(src);
            }

            if (r <= controller.getLocalAssignProbability() && context != Context.controlContext) {
                int assignKind = random.nextInt(3);
                String src = null;
                switch (assignKind) {
                    case 0: //set local variable to random value
                        if (context == Context.controlContext) {
                            src = fieldVar_generator.srcSetRandomLocalVariableValue(context.contextMethod);
                        } else fieldVar_generator.setRandomLocalVariableValue(context.contextMethod);
                        break;
                    case 1: //assign local variable to local variable
                        if (context == Context.controlContext) {
                            src = fieldVar_generator.srcRandomlyAssignLocalVarToLocalVar(context.contextMethod);
                        } else fieldVar_generator.randomlyAssignLocalVarToLocalVar(context.contextMethod);
                        break;
                    case 2: // assign field to local variable
                        if (context == Context.controlContext) {
                            src = fieldVar_generator.srcRandomlyAssignFieldToLocalVar(context.contextMethod);
                        } else fieldVar_generator.randomlyAssignFieldToLocalVar(context.contextMethod);
                        break;
                }
                if (src != null) controlFlow_generator.addCodeToControlSrc(src);
            }

            if (context == Context.programContext && r <= controller.getMethodProbability())
                method_generator.generateRandomMethodWithBody(controller.getMaximumMethodParameters());

            if (r <= controller.getMethodCallProbability()) {
                int callKind = 2;//random.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == Context.controlContext) {
                            src = method_generator.srcGenerateRandomMethodCall(context.contextMethod);
                        } else method_generator.generateRandomMethodCall(context.contextMethod);
                        break;
                    case 1: //assign return value of called method to field
                        if (context == Context.controlContext)
                            src = method_generator.srcSetRandomFieldToReturnValue(context.contextMethod);
                        else method_generator.setRandomFieldToReturnValue(context.contextMethod);
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == Context.controlContext) {
                            src = method_generator.srcSetRandomLocalVarToReturnValue(context.contextMethod);
                        } else method_generator.setRandomLocalVarToReturnValue(context.contextMethod);
                        break;
                }
                if (src != null) controlFlow_generator.addCodeToControlSrc(src);
            }

            //TODO check java.lang.Math - Boundries
//            if (r <= controller.getJavaLangMathProbalility()) {
//                int callKind = random.nextInt(3);
//                String src = null;
//                switch (callKind) {
//                    case 0: //call method
//                        if (context == Context.controlContext) {
//                            src = math_generator.srcGenerateRandomMathMethodCall(context.contextMethod);
//                        } else math_generator.generateRandomMathMethodCall(context.contextMethod);
//                        break;
//                    case 1: //assign return value of called method to field
//                        if (context == Context.controlContext)
//                            src = math_generator.srcSetRandomFieldToMathReturnValue(context.contextMethod);
//                        else math_generator.setRandomFieldToMathReturnValue(context.contextMethod);
//                        break;
//                    case 2: //assign return value of called method to local variable
//                        if (context == Context.controlContext) {
//                            src = math_generator.srcSetRandomLocalVarToMathReturnValue(context.contextMethod);
//                        } else math_generator.setRandomLocalVarToMathReturnValue(context.contextMethod);
//                        break;
//                }
//                if (src != null) controlFlow_generator.addCodeToControlSrc(src);
//            }

            if (context == Context.programContext && r <= controller.getMethodOverloadProbability()) {
                method_generator.overLoadRandomMethodWithBody(controller.getMaximumMethodParameters());
            }
            if (r <= controller.getPrintProbability()) {
                String src = null;
                if (context == Context.controlContext) {
                    src = fieldVar_generator.srcGenerateRandomPrintStatement(context.contextMethod);
                } else fieldVar_generator.generateRandomPrintStatement(context.contextMethod);
                if (src != null) controlFlow_generator.addCodeToControlSrc(src);
            }

            if (r <= controller.getControlFlowProbability() &&
                    controlFlow_generator.getDeepness() < controller.getControlFlowDeepness()) {
                //TODO add and select differnent kind of control flow statement
                controlFlow_generator.generateRandomIfStatement(context.contextMethod);
            }
        }
    }

    public void writeFile() {
        fieldVar_generator.writeFile();
    }

    public void writeFile(String directoryName) {
        fieldVar_generator.writeFile(directoryName);
    }


}


//TODO only use initialized Local Vars if in controlContext
//TODO use Sets to generate parameters for Overloading

//TODO Fix Bad Local Variable Bug
//TODO Fix String top exception
//TODO prevent Stack Overflow in generation

//TODO test genration in methodContext

//TODO check for correct Method paramater variable names


//TODO add main to functions again?
//TODO generation-conditions in RandomCodeGenerator?
//TODO fetch more precisely?
