package generator;

import javassist.*;
import utils.*;

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
            CtMethod newMethod = CtNewMethod.make(
                    returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") { " +
                            generateMethodBody(returnType) + " }", this.getClazzFile());
            newMethod.setModifiers(modifiers);
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
            if (paramType == fvl.getType()) {
                return fvl.getName();
            } else {
                return null;
            }
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

    public void setFieldToReturnValue(FieldVarLogger f, String calledMethodName, String methodName, Object... paramValues) {
        MethodLogger ml = this.getClazzLogger().getMethodLogger(calledMethodName);
        CtMethod m = this.getMethod(methodName);
        if (!f.isFinal()) {
            try {
                m.insertAfter(f.getName() + " = " + generateMethodCallString(calledMethodName, ml.getParamsTypes(), paramValues));
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
    }
}

//TODO: add some MethodPool with interesting Methods