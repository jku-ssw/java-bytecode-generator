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

    public boolean generateMethod(String name, FieldVarType returnType, FieldVarType[] paramTypes, int[] modifiers) {
        if (returnType == null || paramTypes == null || modifiers == null) return false;
        try {
            MethodLogger ml = new MethodLogger(name);

            StringBuilder paramsStrB = new StringBuilder("");
            if (paramTypes != null) {
                paramsStrB.append(paramTypes[0].getName() + " " + RandomSupplier.getVarName());
                for (int i = 1; i < paramTypes.length; i++) {
                    paramsStrB.append(", ");
                    String paramName = RandomSupplier.getVarName();
                    ml.logParam(paramName, paramTypes[0]);
                    paramsStrB.append(paramTypes[0].getName() + " " + paramName);
                }
            }

            String returnTypeStr = returnType.getName();
            CtMethod newMethod = CtNewMethod.make(
                    returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") { " +
                            generateMethodBody(returnType) + " }", this.getClazzFile());

            int mod = mergeModifiers(modifiers);
            ml.setModifiers(mod);
            newMethod.setModifiers(mod);

            this.getClazzFile().addMethod(newMethod);
            this.getClazzLogger().logMethod(ml);

            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateMethodBody(FieldVarType returnType) {
        if (returnType == FieldVarType.Char) {
            return "return '" + RandomSupplier.getValue(returnType) + "';";
        } else if (returnType == FieldVarType.String) {
            return "return \"" + RandomSupplier.getValue(returnType) + "\";";
        } else if (returnType != null) {
            return "return " + RandomSupplier.getValue(returnType) + ";";
        }
        return "";
    }

//    public boolean generateMethodCall(String name) {
//
//    }

}
