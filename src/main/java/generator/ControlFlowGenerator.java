package generator;

import utils.ClazzFileContainer;
import utils.ClazzLogger;
import utils.MethodLogger;

class ControlFlowGenerator extends Generator {
    public ControlFlowGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public ControlFlowGenerator(String filename) {
        super(filename);
    }


    public boolean createIfStatement(MethodLogger method, ClazzLogger logger) {
        return false;
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


