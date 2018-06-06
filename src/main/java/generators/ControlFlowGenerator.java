package generators;

import javassist.CannotCompileException;
import javassist.CtMethod;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.FieldVarType;
import utils.RandomSupplier;

import java.util.LinkedList;

public class ControlFlowGenerator extends Generator {
    private class IfContext {
        int numberOfElseIf;
        boolean hasElse;
        int deepness;

        public IfContext(int deepness) {
            this.deepness = deepness;
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

    LinkedList<IfContext> openIfContexts = new LinkedList<>();
    private final StringBuilder controlSrc = new StringBuilder();
    private int deepness = 0;
    private final int ifBranchingFactor;
    private final int maxLoopIterations;
    private final RandomCodeGenerator randomCodeGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
        this.ifBranchingFactor = randomCodeGenerator.getController().getIfBranchingFactor();
        this.maxLoopIterations = randomCodeGenerator.getController().getMaxLoopIterations();
    }

    //==========================================IF ELSEIF ELSE==========================================================

    public void generateRandomIfElseStatement(MethodLogger contextMethod) {
        if (openIfContexts.size() != 0 && deepness == openIfContexts.getLast().deepness) {
            switch (random.nextInt(3)) {
                case 0:
                    if (openIfContexts.getLast().hasElse == false &&
                            openIfContexts.getLast().numberOfElseIf < ifBranchingFactor) {
                        openElseIfStatement(contextMethod);
                        this.generateBody(contextMethod, ControlType.elseType);
                    }
                    break;
                case 1:
                    if (openIfContexts.getLast().hasElse == false) {
                        openElseStatement();
                        this.generateBody(contextMethod, ControlType.elseType);
                    }
                    break;
                case 2:
                    openIfStatement(contextMethod);
                    this.generateBody(contextMethod, ControlType.ifType);
            }
        } else {
            this.openIfStatement(contextMethod);
            this.generateBody(contextMethod, ControlType.ifType);
        }
    }

    private void openIfStatement(MethodLogger contextMethod) {
        controlSrc.append("if(" + getIfCondition(contextMethod) + ") {");
        deepness++;
        IfContext c = new IfContext(deepness);
        openIfContexts.add(c);
    }

    private void openElseStatement() {
        controlSrc.append("} else {");
        openIfContexts.getLast().hasElse = true;
    }

    private void openElseIfStatement(MethodLogger contextMethod) {
        openIfContexts.getLast().numberOfElseIf++;
        controlSrc.append("} else if(" + getIfCondition(contextMethod) + " ) {");
    }

    private void closeIStatement() {
        controlSrc.append("}");
        openIfContexts.removeLast();
        deepness--;
    }

    private String getIfCondition(MethodLogger method) {
        StringBuilder condition = new StringBuilder();
        FieldVarType type = RandomSupplier.getFieldVarType();
        FieldVarLogger op1 = this.getClazzLogger().getGlobalOrLocalVarOfTypeUsableInMethod(method, type);
        FieldVarLogger op2 = this.getClazzLogger().getGlobalOrLocalVarOfTypeUsableInMethod(method, type);
        if (type != FieldVarType.STRING) {
            addOperandToCondition(op1, type, condition);
            String eqRelOp = getRandomRelOperator();
            condition.append(eqRelOp);
            addOperandToCondition(op2, type, condition);
        } else {
            if (op1 != null) {
                condition.append(op1.getName() + " != null && ");
            }
            if (random.nextBoolean()) condition.append("!");
            addOperandToCondition(op1, type, condition);
            condition.append(".equals(");
            addOperandToCondition(op1, type, condition);
            condition.append(")");
        }
        return condition.toString();
    }

    private static void addOperandToCondition(FieldVarLogger operand, FieldVarType type, StringBuilder condition) {
        if (operand == null) {
            condition.append(RandomSupplier.getRandomValueNotNull(type));
        } else {
            condition.append(operand.getName());
        }
    }

    //=================================================DO WHILE=========================================================

    public void generateRandomDoWhileStatement(MethodLogger contextMethod) {
        String condition = this.openDoWhileStatement();
        this.generateBody(contextMethod, ControlType.doWhileType, condition);
    }

    private String openDoWhileStatement() {
        String varName = this.getClazzContainer().getRandomSupplier().getVarName();
        deepness++;
        if (random.nextBoolean()) {
            controlSrc.append("int " + varName + " = 0; do { " + varName + "++;");
            return varName + " < " + random.nextInt(maxLoopIterations);
        } else {
            controlSrc.append("int " + varName + " = " + random.nextInt(maxLoopIterations) + "; do { " + varName + "--;");
            return varName + " > 0";
        }
    }

    //TODO condition
    private void closeDoWhileStatement(String condition) {
        controlSrc.append("} while(" + condition + ");");
        deepness--;
    }

    //==================================================FOR/WHILE=======================================================

    public void generateRandomWhileStatement(MethodLogger contextMethod) {
        this.openWhileStatement();
        this.generateBody(contextMethod, ControlType.forWhileType);
    }

    private void openWhileStatement() {
        String varName = this.getClazzContainer().getRandomSupplier().getVarName();
        if (random.nextBoolean()) {
            controlSrc.append("int " + varName + " = 0; while(" +
                    varName + " < " + random.nextInt(maxLoopIterations) + ") { " + varName + "++; ");
        } else {
            controlSrc.append("int " + varName + " = " + random.nextInt(maxLoopIterations) + "; while(" +
                    varName + " > 0) { " + varName + "--; ");
        }
        ++deepness;
    }

    private void closeForWhileStatement() {
        controlSrc.append("}");
        deepness--;
    }

    public void generateRandomForStatement(MethodLogger contextMethod) {
        this.openForStatement();
        this.generateBody(contextMethod, ControlType.forWhileType);
    }

    private void openForStatement() {
        RandomSupplier supplier = this.getClazzContainer().getRandomSupplier();
        String varName = supplier.getVarName();
        int it = random.nextInt(this.maxLoopIterations + 1);
        controlSrc.append("for(int " + varName + " = 0; " + varName + " < " + it + "; " + varName + "++) {");
        deepness++;
    }

    //==================================================COMMON==========================================================


    private void generateBody(MethodLogger contextMethod, ControlType controlType, String... condition) {
        RandomCodeGenerator.Context.CONTROL_CONTEXT.setContextMethod(contextMethod);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.CONTROL_CONTEXT);
        if (controlType == ControlType.ifType) {
            this.closeIStatement();
        } else if (controlType == ControlType.forWhileType) {
            this.closeForWhileStatement();
        } else if (controlType == ControlType.doWhileType) {
            this.closeDoWhileStatement(condition[0]);
        }
        if (this.getDeepness() == 0) {
            this.insertControlSrcIntoMethod(contextMethod);
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
        if (deepness > 0) {
            controlSrc.append(code);
        } else {
            System.err.println("Cannot insert code, no open control-flow-block");
        }
    }

    public int getDeepness() {
        return deepness;
    }

    private String getRandomRelOperator() {
        String[] relOps = {" == ", " != ", " > ", " >= ", " < ", " <= "};
        return relOps[random.nextInt(relOps.length)];
    }

    //TODO ev. Condition-concatination with conditional Operators) => max number of conditions => user!!
}



