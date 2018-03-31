package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.CtNewMethod;
import utils.ClazzFileContainer;
import utils.FieldVarType;
import utils.MethodLogger;
import utils.RandomSupplier;

public class MethodGenerator extends Generator {

    public MethodGenerator(String filename) {
        super(filename);
    }

    public MethodGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public MethodGenerator() {
        super();
    }

    public boolean generateMethod(String name, FieldVarType returnType, FieldVarType[] params, int[] modifiers) {
        try {
            StringBuilder paramsStrB = new StringBuilder("");
            if (params != null) {
                paramsStrB.append(params[0].getName() + " " + RandomSupplier.getVarName());
                for (int i = 1; i < params.length; i++) {
                    paramsStrB.append(", ");
                    paramsStrB.append(params[0].getName() + " " + RandomSupplier.getVarName());
                }
            }
            String returnTypeStr = returnType != null ? returnType.getName() + " " : "void ";
            CtMethod newMethod = CtNewMethod.make(
                    returnTypeStr + name + "(" + paramsStrB.toString() + ") { " +
                            /*generateMethodBody() + */  " }", this.getClazzFile());
            newMethod.setModifiers(mergeModifiers(modifiers));
            this.getClazzFile().addMethod(newMethod);
            MethodLogger ml = new MethodLogger(name);
            this.getClazzLogger().logMethod(ml);
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

//    private String generateMethodBody() {
//
//    }

//    public boolean invokeMethod(String name) {
//
//    }

}
