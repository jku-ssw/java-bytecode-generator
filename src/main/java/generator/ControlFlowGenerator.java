package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import utils.*;
import utils.logger.MethodLogger;

public class ControlFlowGenerator extends Generator {
    private StringBuilder controlSrc = new StringBuilder();

    private int deepness = 0;

    public ControlFlowGenerator(ClazzFileContainer cf) {
        super(cf);
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


    //TODO string represenationen der Insert-Methoden, das returnen was ins insert-After kommen würde
    //maximum Controlflow deepness als Input parameter
    //Verwendbare funktions:
    //generateLocalVariable(context)
    //global Assignments
    //local Assignments
    //Methodcalls
    //print
    //eigentlich alles vom Method-Context
}


