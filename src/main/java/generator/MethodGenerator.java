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
            MethodLogger ml = new MethodLogger(name, modifiers, returnType, paramTypes);
            StringBuilder paramsStrB = new StringBuilder("");
            String returnStatement = null;
            if (paramTypes != null) {
                String paramName = RandomSupplier.getVarName();
                paramsStrB.append(paramTypes[0].getName() + " " + paramName);
                ml.logVariable(RandomSupplier.getParVarName(1), paramTypes[0], 0, true);
                for (int i = 1; i < paramTypes.length; i++) {
                    paramsStrB.append(", ");
                    paramName = RandomSupplier.getVarName();
                    ml.logVariable(RandomSupplier.getParVarName(i + 1), paramTypes[i], 0, true);
                    paramsStrB.append(paramTypes[i].getName() + " " + paramName);
                    if (returnType == paramTypes[i] && returnStatement == null) {
                        returnStatement = "return " + paramName + ";";
                    }
                }

            }
            if (returnType == FieldVarType.Void) {
                returnStatement = "";
            } else if (returnStatement == null) {
                returnStatement = "return " + paramToCorrectStringFormat(returnType, RandomSupplier.getValue(returnType)) + ";";
            }
            String returnTypeStr = returnType.getName();
            this.getClazzLogger().logMethod(ml);
            System.out.println(modifiersToString(modifiers) + returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") {" + returnStatement +
                    "} ");
            CtMethod newMethod = CtNewMethod.make(modifiersToString(modifiers) +
                    returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") {" + returnStatement +
                    "} ", this.getClazzFile());
            this.getClazzFile().addMethod(newMethod);
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }

    }

    private String modifiersToString(int modifiers) {
        StringBuilder b = new StringBuilder();
        if (Modifier.isStatic(modifiers)) b.append("static ");
        if (Modifier.isFinal(modifiers)) b.append("final ");
        if (Modifier.isPrivate(modifiers)) b.append("private ");
        if (Modifier.isProtected(modifiers)) b.append("protected ");
        if (Modifier.isPublic(modifiers)) b.append("public ");
        return b.toString();
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
        if (paramType.getClazzType().getName().startsWith("java.lang") && param == null) {
            return "null";
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
            if (l != null) {
                String name = l.getName();
                name = name.replace(name.charAt(1), (char) (name.charAt(1) - 1));
                System.out.println(l.getName() + " " + name);
                System.out.println(methodName + " " + "return " + name + ";");
                System.out.println(ml.getReturnType() + " " + l.getType());
                //ml.removeVariable(l.getName(), l.getType());
                if (returnType == FieldVarType.Double || returnType == FieldVarType.Long) {
                    System.out.println("return (" + name + "<<32 + ) " + l.getName() + ";");
                    return "return (" + name + "<<32) + " + l.getName() + ";";
                }
                return "return " + name + ";";
            } else {
                //l = this.getClazzLogger().getRandomVariableOfType(returnType);
                //if (l != null) return "return " + l.getName() + ";";
                //else { //return random value if no variable of this returnType is available
                return "return " + paramToCorrectStringFormat(returnType, RandomSupplier.getValue(returnType)) + ";";
                //}
            }
        } else {
            return "return;";
        }
    }
}

//TODO: add some MethodPool with interesting Methods