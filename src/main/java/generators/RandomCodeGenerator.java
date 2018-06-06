package generators;

import cli.GenerationController;
import javassist.CannotCompileException;
import javassist.CtMethod;
import logger.ClazzLogger;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.FieldVarType;

import java.util.List;
import java.util.Random;

public class RandomCodeGenerator {
    enum Context {
        PROGRAM_CONTEXT,
        METHOD_CONTEXT,
        CONTROL_CONTEXT;

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
    private final MathGenerator math_generator;
    private final ControlFlowGenerator controlFlow_generator;


    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.controller = controller;
        ClazzFileContainer container = new ClazzFileContainer(fileName);
        this.fieldVar_generator = new FieldVarGenerator(container);
        this.method_generator = new MethodGenerator(this);
        this.math_generator = new MathGenerator(container);
        this.controlFlow_generator = new ControlFlowGenerator(this);

        MethodLogger run = this.method_generator.generateAndCallRunMethod();
        Context.PROGRAM_CONTEXT.lengthWeighting = controller.getProgramLengthWeighting();
        Context.PROGRAM_CONTEXT.contextMethod = run;
        Context.METHOD_CONTEXT.lengthWeighting = controller.getMethodLengthWeighting();
        Context.CONTROL_CONTEXT.lengthWeighting = controller.getControlLengthWeighting();
    }

    public GenerationController getController() {
        return controller;
    }

    public ClazzFileContainer getClazzFileContainer() {
        return fieldVar_generator.getClazzContainer();
    }


    private ClazzLogger getClazzLogger() {
        return fieldVar_generator.getClazzLogger();
    }

    public void generate() {
        generate(Context.PROGRAM_CONTEXT);
        //compute HashValue of all locals
        computeHash();
    }

    void generate(Context context) {
        for (int i = 0; i < context.lengthWeighting; i++) {
            int r = 1 + random.nextInt(100);

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getFieldProbability()) {
                fieldVar_generator.generateRandomField();
            }

            //TODO maybe add local variable declaration in CONTROL_CONTEXT
            if (r <= controller.getLocalVariableProbability() && context != Context.CONTROL_CONTEXT) {
                fieldVar_generator.generateRandomLocalVariable(context.contextMethod);
            }

            if (r <= controller.getGlobalAssignProbability()) {
                String src = null;
                int assignKind = random.nextInt(3);
                switch (assignKind) {
                    case 0: //set field to random value
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcSetRandomFieldValue(context.contextMethod);
                        } else {
                            fieldVar_generator.setRandomFieldValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign field to field
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcRandomlyAssignFieldToField(context.contextMethod);
                        } else {
                            fieldVar_generator.randomlyAssignFieldToField(context.contextMethod);
                        }
                        break;
                    case 2: // assign local var to field
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcRandomlyAssignLocalVarToField(context.contextMethod);
                        } else {
                            fieldVar_generator.randomlyAssignLocalVarToField(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlow_generator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getLocalAssignProbability() && context != Context.CONTROL_CONTEXT) {
                int assignKind = random.nextInt(3);
                String src = null;
                switch (assignKind) {
                    case 0: //set local variable to random value
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcSetRandomLocalVariableValue(context.contextMethod);
                        } else {
                            fieldVar_generator.setRandomLocalVariableValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign local variable to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcRandomlyAssignLocalVarToLocalVar(context.contextMethod);
                        } else {
                            fieldVar_generator.randomlyAssignLocalVarToLocalVar(context.contextMethod);
                        }
                        break;
                    case 2: // assign field to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVar_generator.srcRandomlyAssignFieldToLocalVar(context.contextMethod);
                        } else {
                            fieldVar_generator.randomlyAssignFieldToLocalVar(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlow_generator.addCodeToControlSrc(src);
                }
            }

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getMethodProbability()) {
                method_generator.generateRandomMethodWithBody(controller.getMaximumMethodParameters());
            }

//            if (context == Context.PROGRAM_CONTEXT && r <= controller.getMethodOverloadProbability()) {
//                method_generator.overLoadRandomMethodWithBody(controller.getMaximumMethodParameters());
//            }

            if (r <= controller.getMethodCallProbability()) {
                int callKind = random.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == Context.CONTROL_CONTEXT) {
                            src = method_generator.srcGenerateRandomMethodCall(context.contextMethod);
                        } else {
                            method_generator.generateRandomMethodCall(context.contextMethod);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == Context.CONTROL_CONTEXT)
                            src = method_generator.srcSetRandomFieldToReturnValue(context.contextMethod);
                        else {
                            method_generator.setRandomFieldToReturnValue(context.contextMethod);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = method_generator.srcSetRandomLocalVarToReturnValue(context.contextMethod);
                        } else {
                            method_generator.setRandomLocalVarToReturnValue(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlow_generator.addCodeToControlSrc(src);
                }
            }
            if (r <= controller.getJavaLangMathProbability()) {
                boolean noOverflow = false;
                if(r <= this.controller.getAvoidOverFlowProbability()) {
                    noOverflow = true;
                }
                int callKind = random.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == Context.CONTROL_CONTEXT) {
                            src = math_generator.srcGenerateRandomMathMethodCall(context.contextMethod, noOverflow);
                        } else {
                            math_generator.generateRandomMathMethodCall(context.contextMethod, noOverflow);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == Context.CONTROL_CONTEXT)
                            src = math_generator.srcSetRandomFieldToMathReturnValue(context.contextMethod, noOverflow);
                        else {
                            math_generator.setRandomFieldToMathReturnValue(context.contextMethod, noOverflow);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = math_generator.srcSetRandomLocalVarToMathReturnValue(context.contextMethod, noOverflow);
                        } else {
                            math_generator.setRandomLocalVarToMathReturnValue(context.contextMethod, noOverflow);
                        }
                        break;
                }
                if (src != null) {
                    controlFlow_generator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getPrintProbability()) {
                String src = null;
                if (context == Context.CONTROL_CONTEXT) {
                    src = fieldVar_generator.srcGenerateRandomPrintStatement(context.contextMethod);
                } else {
                    fieldVar_generator.generateRandomPrintStatement(context.contextMethod);
                }
                if (src != null) {
                    controlFlow_generator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getControlFlowProbability() &&
                    controlFlow_generator.getDeepness() < controller.getControlFlowDeepness()) {
                int controlKind = random.nextInt(4);
                boolean noStatementGenerated = true;
                for (int j = 0; j < 4 && noStatementGenerated; j++) {
                    switch (controlKind) {
                        case 0:
                            if (r <= controller.getIfProbability()) {
                                controlFlow_generator.generateRandomIfElseStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 1;
                            }
                            break;
                        case 1:
                            if (r < controller.getWhileProbability()) {
                                controlFlow_generator.generateRandomWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 2;
                            }
                            break;
                        case 2:
                            if (r < controller.getDoWhileProbability()) {
                                controlFlow_generator.generateRandomDoWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 3;
                            }
                            break;
                        case 3:
                            if (r < controller.getForProbability()) {
                                controlFlow_generator.generateRandomForStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 0;
                            }
                            break;
                    }
                }
            }
        }
    }

    public void writeFile() {
        fieldVar_generator.writeFile();
    }

    public void writeFile(String directoryName) {
        fieldVar_generator.writeFile(directoryName);
    }

    private void computeHash() {
        StringBuilder src = new StringBuilder("int hashValue = 0; ");
        List<FieldVarLogger> initGlobals = this.getClazzLogger().getVariablesWithPredicate(v -> v.isInitialized());
        if (this.getClazzLogger().hasVariables()) {
            for (FieldVarLogger field : initGlobals) {
                if (field.getType() != FieldVarType.STRING) {
                    src.append("hashValue += (int)" + field.getName() + ";");
                } else {
                    src.append("if(" + field.getName() + " != null) {");
                    src.append("hashValue += " + field.getName() + ".hashCode();}");
                }
            }
        }
        try {
            CtMethod run = fieldVar_generator.getCtMethod(this.getClazzLogger().getRun());
            run.insertAfter(src.toString() +
                    " System.out.println(\"#############   GLOBAL HASH: \" + hashValue + \"  #############\");");
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }
}

//TODO function calls maybe also with compatible types
//TODO probability value borders
//TODO user option for filename and location
//TODO only use initialized Local Vars if in CONTROL_CONTEXT????(Bad Local Variable Bug)
//TODO math and logical operators
//TODO tests
//TODO random probabilities in tests
