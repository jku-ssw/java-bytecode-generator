package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import utils.*;

import java.util.List;
import java.util.Random;

public class MethodGenerator extends Generator {

    public MethodGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public MethodGenerator(String filename) {
        super(filename);
    }

    private Random random = new Random();

    /**
     * @param name       the name of the generated method
     * @param returnType the returnType of this method given by a FieldVarType
     * @param paramTypes the parameterTypes of this method given by FieldVarTypes
     * @param modifiers  merged modifiers for the new field (from javassist class Modifier)
     * @return
     */
    public MethodLogger generateMethod(String name, FieldVarType returnType, FieldVarType[] paramTypes, int modifiers) {
        try {
            MethodLogger ml = new MethodLogger(name, modifiers, returnType, paramTypes);
            StringBuilder paramsStrB = new StringBuilder("");
            String returnStatement = null;
            if (paramTypes.length != 0) {
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
                returnStatement = "return " + paramToCorrectStringFormat(returnType, RandomSupplier.getRandomValue(returnType)) + ";";
            }
            String returnTypeStr = returnType.getName();
            this.getClazzLogger().logMethod(ml);
            CtMethod newMethod = CtNewMethod.make(modifiersToString(modifiers) +
                    returnTypeStr + " " + name + "(" + paramsStrB.toString() + ") {" + returnStatement +
                    "} ", this.getClazzFile());
            this.getClazzFile().addMethod(newMethod);
            return ml;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public boolean overrideReturnStatement(MethodLogger method) {
//        CtMethod m = this.getCtMethod(method);
//        MethodInfo minfo = m.getMethodInfo();
//        CodeAttribute codeAttribute = minfo.getCodeAttribute();
//        CodeIterator iterator = codeAttribute.iterator();
//        iterator
//        codeAttribute.set
//        minfo.removeAttribute(lastAttribute.getName());
//    }

    public boolean overrideReturnStatement(MethodLogger method) {
        CtMethod ctMethod = this.getCtMethod(method);
        FieldVarType returnType = method.getReturnType();
        FieldVarLogger f = null;
        if (random.nextBoolean()) {
            f = fetchLocalReturnValue(method, returnType);
            if (f != null) fetchGlobalReturnValue(method, returnType);
        } else {
            System.out.println("heeeerrreee");
            f = fetchGlobalReturnValue(method, returnType);
            if (f != null) {
                fetchLocalReturnValue(method, returnType);
                System.out.println(f.getName());
            }
        }
        try {
            if (f != null) ctMethod.insertAfter("return " + f.getName() + ";");
            else ctMethod.insertAfter("return " + RandomSupplier.getRandomValueAsString(returnType) + ";");
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    private FieldVarLogger fetchLocalReturnValue(MethodLogger method, FieldVarType returnType) {
        return method.getVariableWithPredicate(v -> v.getType() == returnType);
    }

    private FieldVarLogger fetchGlobalReturnValue(MethodLogger method, FieldVarType returnType) {
        if (method.isStatic()) {
            return this.getClazzLogger().getVariableWithPredicate(v -> v.isInitialized() && v.isStatic());
        } else {
            return this.getClazzLogger().getVariableWithPredicate(v -> v.isInitialized());
        }
    }


    /**
     * @param modifiers the Integer-Representation of the modifiers
     * @return a String-Representation of these modifiers
     */
    private String modifiersToString(int modifiers) {
        StringBuilder b = new StringBuilder();
        if (Modifier.isStatic(modifiers)) b.append("static ");
        if (Modifier.isFinal(modifiers)) b.append("final ");
        if (Modifier.isPrivate(modifiers)) b.append("private ");
        if (Modifier.isProtected(modifiers)) b.append("protected ");
        if (Modifier.isPublic(modifiers)) b.append("public ");
        return b.toString();
    }

    /**
     * @param calledMethod the method that gets called
     * @param method       the method in which calledMethod is called
     * @param paramValues  the values used to call the method
     * @return @code{true} if the methodCall was generated successfully, otherwise @code{false}
     */
    public boolean generateMethodCall(MethodLogger calledMethod, MethodLogger method, Object... paramValues) {
        FieldVarType[] paramTypes = calledMethod.getParamsTypes();
        try {
            if (calledMethod.isStatic() || !method.isStatic()) {
                CtMethod m = this.getCtMethod(method);
                String callString = generateMethodCallString(calledMethod.getName(), paramTypes, paramValues);
                m.insertAfter(callString);
                return true;
            } else return false;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param methodName  the method that gets called
     * @param paramTypes  the paramTypes of the method that is called given by FieldVarType
     * @param paramValues the values used to call the method
     * @return a String-Representation of the methodCall
     */
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

    /**
     * @param paramType the FieldVarType of the given value
     * @param param     the parameter value to be returned as String
     * @return the correct String-Format for this value
     */
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

    /**
     * @param field        the field that is set to the return value of the function
     * @param calledMethod the method that is called
     * @param method       the method, in which the assign-statement is generated
     * @param paramValues  the values used to call the method
     * @return @code{true} if the statement was generated successfully, otherwise @code{false}
     */
    public boolean setFieldVarToReturnValue(FieldVarLogger field, MethodLogger calledMethod, MethodLogger method, Object... paramValues) {
        CtMethod m = this.getCtMethod(method);
        if (!field.isFinal()) {
            try {
                if (!calledMethod.isVoid() && (calledMethod.isStatic() || !method.isStatic()) &&
                        (method.hasVariable(field.getName()) || field.isStatic() || !method.isStatic())) {
                    m.insertAfter(field.getName() + " = "
                            + generateMethodCallString(calledMethod.getName(), calledMethod.getParamsTypes(), paramValues));
                    field.setInitialized();
                    return true;
                } else return false;
            } catch (CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * compares two arrays of parameters for equality
     * used to ensure that methods are not duplicated, when overloading methods
     *
     * @param types1
     * @param types2
     * @return @code{true} if these FieldVarTypes are equal, otherwise @code{false}
     */
    public static boolean compareParametersForEquality(FieldVarType[] types1, FieldVarType[] types2) {
        if (types1.length == types2.length) {
            for (int i = 0; i < types1.length; i++) {
                if (types1[i] != types2[i]) return false;
            }
            return true;
        } else return false;
    }

}
