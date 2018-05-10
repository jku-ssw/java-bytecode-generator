package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import utils.ClazzFileContainer;
import utils.ClazzLogger;
import utils.MethodLogger;

class ControlFlowGenerator extends Generator {
    private StringBuilder blockSrc = new StringBuilder();

    private boolean openStatement = false;

    public ControlFlowGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public ControlFlowGenerator(String filename) {
        super(filename);
    }


    public void openIfStatement(MethodLogger method, ClazzLogger logger) {
        blockSrc = new StringBuilder("if(" + getRandomCondition(method, logger) + ") {");
        openStatement = true;
    }


    //TODO condition
    private String getRandomCondition(MethodLogger method, ClazzLogger logger) {
        return "true";
    }

    public boolean closeAndInsertStatement(MethodLogger method) {
        blockSrc.append("}");
        CtMethod ctMethod = this.getCtMethod(method);
        try {
            System.out.println(method.getName() + ":");
            System.out.println(blockSrc.toString());
            ctMethod.insertAfter(blockSrc.toString());
            openStatement = false;
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addCodeToControlSrc(String code) {
        if (openStatement) blockSrc.append(code);
        else {
            System.err.println("Cannot insert code, no open control-flow-block");
        }
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


