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

import generators.MathGenerator.OpStatKind;

import static generators.MathGenerator.OpStatKind.*;

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

    private static final Random RANDOM = new Random();

    private final GenerationController controller;

    //Generators
    private final FieldVarGenerator fieldVarGenerator;
    private final MethodGenerator methodGenerator;
    private final MathGenerator mathGenerator;
    private final ControlFlowGenerator controlFlowGenerator;

    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.controller = controller;
        ClazzFileContainer container = new ClazzFileContainer(fileName);
        this.fieldVarGenerator = new FieldVarGenerator(container);
        this.methodGenerator = new MethodGenerator(this);
        this.mathGenerator = new MathGenerator(container, controller.avoidOverflows(), controller.avoidDivByZero());
        this.controlFlowGenerator = new ControlFlowGenerator(this, mathGenerator);

        MethodLogger run = this.methodGenerator.generateAndCallRunMethod();
        Context.PROGRAM_CONTEXT.lengthWeighting = controller.getProgramLengthWeighting();
        Context.PROGRAM_CONTEXT.contextMethod = run;
        Context.METHOD_CONTEXT.lengthWeighting = controller.getMethodLengthWeighting();
        Context.CONTROL_CONTEXT.lengthWeighting = controller.getControlLengthWeighting();
    }

    public GenerationController getController() {
        return controller;
    }

    public ClazzFileContainer getClazzFileContainer() {
        return fieldVarGenerator.getClazzContainer();
    }


    private ClazzLogger getClazzLogger() {
        return fieldVarGenerator.getClazzLogger();
    }

    public void generate() {
        //generate code in run()-method
        generate(Context.PROGRAM_CONTEXT);

        //generate method-bodies
        if (this.getClazzLogger().hasMethods()) {
            for (MethodLogger method : this.getClazzLogger().getMethods()) {
                methodGenerator.generateMethodBody(method);
            }
        }

        //compute HashValue of all globals
        computeHash();
    }

    void generate(Context context) {
        for (int i = 0; i < context.lengthWeighting; i++) {
            int r = 1 + RANDOM.nextInt(100);

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getFieldProbability()) {
                fieldVarGenerator.generateField();
            }

            if (r <= controller.getLocalVariableProbability() && context != Context.CONTROL_CONTEXT) {
                fieldVarGenerator.generateLocalVariable(context.contextMethod);
            }

            if (r <= controller.getGlobalAssignProbability()) {
                String src = null;
                int assignKind = RANDOM.nextInt(4);
                switch (assignKind) {
                    case 0: //set field to RANDOM value
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcSetFieldValue(context.contextMethod);
                        } else {
                            fieldVarGenerator.setFieldValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign field to field
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignFieldToField(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignFieldToField(context.contextMethod);
                        }
                        break;
                    case 2: // assign local var to field
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignLocalVarToField(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignLocalVarToField(context.contextMethod);
                        }
                        break;
                    case 3:

                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getLocalAssignProbability() && context != Context.CONTROL_CONTEXT) {
                int assignKind = RANDOM.nextInt(3);
                String src = null;
                switch (assignKind) {
                    case 0: //set local variable to RANDOM value
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcSetLocalVarValue(context.contextMethod);
                        } else {
                            fieldVarGenerator.setLocalVarValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign local variable to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignLocalVarToLocalVar(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignLocalVarToLocalVar(context.contextMethod);
                        }
                        break;
                    case 2: // assign field to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignFieldToLocalVar(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignFieldToLocalVar(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getMethodProbability()) {
                methodGenerator.generateMethod(controller.getMaximumMethodParameters());
            }

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getMethodOverloadProbability()) {
                methodGenerator.overloadMethod(controller.getMaximumMethodParameters());
            }

            if (r <= controller.getMethodCallProbability()) {
                int callKind = RANDOM.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == Context.CONTROL_CONTEXT) {
                            src = methodGenerator.srcGenerateMethodCall(context.contextMethod);
                        } else {
                            methodGenerator.generateMethodCall(context.contextMethod);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == Context.CONTROL_CONTEXT)
                            src = methodGenerator.srcSetFieldToReturnValue(context.contextMethod);
                        else {
                            methodGenerator.setFieldToReturnValue(context.contextMethod);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = methodGenerator.srcSetLocalVarToReturnValue(context.contextMethod);
                        } else {
                            methodGenerator.setLocalVarToReturnValue(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }
            if (r <= controller.getJavaLangMathProbability()) {
                int callKind = RANDOM.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == Context.CONTROL_CONTEXT) {
                            src = mathGenerator.srcGenerateMathMethodCall(context.contextMethod);
                        } else {
                            mathGenerator.generateMathMethodCall(context.contextMethod);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == Context.CONTROL_CONTEXT)
                            src = mathGenerator.srcSetFieldToMathReturnValue(context.contextMethod);
                        else {
                            mathGenerator.setFieldToMathReturnValue(context.contextMethod);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == Context.CONTROL_CONTEXT) {
                            src = mathGenerator.srcSetLocalVarToMathReturnValue(context.contextMethod);
                        } else {
                            mathGenerator.setLocalVarToMathReturnValue(context.contextMethod);
                        }
                        break;
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getPrintProbability()) {
                String src = null;
                if (context == Context.CONTROL_CONTEXT) {
                    src = fieldVarGenerator.srcGeneratePrintStatement(context.contextMethod);
                } else {
                    fieldVarGenerator.generatePrintStatement(context.contextMethod);
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getControlFlowProbability() && controlFlowGenerator.getDeepness() < controller.getControlFlowDeepness()) {
                int controlKind = RANDOM.nextInt(4);
                boolean noStatementGenerated = true;
                int ctrlTypeProb = 1 + RANDOM.nextInt(100);
                for (int j = 0; j < 4 && noStatementGenerated; j++) {
                    switch (controlKind) {
                        case 0:
                            if (ctrlTypeProb <= controller.getIfProbability()) {
                                controlFlowGenerator.generateIfElseStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 1;
                            }
                            break;
                        case 1:
                            if (ctrlTypeProb < controller.getWhileProbability()) {
                                controlFlowGenerator.generateWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 2;
                            }
                            break;
                        case 2:
                            if (ctrlTypeProb < controller.getDoWhileProbability()) {
                                controlFlowGenerator.generateDoWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 3;
                            }
                            break;
                        case 3:
                            if (ctrlTypeProb < controller.getForProbability()) {
                                controlFlowGenerator.generateForStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 0;
                            }
                            break;
                    }
                }
            }


            if (r < controller.getOperatorStatementProbability()) {
                int globalOrLocalOrNotAssign;
                if (r < controller.getLocalAssignProbability() && r < controller.getGlobalAssignProbability()) {
                    globalOrLocalOrNotAssign = RANDOM.nextInt(3);
                } else if (r < controller.getGlobalAssignProbability()) {
                    globalOrLocalOrNotAssign = 0;
                } else if (r < controller.getLocalAssignProbability()) {
                    globalOrLocalOrNotAssign = 1;
                } else {
                    globalOrLocalOrNotAssign = 2;
                }

                OpStatKind opStatKind = getOpStatKind();

                if (opStatKind == null) {
                    break;
                }

                int maxOperations = controller.getMaxOperatorsInOperatorStatement();
                String src = null;
                switch (globalOrLocalOrNotAssign) {
                    case 0:
                        if (context == Context.CONTROL_CONTEXT) {
                            src = mathGenerator.srcSetFieldToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.setFieldToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                        break;
                    case 1:
                        if (context == Context.CONTROL_CONTEXT) {
                            src = mathGenerator.srcSetLocalVarToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.setLocalVarToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                        break;
                    case 2:
                        if (context == Context.CONTROL_CONTEXT) {
                            src = mathGenerator.srcGenerateOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.generateOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }
        }
    }

    private OpStatKind getOpStatKind() {
        int opProb = 1 + RANDOM.nextInt(100);
        OpStatKind selectedKind = OpStatKind.values()[RANDOM.nextInt(OpStatKind.values().length)];
        for (int i = 0; i < OpStatKind.values().length; i++) {
            switch (selectedKind) {
                case ARITHMETIC:
                    if (opProb < controller.getArithmeticStatementProbability()) {
                        return ARITHMETIC;
                    } else {
                        selectedKind = LOGICAL;
                        break;
                    }
                case LOGICAL:
                    if (opProb < controller.getLogicalStatementProbability()) {
                        return LOGICAL;
                    } else {
                        selectedKind = BITWISE;
                        break;
                    }
                case BITWISE:
                    if (opProb < controller.getBitwiseStatementProbability()) {
                        return BITWISE;
                    } else {
                        selectedKind = ARITHMETIC_LOGICAL;
                        break;
                    }
                case ARITHMETIC_LOGICAL:
                    if (opProb < controller.getArithmeticLogicalStatementProbability()) {
                        return ARITHMETIC_LOGICAL;
                    } else {
                        selectedKind = ARITHMETIC_BITWISE;
                        break;
                    }
                case ARITHMETIC_BITWISE:
                    if (opProb < controller.getArithmeticBitwiseStatementProbability()) {
                        return ARITHMETIC_BITWISE;
                    } else {
                        selectedKind = BITWISE_LOGICAL;
                        break;
                    }
                case BITWISE_LOGICAL:
                    if (opProb < controller.getLogicBitwiseStatementProbability()) {
                        return BITWISE_LOGICAL;
                    } else {
                        selectedKind = ARITHMETIC_LOGICAL_BITWISE;
                        break;
                    }
                case ARITHMETIC_LOGICAL_BITWISE:
                    if (opProb < controller.getArithmeticLogicalBitwiseStatementProbability()) {
                        return ARITHMETIC_LOGICAL_BITWISE;
                    } else {
                        selectedKind = ARITHMETIC;
                    }
            }
        }
        return null;
    }


    public void writeFile() {
        fieldVarGenerator.writeFile();
    }

    public void writeFile(String directoryName) {
        fieldVarGenerator.writeFile(directoryName);
    }

    private void computeHash() {
        StringBuilder src = new StringBuilder("long hashValue = 0; ");
        List<FieldVarLogger> initGlobals = this.getClazzLogger().getVariablesWithPredicate(v -> v.isInitialized());
        if (this.getClazzLogger().hasVariables()) {
            for (FieldVarLogger field : initGlobals) {
                if (field.getType() == FieldVarType.STRING) {
                    src.append("if(" + field.getName() + " != null) {");
                    src.append("hashValue += " + field.getName() + ".hashCode();}");
                } else if (field.getType() == FieldVarType.BOOLEAN) {
                    src.append("hashValue += " + field.getName() + "? 1 : 0;");
                } else {
                    src.append("hashValue += (long)" + field.getName() + ";");
                }
            }
        }
        try {
            CtMethod run = fieldVarGenerator.getCtMethod(this.getClazzLogger().getRun());
            run.insertAfter(src.toString() +
                    " System.out.println(\"#############   GLOBAL HASH: \" + hashValue + \"  #############\");");
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }
}

