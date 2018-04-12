package generator;

import javassist.*;
import utils.*;

import java.util.Random;

public class MethodGenerator extends Generator {

    public MethodGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public boolean generateMethod(String name, FieldVarType returnType, FieldVarType[] paramTypes, int modifiers) {
        try {
            MethodLogger ml = new MethodLogger(name, modifiers, returnType);
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
            this.getClazzLogger().logMethod(ml);
            CtMethod newMethod = CtNewMethod.make(
                    returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") {" + addReturnStatement(name) +
                            "} ", this.getClazzFile());
            newMethod.setModifiers(modifiers);
            this.getClazzFile().addMethod(newMethod);
            return true;
        } catch (
                CannotCompileException e)

        {
            e.printStackTrace();
            return false;
        }

    }

    public boolean generateMethodCall(String calledMethodName, String methodName, Object... paramValues) {
        MethodLogger ml = this.getClazzLogger().getMethodLogger(calledMethodName);
        FieldVarType[] paramTypes = ml.getParamsTypes();
        try {
            CtMethod m = this.getMethod(methodName);
            String callString = generateMethodCallString(calledMethodName, paramTypes, paramValues);
            m.insertAfter(callString);
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String generateMethodCallString(String methodName, FieldVarType[] paramTypes, Object... paramValues) {
        StringBuilder statement = new StringBuilder(methodName + "(");
        if (paramValues.length != 0) {
            statement.append(paramToCorrectStringFormat(paramTypes[0], paramValues[0]));
        }
        for (int i = 1; i < paramValues.length; i++) {
            statement.append(", ");
            statement.append(paramToCorrectStringFormat(paramTypes[i], paramValues[i]));
        }
        statement.append(");");
        return statement.toString();
    }

    private static String paramToCorrectStringFormat(FieldVarType paramType, Object param) {
        if (param instanceof FieldVarLogger) {
            FieldVarLogger fvl = (FieldVarLogger) param;
            if (paramType == fvl.getType()) return fvl.getName();
            else return null;
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
            default:
                return "";
        }
    }

    public boolean setFieldToReturnValue(FieldVarLogger f, String calledMethodName, String methodName, Object... paramValues) {
        MethodLogger ml = this.getClazzLogger().getMethodLogger(calledMethodName);
        CtMethod m = this.getMethod(methodName);
        if (!f.isFinal()) {
            try {
                m.insertAfter(f.getName() + " = "
                        + generateMethodCallString(calledMethodName, ml.getParamsTypes(), paramValues));
                return true;
            } catch (CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private String addReturnStatement(String methodName) {
        MethodLogger ml = this.getClazzLogger().getMethodLogger(methodName);
        FieldVarType returnType = ml.getReturnType();
        FieldVarLogger l = ml.getRandomVariableOfType(returnType);

        if (returnType != FieldVarType.Void) {
            if (l != null) return "return " + l.getName() + ";";
            else {
                l = this.getClazzLogger().getRandomVariableOfType(returnType);
                if (l != null) return "return " + l.getName() + ";";
                else { //return random value if no variable of this returnType is available
                    return "return " + paramToCorrectStringFormat(returnType, RandomSupplier.getValue(returnType)) + ";";
                }
            }
        } else {
            return "return;";
        }
    }
}

//TODO: add some MethodPool with interesting Methods