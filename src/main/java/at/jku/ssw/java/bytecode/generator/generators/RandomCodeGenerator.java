package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator.Context.CONTROL_CONTEXT;
import static at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator.Context.METHOD_CONTEXT;
import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind;
import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind.*;


public class RandomCodeGenerator {

    private static final Logger logger = LogManager.getLogger();

    enum Context {
        PROGRAM_CONTEXT,
        METHOD_CONTEXT,
        CONTROL_CONTEXT;

        private int lengthWeighting;
        private MethodLogger<?> contextMethod;

        public void setContextMethod(MethodLogger<?> contextMethod) {
            this.contextMethod = contextMethod;
        }
    }

    private final GenerationController controller;

    //Generators
    private final FieldVarGenerator fieldVarGenerator;
    private final MethodGenerator methodGenerator;
    private final MathGenerator mathGenerator;
    private final SnippetGenerator snippetGenerator;
    private final TypeCastGenerator typeCastGenerator;
    private final ControlFlowGenerator controlFlowGenerator;
    private final ArrayAccessGenerator arrayAccessGenerator;
    private final int maxOpProbability;

    /**
     * The central random instance that is used throughout the generation
     * process.
     */
    private final Random rand;

    /**
     * The seed that the random generator was initialized with.
     */
    private final int seed;

    public RandomCodeGenerator(String fileName, GenerationController controller) {
        this.controller = controller;

        this.seed = controller.getSeedValue();

        // reset the types
        TypeCache.CACHE.reset();

        logger.info("Generating class {}", fileName);
        logger.info("SEED: {}", seed);

        this.rand = new Random(seed);

        ClazzFileContainer container = new ClazzFileContainer(rand, controller, fileName);
        maxOpProbability = Collections.max(Arrays.asList(controller.getBitwiseProbability(),
                controller.getArithmeticBitwiseProbability(),
                controller.getArithmeticLogicalBitwiseProbability(),
                controller.getArithmeticLogicalProbability(),
                controller.getArithmeticProbability()));
        this.fieldVarGenerator = new FieldVarGenerator(rand, container);

        this.methodGenerator = new MethodGenerator(rand, this);
        this.mathGenerator = new MathGenerator(rand, container, controller.avoidOverflows(), controller.avoidDivByZero());
        this.snippetGenerator = new SnippetGenerator(rand, this);
        this.typeCastGenerator = new TypeCastGenerator(rand, this);
        this.controlFlowGenerator = new ControlFlowGenerator(rand, this, mathGenerator);
        this.arrayAccessGenerator = new ArrayAccessGenerator(rand, container);

        this.methodGenerator.generateRunMethod();
        Context.PROGRAM_CONTEXT.lengthWeighting = controller.getProgramLengthWeighting();
        Context.PROGRAM_CONTEXT.contextMethod = getClazzLogger().run();
        METHOD_CONTEXT.lengthWeighting = controller.getMethodLengthWeighting();
        CONTROL_CONTEXT.lengthWeighting = controller.getControlLengthWeighting();
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
        try {
            // generate code in run()-method
            generate(Context.PROGRAM_CONTEXT);
            // generate method bodies for pre-registerd methods
            getClazzLogger().methods()
                    .forEach(methodGenerator::generateMethodBody);
            // compute HashValue of all globals
            this.methodGenerator.generateHashMethod();
            this.methodGenerator.callRunAndHashMethods(controller.executeRunXTimes());
        } catch (CompilationFailedException e) {
            logger.fatal("Could not finish generation of class {} due to compilation errors", getClazzFileContainer().getFileName());
            logger.fatal("The seed that was used to initialize the random generator was {}", seed);
            logger.fatal("-- CLASS -----------------------------------------------");
            logger.fatal(getClazzFileContainer());
            logger.fatal("--------------------------------------------------------");
            e.printStackTrace();
            throw e;
        }
    }

    void generate(Context context) {
        int l;
        if (context == CONTROL_CONTEXT || context == METHOD_CONTEXT) {
            l = rand.nextInt(context.lengthWeighting + 1);
        } else {
            l = context.lengthWeighting;
        }
        for (int i = 0; i < l; i++) {
            int r = 1 + rand.nextInt(100);

            if (context == Context.PROGRAM_CONTEXT && r <= controller.getFieldProbability()) {
                fieldVarGenerator.generateField();
            }

            if (r <= controller.getLocalVariableProbability() && context != CONTROL_CONTEXT) {
                fieldVarGenerator.generateLocalVariable(context.contextMethod);
            }

            if (r <= controller.getGlobalAssignProbability()) {
                String src = null;
                int assignKind = rand.nextInt(3);
                switch (assignKind) {
                    case 0: //set field to RANDOM value
                        if (context == CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcSetFieldValue(context.contextMethod);
                        } else {
                            fieldVarGenerator.setFieldValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign field to field
                        if (context == CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignFieldToField(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignFieldToField(context.contextMethod);
                        }
                        break;
                    case 2: // assign local var to field
                        if (context == CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignLocalVarToField(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignLocalVarToField(context.contextMethod);
                        }
                        break;

                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (context != CONTROL_CONTEXT && r <= controller.getArrayAccessProbability()) {
                new Randomizer(rand).oneOf(
                        () -> arrayAccessGenerator.srcGenerateArrayReadAccess(context.contextMethod),
                        () -> arrayAccessGenerator.srcGenerateArrayWriteAccess(context.contextMethod))
                        .ifPresent(src ->
                                arrayAccessGenerator.insertIntoMethodBody(context.contextMethod, src));//, Arrays.asList(ArrayIndexOutOfBoundsException.class, NullPointerException.class)));
            }

            if (r <= controller.getLocalAssignProbability() && context != CONTROL_CONTEXT) {
                int assignKind = rand.nextInt(3);
                String src = null;
                switch (assignKind) {
                    case 0: //set local variable to RANDOM value
                        if (context == CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcSetLocalVarValue(context.contextMethod);
                        } else {
                            fieldVarGenerator.setLocalVarValue(context.contextMethod);
                        }
                        break;
                    case 1: //assign local variable to local variable
                        if (context == CONTROL_CONTEXT) {
                            src = fieldVarGenerator.srcAssignLocalVarToLocalVar(context.contextMethod);
                        } else {
                            fieldVarGenerator.assignLocalVarToLocalVar(context.contextMethod);
                        }
                        break;
                    case 2: // assign field to local variable
                        if (context == CONTROL_CONTEXT) {
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
                int callKind = rand.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == CONTROL_CONTEXT) {
                            src = methodGenerator.srcGenerateMethodCall(context.contextMethod);
                        } else {
                            methodGenerator.generateMethodCall(context.contextMethod);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == CONTROL_CONTEXT && r <= controller.getGlobalAssignProbability())
                            src = methodGenerator.srcSetFieldToReturnValue(context.contextMethod);
                        else {
                            methodGenerator.setFieldToReturnValue(context.contextMethod);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == CONTROL_CONTEXT && r <= controller.getLocalAssignProbability()) {
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
                int callKind = rand.nextInt(3);
                String src = null;
                switch (callKind) {
                    case 0: //call method
                        if (context == CONTROL_CONTEXT) {
                            src = mathGenerator.srcGenerateMathMethodCall(context.contextMethod);
                        } else {
                            mathGenerator.generateMathMethodCall(context.contextMethod);
                        }
                        break;
                    case 1: //assign return value of called method to field
                        if (context == CONTROL_CONTEXT)
                            src = mathGenerator.srcSetFieldToMathReturnValue(context.contextMethod);
                        else {
                            mathGenerator.setFieldToMathReturnValue(context.contextMethod);
                        }
                        break;
                    case 2: //assign return value of called method to local variable
                        if (context == CONTROL_CONTEXT) {
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
                if (context == CONTROL_CONTEXT) {
                    src = fieldVarGenerator.srcGeneratePrintStatement(context.contextMethod);
                } else {
                    fieldVarGenerator.generatePrintStatement(context.contextMethod);
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getControlFlowProbability() && controlFlowGenerator.getDepth() < controller.getControlFlowDeepness()) {
                int controlKind = rand.nextInt(4);
                boolean noStatementGenerated = true;
                int ctrlTypeProb = 1 + rand.nextInt(100);
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
                            if (ctrlTypeProb <= controller.getWhileProbability()) {
                                controlFlowGenerator.generateWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 2;
                            }
                            break;
                        case 2:
                            if (ctrlTypeProb <= controller.getDoWhileProbability()) {
                                controlFlowGenerator.generateDoWhileStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 3;
                            }
                            break;
                        case 3:
                            if (ctrlTypeProb <= controller.getForProbability()) {
                                controlFlowGenerator.generateForStatement(context.contextMethod);
                                noStatementGenerated = false;
                            } else {
                                controlKind = 0;
                            }
                            break;
                    }
                }
            }


            if (r <= controller.getOperatorStatementProbability()) {
                int globalOrLocalOrNotAssign;
                if (r <= controller.getLocalAssignProbability() && r <= controller.getGlobalAssignProbability()) {
                    globalOrLocalOrNotAssign = rand.nextInt(3);
                } else if (r <= controller.getGlobalAssignProbability()) {
                    globalOrLocalOrNotAssign = 0;
                } else if (r <= controller.getLocalAssignProbability()) {
                    globalOrLocalOrNotAssign = 1;
                } else {
                    globalOrLocalOrNotAssign = 2;
                }

                OpStatKind opStatKind = getOpStatKind();

                if (opStatKind == null) {
                    continue;
                }

                int maxOperations = controller.getMaxOperators();
                String src = null;
                switch (globalOrLocalOrNotAssign) {
                    case 0:
                        if (context == CONTROL_CONTEXT) {
                            src = mathGenerator.srcSetFieldToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.setFieldToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                        break;
                    case 1:
                        if (context == CONTROL_CONTEXT) {
                            src = mathGenerator.srcSetLocalVarToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.setLocalVarToOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                        break;
                    case 2:
                        if (context == CONTROL_CONTEXT) {
                            src = mathGenerator.srcGenerateOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        } else {
                            mathGenerator.generateOperatorStatement(context.contextMethod, maxOperations, opStatKind);
                        }
                }
                if (src != null) {
                    controlFlowGenerator.addCodeToControlSrc(src);
                }
            }

            if (r <= controller.getSnippetProbability())
                snippetGenerator.generate(context.contextMethod);

            if (r <= controller.getTypeCastProbability())
                typeCastGenerator.generatePrimitiveTypeCast(context.contextMethod);

            if (r <= controller.getBreakProbability())
                controlFlowGenerator.insertBreak();

            if (r <= controller.getPreemptiveReturnProbability()) {
                methodGenerator.insertReturn(context.contextMethod);
            }
        }
    }

    private OpStatKind getOpStatKind() {
        int opProb = 1 + rand.nextInt(maxOpProbability);
        OpStatKind selectedKind = OpStatKind.values()[rand.nextInt(OpStatKind.values().length - 1)];
        for (int i = 0; i < OpStatKind.values().length; i++) {
            switch (selectedKind) {
                case ARITHMETIC:
                    if (opProb <= controller.getArithmeticProbability()) {
                        return ARITHMETIC;
                    } else {
                        selectedKind = LOGICAL;
                        break;
                    }
                case LOGICAL:
                    if (opProb <= controller.getLogicalProbability()) {
                        return LOGICAL;
                    } else {
                        selectedKind = BITWISE;
                        break;
                    }
                case BITWISE:
                    if (opProb <= controller.getBitwiseProbability()) {
                        return BITWISE;
                    } else {
                        selectedKind = ARITHMETIC_LOGICAL;
                        break;
                    }
                case ARITHMETIC_LOGICAL:
                    if (opProb <= controller.getArithmeticLogicalProbability()) {
                        return ARITHMETIC_LOGICAL;
                    } else {
                        selectedKind = ARITHMETIC_BITWISE;
                        break;
                    }
                case ARITHMETIC_BITWISE:
                    if (opProb <= controller.getArithmeticBitwiseProbability()) {
                        return ARITHMETIC_BITWISE;
                    } else {
                        selectedKind = BITWISE_LOGICAL;
                        break;
                    }
                case BITWISE_LOGICAL:
                    if (opProb <= controller.getLogicBitwiseProbability()) {
                        return BITWISE_LOGICAL;
                    } else {
                        selectedKind = ARITHMETIC_LOGICAL_BITWISE;
                        break;
                    }
                case ARITHMETIC_LOGICAL_BITWISE:
                    if (opProb <= controller.getArithmeticLogicalBitwiseProbability()) {
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

    public int getSeed() {
        return seed;
    }
}

