package generator;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import utils.*;

import java.util.Random;

public class MethodGenerator extends Generator {

    public MethodGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public MethodGenerator(String filename) {
        super(filename);
    }

    private Random random = new Random();

    //============================================Method Generation=====================================================
    /**
     * @param name       the name of the generated method
     * @param returnType the returnType of this method given by a FieldVarType
     * @param paramTypes the parameterTypes of this method given by FieldVarTypes
     * @param modifiers  merged modifiers for the new field (from javassist class Modifier)
     * @return
     */
    private MethodLogger generateMethod(String name, FieldVarType returnType, FieldVarType[] paramTypes, int modifiers) {
        MethodLogger ml = new MethodLogger(name, modifiers, returnType, paramTypes);
        StringBuilder paramsStr = new StringBuilder();
        if (paramTypes.length != 0) {
            String paramName = this.getRandomSupplier().getParVarName(1);
            paramsStr.append(paramTypes[0].getName() + " " + paramName);
            ml.logVariable(paramName, paramTypes[0], 0, true);
            for (int i = 1; i < paramTypes.length; i++) {
                paramsStr.append(", ");
                paramName = this.getRandomSupplier().getParVarName(i + 1);
                ml.logVariable(paramName, paramTypes[i], 0, true);
                paramsStr.append(paramTypes[i].getName() + " " + paramName);
            }

        }
        String returnStatement;
        if (returnType == FieldVarType.Void) {
            returnStatement = "";
        } else returnStatement = "return " + RandomSupplier.getRandomValueAsString(returnType) + ";";
        this.getClazzLogger().logMethod(ml);
        CtMethod newMethod;
        try {
            newMethod = CtNewMethod.make(modifiersToString(modifiers) +
                    returnType.getName() + " " + name + "(" + paramsStr.toString() + ") {" + returnStatement +
                    "} ", this.getClazzFile());
            this.getClazzFile().addMethod(newMethod);
            return ml;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MethodLogger generateRandomMethod(int maximumParameters) {
        String methodName = getRandomSupplier().getMethodName();
        return this.generateMethod(methodName, getRandomSupplier().getReturnType(),
                getRandomSupplier().getParameterTypes(maximumParameters), getRandomSupplier().getModifiers());
    }

    public MethodLogger overloadRandomMethod(int maximumParameters) {
        MethodLogger methodToOverload = getClazzLogger().getRandomMethod();
        if (methodToOverload == null) return null;
        boolean equalParamTypes = true;
        FieldVarType[] paramTypes = null;
        while (equalParamTypes) {
            paramTypes = getRandomSupplier().getParameterTypes(maximumParameters);
            equalParamTypes = this.compareParametersForEquality(paramTypes, methodToOverload.getParamsTypes());
        }
        MethodLogger method = this.generateMethod(methodToOverload.getName(), getRandomSupplier().getReturnType(),
                paramTypes, getRandomSupplier().getModifiers());
        return method;
    }

    public boolean overrideReturnStatement(MethodLogger method) {
        CtMethod ctMethod = this.getCtMethod(method);
        FieldVarType returnType = method.getReturnType();
        FieldVarLogger f;
        if (random.nextBoolean()) {
            f = this.getClazzLogger().getInitializedLocalVarOfType(method, returnType);
            if (f != null) this.getClazzLogger().getInitializedFieldOfTypeUsableInMethod(method, returnType);
        } else {
            f = this.getClazzLogger().getInitializedFieldOfTypeUsableInMethod(method, returnType);
            if (f != null) this.getClazzLogger().getInitializedLocalVarOfType(method, returnType);
        }
        try {
            if (f != null) ctMethod.insertAfter("return " + f.getName() + ";");
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param modifiers the Integer-Representation of the modifiers
     * @return a String-Representation of these modifiers
     */
    private static String modifiersToString(int modifiers) {
        StringBuilder b = new StringBuilder();
        if (Modifier.isStatic(modifiers)) b.append("static ");
        if (Modifier.isFinal(modifiers)) b.append("final ");
        if (Modifier.isPrivate(modifiers)) b.append("private ");
        if (Modifier.isProtected(modifiers)) b.append("protected ");
        if (Modifier.isPublic(modifiers)) b.append("public ");
        return b.toString();
    }

//    /**
//     * @param calledMethod the method that gets called
//     * @param method       the method in which calledMethod is called
//     * @param paramValues  the values used to call the method
//     * @return @code{true} if the methodCall was generated successfully, otherwise @code{false}
//     */
//    public boolean generateMethodCall(MethodLogger calledMethod, MethodLogger method, ParamWrapper... paramValues) {
//        FieldVarType[] paramTypes = calledMethod.getParamsTypes();
//        if (calledMethod.isStatic() || !method.isStatic()) {
//            CtMethod m = this.getCtMethod(method);
//            String callString = generateMethodCallString(calledMethod.getName(), paramTypes, paramValues);
//            return insertIntoMethodBody(method, callString);
//        } else return false;
//    }

    //===============================================Method Calling=====================================================

    /**
     * @param methodName  the method that gets called
     * @param paramTypes  the paramTypes of the method that is called given by FieldVarType
     * @param paramValues the values used to call the method
     * @return a String-Representation of the methodCall
     */
    private static String generateMethodCallString(String methodName, FieldVarType[] paramTypes, ParamWrapper... paramValues) {
        StringBuilder statement = new StringBuilder(methodName + "(");
        if (paramValues != null && paramValues.length != 0) {
            statement.append(paramToCorrectStringFormat(paramTypes[0], paramValues[0]));
        }
        for (int i = 1; i < paramValues.length; i++) {
            statement.append(", ");
            statement.append(paramToCorrectStringFormat(paramTypes[i], paramValues[i]));
        }
        statement.append(");");
        return statement.toString();
    }


    public boolean generateRandomMethodCall(MethodLogger method) {
        String src = srcGenerateRandomMethodCall(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcGenerateRandomMethodCall(MethodLogger method) {
        if (this.getClazzLogger().hasMethods()) {
            MethodLogger calledMethod = getClazzLogger().getRandomCallableMethod(method);
            if (calledMethod == null) return null;
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            ParamWrapper[] values = getClazzLogger().getParamValues(paramTypes, method);
            calledMethod.addMethodToExcludedForCalling(method);
            return this.generateMethodCallString(calledMethod.getName(), paramTypes, values);
        } else return null;
    }

    public boolean setRandomFieldToReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToReturnValue(MethodLogger method) {
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (fieldVar == null) return null;
            return srcSetVariableToReturnValue(method, fieldVar);
        } else return null;
    }

    public boolean setRandomLocalVarToReturnValue(MethodLogger method) {
        String src = srcSetRandomLocalVarToReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToReturnValue(MethodLogger method) {
        if (!method.hasVariables()) return null;
        FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalLocalVar(method);
        if (fieldVar == null) return null;
        return srcSetVariableToReturnValue(method, fieldVar);
    }


    private String srcSetVariableToReturnValue(MethodLogger method, FieldVarLogger fieldVar) {
        MethodLogger calledMethod = getClazzLogger().getRandomMethodWithReturnTypeUsableInMethod(method, fieldVar.getType());
        if (calledMethod == null) return null;
        FieldVarType[] paramTypes = calledMethod.getParamsTypes();
        ParamWrapper[] values = getClazzLogger().getParamValues(paramTypes, method);
        calledMethod.addMethodToExcludedForCalling(method);
        return this.srcSetFieldVarToReturnValue(
                fieldVar, calledMethod, values);
    }

    private String srcSetFieldVarToReturnValue(FieldVarLogger field, MethodLogger calledMethod, ParamWrapper... paramValues) {
        field.setInitialized();
        return field.getName() + " = "
                + generateMethodCallString(calledMethod.getName(), calledMethod.getParamsTypes(), paramValues);
    }

//    /**
//     * @param field        the field that is set to the return value of the function
//     * @param calledMethod the method that is called
//     * @param method       the method, in which the assign-statement is generated
//     * @param paramValues  the values used to call the method
//     * @return @code{true} if the statement was generated successfully, otherwise @code{false}
//     */
//    public boolean setFieldVarToReturnValue(FieldVarLogger field, MethodLogger calledMethod, MethodLogger method, ParamWrapper... paramValues) {
//        String src = srcSetFieldVarToReturnValue(field, calledMethod, paramValues);
//        return insertIntoMethodBody(method, src);
//    }

    //=================================================Utility==========================================================

    /**
     * @param paramType the FieldVarType of the given value
     * @param param     the parameter value to be returned as String
     * @return the correct String-Format for this value
     */
    private static String paramToCorrectStringFormat(FieldVarType paramType, ParamWrapper param) {
        if (param.isVariable()) {
            FieldVarLogger fvl = (FieldVarLogger) param.getParam();
            if (paramType == fvl.getType()) return fvl.getName();
            else {
                System.err.println("Invalid parameter value for parameter type " + paramType.getName());
                return null;
            }
        } else if (param.isValue()) return (String) param.getParam();
        else {
            System.err.println("Incorrect Parameter type: Can either be of FieldVarLogger or String");
            return "";
        }
    }

    /**
     * compares two arrays of parameters for equality
     * used to ensure that methods are not duplicated, when overloading methods
     *
     * @param types1
     * @param types2
     * @return @code{true} if these FieldVarTypes are equal, otherwise @code{false}
     */
    private static boolean compareParametersForEquality(FieldVarType[] types1, FieldVarType[] types2) {
        if (types1.length == types2.length) {
            for (int i = 0; i < types1.length; i++) {
                if (types1[i] != types2[i]) return false;
            }
            return true;
        } else return false;
    }

}
