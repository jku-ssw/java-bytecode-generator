package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import utils.*;

public class ControlFlowGenerator extends Generator {
    private StringBuilder blockSrc = new StringBuilder();

    private int deepness = 0;

    public ControlFlowGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public ControlFlowGenerator(String filename) {
        super(filename);
    }


    public void openIfStatement(MethodLogger method, ClazzLogger logger) {
        blockSrc.append("if(" + getRandomCondition(method, logger) + ") {");
        ++deepness;
    }

    //TODO condition
    private String getRandomCondition(MethodLogger method, ClazzLogger logger) {
        return "true";
    }

    public void closeStatement() {
        blockSrc.append("}");
        deepness--;
    }

    public boolean insertBlockSrc(MethodLogger method) {
        if (deepness != 0) return false;
        for (int i = 0; i < deepness; i++) blockSrc.append("}");
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.insertAfter(blockSrc.toString());
            blockSrc = new StringBuilder();
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addCodeToControlSrc(String code) {
        if (deepness > 0) {
            blockSrc.append(code);
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


