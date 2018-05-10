package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import utils.*;

import java.util.Random;

class ControlFlowGenerator extends Generator {
    private StringBuilder blockSrc = new StringBuilder();

    private boolean openStatement = false;
    private int deepness = 0;

    public ControlFlowGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public ControlFlowGenerator(String filename) {
        super(filename);
    }


    public void openIfStatement(MethodLogger method, ClazzLogger logger) {
        blockSrc.append("if(" + getRandomCondition(method, logger) + ") {");
        deepness++;
        openStatement = true;
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
        if(!openStatement && deepness != 0 ) return false;
        for(int i = 0; i < deepness; i++) blockSrc.append("}");
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            ctMethod.insertAfter(blockSrc.toString());
            openStatement = false;
            blockSrc = new StringBuilder();
            deepness = 0;
            return true;
        } catch (CannotCompileException e) {
            System.out.println(method.getName() + ":");
            System.out.println("failed: " + blockSrc.toString());
            //e.printStackTrace();
            return false;
        }
    }

    public void addCodeToControlSrc(String code) {
        if (openStatement) blockSrc.append(code);
        else {
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


