package generators;

import javassist.CannotCompileException;
import javassist.CtMethod;
import utils.logger.MethodLogger;

public class ControlFlowGenerator extends Generator {
    private StringBuilder controlSrc = new StringBuilder();

    private int deepness = 0;
    private RandomCodeGenerator randomCodeGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
    }

    public void generateRandomIfStatement(MethodLogger contextMethod) {
        this.openIfStatement(contextMethod);
        RandomCodeGenerator.Context.controlContext.setContextMethod(contextMethod);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.controlContext);
        this.closeStatement();
        if (this.getDeepness() == 0) {
            this.insertControlSrcIntoMethod(contextMethod);
        }
    }

    public void openIfStatement(MethodLogger method) {
        controlSrc.append("if(" + getRandomCondition(method) + ") {");
        ++deepness;
    }

    //TODO condition
    private String getRandomCondition(MethodLogger method) {
        //this.getClazzLogger();
        return "true";
    }

    public void closeStatement() {
        controlSrc.append("}");
        deepness--;
    }

    public boolean insertControlSrcIntoMethod(MethodLogger method) {
        if (deepness != 0) return false;
        for (int i = 0; i < deepness; i++) controlSrc.append("}");
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
}


