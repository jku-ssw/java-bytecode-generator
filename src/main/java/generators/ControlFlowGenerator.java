package generators;

import javassist.CannotCompileException;
import javassist.CtMethod;
import logger.MethodLogger;

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
    private RandomCodeGenerator randomCodeGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
        ifBranchingFactor = randomCodeGenerator.controller.getIfBranchingFactor();
    }

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

    private void openElseStatement() {
        controlSrc.append("} else {");
        openIfContexts.getLast().hasElse = true;
    }

    private void openElseIfStatement(MethodLogger contextMethod) {
        openIfContexts.getLast().numberOfElseIf++;
        controlSrc.append("} else if(" + getRandomCondition(contextMethod) + ") {");
    }

    //TODO condition
    private void openIfStatement(MethodLogger contextMethod) {
        controlSrc.append("if(" + getRandomCondition(contextMethod) + ") {");
        deepness++;
        IfContext c = new IfContext(deepness);
        openIfContexts.add(c);
    }


    private void closeIStatement() {
        controlSrc.append("}");
        openIfContexts.removeLast();
        deepness--;
    }

    private void generateBody(MethodLogger contextMethod, ControlType controlType) {
        RandomCodeGenerator.Context.controlContext.setContextMethod(contextMethod);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.controlContext);
        if (controlType == ControlType.ifType) {
            this.closeIStatement();
        } else if (controlType == ControlType.forWhileType) {
            this.closeWhileStatement();
        } else if (controlType == ControlType.doWhileType) {
            this.closeDoWhileStatement(contextMethod);
        }
        System.out.println(deepness);
        if (this.getDeepness() == 0) this.insertControlSrcIntoMethod(contextMethod);
    }

    //TODO conditions
    private String getRandomCondition(MethodLogger method) {
        //this.getClazzLogger();
        return "true";
    }


    private boolean insertControlSrcIntoMethod(MethodLogger method) {
        //for (int i = 0; i < deepness; i++) controlSrc.append("}");
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            System.out.println(controlSrc);
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

    public void generateRandomDoWhileStatement(MethodLogger contextMethod) {
        this.openDoWhileStatement(contextMethod);
        this.generateBody(contextMethod, ControlType.doWhileType);
    }

    private void openDoWhileStatement(MethodLogger method) {
        controlSrc.append("do {");
        deepness++;
    }

    //TODO condition
    private void closeDoWhileStatement(MethodLogger method) {
        controlSrc.append("} while(false);");
        deepness--;
    }

    public void generateRandomWhileStatement(MethodLogger contextMethod) {
        this.openWhileStatement(contextMethod);
        this.generateBody(contextMethod, ControlType.forWhileType);
    }

    //TODO condition
    private void openWhileStatement(MethodLogger method) {
        controlSrc.append("while(false) {");
        ++deepness;
    }

    private void closeWhileStatement() {
        controlSrc.append("}");
        deepness--;
    }

    public void generateRandomForStatement(MethodLogger contextMethod) {
        this.openForStatement(contextMethod);
        this.generateBody(contextMethod, ControlType.forWhileType);
    }

    private void openForStatement(MethodLogger method) {
        controlSrc.append("for(int i = 0; i < 10; i++) {");
        deepness++;
    }

    //TODO random conditions
    //TODO swtich
    //TODO use break, continue statements
}




