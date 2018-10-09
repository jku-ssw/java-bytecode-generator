package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import javassist.CannotCompileException;
import javassist.CtMethod;

import java.util.Stack;

import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind;
import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statements.Break;

class ControlFlowGenerator extends Generator {
    private class IfContext {
        int numberOfElseIf;
        boolean hasElse;
        int depth;

        public IfContext(int depth) {
            this.depth = depth;
            hasElse = false;
            numberOfElseIf = 0;
        }
    }

    private enum ControlType {
        ifType,
        elseType,
        forWhileType,
        doWhileType
    }

    private final Stack<IfContext> openIfContexts = new Stack<>();
    private final StringBuilder controlSrc = new StringBuilder();
    private int loopDepth = 0;
    private int ifDepth = 0;
    private final int ifBranchingFactor;
    private final int maxLoopIterations;
    private final RandomCodeGenerator randomCodeGenerator;
    private final MathGenerator mathGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator, MathGenerator mathGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
        this.ifBranchingFactor = randomCodeGenerator.getController().getIfBranchingFactor();
        this.maxLoopIterations = randomCodeGenerator.getController().getMaxLoopIterations();
        this.mathGenerator = mathGenerator;
    }

    //==========================================IF ELSEIF ELSE==========================================================

    public void generateIfElseStatement(MethodLogger method) {
        if (openIfContexts.size() != 0 && ifDepth == openIfContexts.peek().depth) {
            switch (RANDOM.nextInt(5)) {
                case 0:
                    if (!openIfContexts.peek().hasElse) {
                        openElseStatement();
                        this.generateBody(method, ControlType.elseType);
                    }
                    break;
                case 1:
                    openIfStatement(method);
                    this.generateBody(method, ControlType.ifType);
                    break;
                default:
                    if (!openIfContexts.peek().hasElse && openIfContexts.peek().numberOfElseIf < ifBranchingFactor) {
                        openElseIfStatement(method);
                        this.generateBody(method, ControlType.elseType);
                    }
            }
        } else {
            this.openIfStatement(method);
            this.generateBody(method, ControlType.ifType);
        }
    }

    private void openIfStatement(MethodLogger method) {
        controlSrc.append("if(").append(getIfCondition(method)).append(") {");
        ifDepth++;
        IfContext c = new IfContext(ifDepth);
        openIfContexts.add(c);
    }

    private void openElseStatement() {
        controlSrc.append("} else {");
        openIfContexts.peek().hasElse = true;
    }

    private void openElseIfStatement(MethodLogger method) {
        openIfContexts.peek().numberOfElseIf++;
        controlSrc.append("} else if(").append(getIfCondition(method)).append(") {");
    }

    private void closeIFStatement() {
        controlSrc.append("}");
        openIfContexts.pop();
        ifDepth--;
    }

    private String getIfCondition(MethodLogger method) {
        OpStatKind condKind = null;
        switch (RANDOM.nextInt(4)) {
            case 0:
                condKind = LOGICAL;
                break;
            case 1:
                condKind = ARITHMETIC_LOGICAL;
                break;
            case 2:
                condKind = BITWISE_LOGICAL;
                break;
            case 3:
                condKind = ARITHMETIC_LOGICAL_BITWISE;
                break;
        }
        String src;
        if (condKind == ARITHMETIC_LOGICAL || condKind == ARITHMETIC_LOGICAL_BITWISE) {
            src = mathGenerator.srcGenerateOperatorStatement(
                    method, randomCodeGenerator.getController().getMaxOperators(), condKind, true);
        } else {
            src = mathGenerator.srcGenerateOperatorStatement(
                    method, randomCodeGenerator.getController().getMaxOperators(), condKind, false);
        }
        StringBuilder condition = new StringBuilder(src);
        condition.deleteCharAt(condition.length() - 1);
        return condition.toString();
    }

    //=================================================DO WHILE=========================================================

    public void generateDoWhileStatement(MethodLogger method) {
        String condition = this.openDoWhileStatement();
        this.generateBody(method, ControlType.doWhileType, condition);
    }

    private String openDoWhileStatement() {
        String varName = this.getClazzContainer().getRandomSupplier().getVarName();
        loopDepth++;
        if (RANDOM.nextBoolean()) {
            controlSrc.append("int ").append(varName).append(" = 0; do { ").append(varName).append("++;");
            return varName + " < " + getNumberOfLoopIterations(maxLoopIterations);
        } else {
            controlSrc.append("int ").append(varName).append(" = ").append(getNumberOfLoopIterations(maxLoopIterations)).append("; do { ").append(varName).append("--;");
            return varName + " > 0";
        }
    }

    private void closeDoWhileStatement(String condition) {
        controlSrc.append("} while(").append(condition).append(");");
        loopDepth--;
    }

    //==================================================FOR/WHILE=======================================================

    public void generateWhileStatement(MethodLogger method) {
        this.openWhileStatement();
        this.generateBody(method, ControlType.forWhileType);
    }

    private void openWhileStatement() {
        String varName = this.getClazzContainer().getRandomSupplier().getVarName();
        Randomizer.oneOf(
                () -> controlSrc.append("int ").append(varName).append(" = 0; while(").append(varName).append(" < ").append(getNumberOfLoopIterations(maxLoopIterations)).append(") { ").append(varName).append("++; "),
                () -> controlSrc.append("int ").append(varName).append(" = ").append(getNumberOfLoopIterations(maxLoopIterations)).append("; while(").append(varName).append(" > 0) { ").append(varName).append("--; ")
        );
        ++loopDepth;
    }

    private void closeForWhileStatement() {
        controlSrc.append("}");
        loopDepth--;
    }

    public void generateForStatement(MethodLogger method) {
        this.openForStatement();
        this.generateBody(method, ControlType.forWhileType);
    }

    private void openForStatement() {
        RandomSupplier supplier = this.getClazzContainer().getRandomSupplier();
        String varName = supplier.getVarName();
        int it = getNumberOfLoopIterations(maxLoopIterations);
        controlSrc.append("for(int ").append(varName).append(" = 0; ").append(varName).append(" < ").append(it).append("; ").append(varName).append("++) {");
        loopDepth++;
    }

    public void insertBreak() {
        // only generate break statements when inside some loop
        if (loopDepth > 0)
            controlSrc.append(Break);
    }

    //==================================================COMMON==========================================================

    private void generateBody(MethodLogger method, ControlType controlType, String... condition) {
        RandomCodeGenerator.Context.CONTROL_CONTEXT.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.CONTROL_CONTEXT);
        if (controlType == ControlType.ifType) {
            this.closeIFStatement();
        } else if (controlType == ControlType.forWhileType) {
            this.closeForWhileStatement();
        } else if (controlType == ControlType.doWhileType) {
            this.closeDoWhileStatement(condition[0]);
        }
        if (loopDepth == 0 && ifDepth == 0) {
            this.insertControlSrcIntoMethod(method);
        }
    }

    private void insertControlSrcIntoMethod(MethodLogger method) {
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.insertAfter(controlSrc.toString());
            controlSrc.setLength(0);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void addCodeToControlSrc(String code) {
        if (loopDepth > 0 || ifDepth > 0) {
            controlSrc.append(code);
        } else {
            System.err.println("Cannot insert code, no open control-flow-block");
        }
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public int getIfDepth() {
        return ifDepth;
    }

    private int getNumberOfLoopIterations(int maxLoopIterations) {
        return maxLoopIterations == 0 ? 0 : RANDOM.nextInt(maxLoopIterations);
    }
}



