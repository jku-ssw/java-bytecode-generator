package generator;

import javassist.*;
import utils.*;

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
                String paramName = RandomSupplier.getVarName();
                paramsStrB.append(paramTypes[0].getName() + " " + paramName);
                ml.logParam(paramName, paramTypes[0]);
                for (int i = 1; i < paramTypes.length; i++) {
                    paramsStrB.append(", ");
                    paramName = RandomSupplier.getVarName();
                    ml.logParam(paramName, paramTypes[i]);
                    paramsStrB.append(paramTypes[i].getName() + " " + paramName);
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

    //TODO: add more MethodBody
    private String generateMethodBody(FieldVarType returnType) {
        if (returnType != FieldVarType.Void) {
            return "return " + paramToCorrectStringFormat(returnType, RandomSupplier.getValue(returnType)) + ";";
        } else {
            return "";
        }
    }

    /**
     * @param callMethodName
     * @param insertInMethod
     * @return
     */
    public boolean generateMethodCall(String callMethodName, String insertInMethod, Object... paramValues) {
        MethodLogger ml = this.getClazzLogger().getMethodLogger(callMethodName);
        FieldVarType[] paramTypes = ml.getParamsTypes();
        try {
            CtMethod m = this.getMethod(insertInMethod);
            StringBuilder statement = new StringBuilder(callMethodName + "(");
            if (paramValues.length != 0) {
                statement.append(paramToCorrectStringFormat(paramTypes[0], paramValues[0]));
            }
            for (int i = 1; i < paramValues.length; i++) {
                statement.append(", ");
                statement.append(paramToCorrectStringFormat(paramTypes[i], paramValues[i]));
            }
            statement.append(");");
            if (ml == null || !this.getClazzLogger().hasMethod(insertInMethod))
                return false;
            m.insertAfter(statement.toString());
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String paramToCorrectStringFormat(FieldVarType paramType, Object param) {
        if (param instanceof FieldVarLogger) {
            FieldVarLogger fvl = (FieldVarLogger) param;
            if (paramType == fvl.getType()) {
                return fvl.getName();
            } else return ""; //TODO exception wrong Type Parameter
        }
        switch (paramType) {
            case Byte:
                return "(byte)" + param;
            case Short:
                return "(short)" + param;
            case Int:
                return "" + param;
            case Long:
                return "(long)" + param;
            case Float:
                return "(float)" + param;
            case Double:
                return "" + param;
            case Boolean:
                return "" + param;
            case Char:
                return "'" + param + "'";
            case String:
                return "\"" + param + "\"";
        }
        return "";
    }

}

//TODO: add some MethodPool with interesting Methods