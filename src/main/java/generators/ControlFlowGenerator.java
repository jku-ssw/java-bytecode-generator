package generators;

import javassist.CannotCompileException;
import javassist.CtMethod;
import logger.MethodLogger;
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
    private StringBuilder controlSrc = new StringBuilder();
    private int deepness = 0;
    int ifBranchingFactor;
    int maxLoopIterations;
    private RandomCodeGenerator randomCodeGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
        this.ifBranchingFactor = randomCodeGenerator.controller.getIfBranchingFactor();
        this.maxLoopIterations = randomCodeGenerator.controller.getMaxLoopIterations();
    }

    //==========================================IF ELSEIF ELSE==========================================================

    public void generateRandomIfElseStatement(MethodLogger contextMethod) {
        if (openIfContexts.size() != 0 && deepness == openIfContexts.getLast().deepness) {
            switch (random.nextInt(3)) {
                case 0:
                    //TODO userdefined numberOfElseIf
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

    //TODO condition
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

    //TODO condition
    private void openElseIfStatement(MethodLogger contextMethod) {
        openIfContexts.getLast().numberOfElseIf++;
        controlSrc.append("} else if(" + getIfCondition(contextMethod) + ") {");
    }

    private void closeIStatement() {
        controlSrc.append("}");
        openIfContexts.removeLast();
        deepness--;
    }

    //TODO condition
    private String getIfCondition(MethodLogger method) {
        //this.getClazzLogger();
        return "true";
    }

    //=================================================DO WHILE=========================================================

    public void generateRandomDoWhileStatement(MethodLogger contextMethod) {
        this.openDoWhileStatement();
        this.generateBody(contextMethod, ControlType.doWhileType);
    }

    private void openDoWhileStatement() {
        controlSrc.append("do {");
        deepness++;
    }

    //TODO condition
    private void closeDoWhileStatement(MethodLogger method) {
        controlSrc.append("} while(false);");
        deepness--;
    }

    //================================================FOR / WHILE=====================================================

    public void generateRandomWhileStatement(MethodLogger contextMethod) {
        this.openWhileStatement(contextMethod);
        this.generateBody(contextMethod, ControlType.forWhileType);
    }

    //TODO condition
    private void openWhileStatement(MethodLogger method) {
        controlSrc.append("while(false) {");
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

    //TODO randomize iterations, userinput maximum iterations
    private void openForStatement() {
        RandomSupplier supplier = this.getClazzContainer().getRandomSupplier();
        String varname = supplier.getVarName();
        controlSrc.append("for(int " + varname + " = 0; " + varname + " < 10; " + varname + "++) {");
        deepness++;
    }

    //==================================================COMMON==========================================================


    private void generateBody(MethodLogger contextMethod, ControlType controlType) {
        RandomCodeGenerator.Context.controlContext.setContextMethod(contextMethod);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.controlContext);
        if (controlType == ControlType.ifType) {
            this.closeIStatement();
        } else if (controlType == ControlType.forWhileType) {
            this.closeForWhileStatement();
        } else if (controlType == ControlType.doWhileType) {
            this.closeDoWhileStatement(contextMethod);
        }
        if (this.getDeepness() == 0) this.insertControlSrcIntoMethod(contextMethod);
    }

    private boolean insertControlSrcIntoMethod(MethodLogger method) {
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.insertAfter(controlSrc.toString());
            controlSrc = new StringBuilder();
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
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

    //TODO random conditions
}



