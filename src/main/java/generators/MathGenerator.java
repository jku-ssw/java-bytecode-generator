package generators;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.FieldVarType;
import utils.ParamWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MathGenerator extends MethodCaller {
    private static List<String> OVERFLOW_METHODS;

    private static void initOverflowMethods() {
        OVERFLOW_METHODS = new ArrayList<>();
        OVERFLOW_METHODS.add("java.lang.Math.addExact(int,int)");
        OVERFLOW_METHODS.add("java.lang.Math.addExact(long,long)");
        OVERFLOW_METHODS.add("java.lang.Math.decrementExact(int)");
        OVERFLOW_METHODS.add("java.lang.Math.decrementExact(long)");
        OVERFLOW_METHODS.add("java.lang.Math.incrementExact(int)");
        OVERFLOW_METHODS.add("java.lang.Math.incrementExact(long)");
        OVERFLOW_METHODS.add("java.lang.Math.negateExact(int)");
        OVERFLOW_METHODS.add("java.lang.Math.negateExact(long)");
        OVERFLOW_METHODS.add("java.lang.Math.subtractExact(int,int)");
        OVERFLOW_METHODS.add("java.lang.Math.subtractExact(long,long)");
        OVERFLOW_METHODS.add("java.lang.Math.toIntExact(long)");
        OVERFLOW_METHODS.add("java.lang.Math.multiplyExact(int,int)");
        OVERFLOW_METHODS.add("java.lang.Math.multiplyExact(long,int)");
        OVERFLOW_METHODS.add("java.lang.Math.multiplyExact(long,long)");
        OVERFLOW_METHODS.add("java.lang.Math.floorDiv(int,int)");
        OVERFLOW_METHODS.add("java.lang.Math.floorDiv(long,int)");
        OVERFLOW_METHODS.add("java.lang.Math.floorDiv(long,long)");
        OVERFLOW_METHODS.add("java.lang.Math.floorMod(int,int)");
        OVERFLOW_METHODS.add("java.lang.Math.floorMod(long,int)");
        OVERFLOW_METHODS.add("java.lang.Math.floorMod(long,long)");
    }


    private static CtClass mathClazz;

    public MathGenerator(ClazzFileContainer cf) {
        super(cf);
        this.getClazzContainer().getClazzPool().importPackage("java.lang.Math");
        includeMathPackage();
        initOverflowMethods();
    }

    private void includeMathPackage() {
        try {
            mathClazz = this.getClazzContainer().getClazzPool().get("java.lang.Math");
        } catch (NotFoundException e) {
            System.err.println("Cannot fetch CtClass-Object of java.lang.Math");
            e.printStackTrace();
        }
    }

    public boolean generateRandomMathMethodCall(MethodLogger method) {
        String callString = srcGenerateRandomMathMethodCall(method);
        return this.insertIntoMethodBody(method, callString);
    }

    public String srcGenerateRandomMathMethodCall(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String methodName = mathMethod.getName();
        String signature = mathMethod.getSignature();
        FieldVarType[] paramTypes = getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.contains(mathMethod.getLongName())) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues) + "}";
            return src;
        } else return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
    }

    public boolean setRandomFieldToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToMathReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalFieldOfTypeUsableInMethod(method, returnType);
            if (fieldVar == null) return null;
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else return null;
    }


    private String srcSetVariableToMathReturnValue(CtMethod mathMethod, MethodLogger method, FieldVarLogger fieldVar) {
        FieldVarType[] paramTypes = getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.contains(mathMethod.getLongName())) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + fieldVar.getName() + " = " + "Math." +
                    this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
            return src;
        } else return fieldVar.getName() + " = " + "Math." +
                this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public boolean setRandomLocalVarToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomLocalVarToMathReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalLocalVarOfType(method, returnType);
            if (fieldVar == null) return null;
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else return null;
    }


    //================================================Utility===========================================================
    private static CtMethod getRandomMathMethod() {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        return methods[random.nextInt(methods.length)];
    }

    private static String getNoOverFlowIf(String longName, ParamWrapper[] paramValues, FieldVarType[] paramTypes) {
        String[] params = new String[2];
        params[0] = paramToCorrectStringFormat(paramTypes[0], paramValues[0]);
        if (paramTypes.length == 2) {
            params[1] = paramToCorrectStringFormat(paramTypes[1], paramValues[1]);
        }
        switch (longName) {
            case "java.lang.Math.addExact(int,int)":
                return "if(" + params[1] + " > 0 ? Integer.MAX_VALUE - " + params[1] + " > " + params[0] +
                        " : Integer.MIN_VALUE - " + params[1] + " < " + params[0] + ") {";
            case "java.lang.Math.addExact(long,long)":
                return "if(" + params[1] + " > 0 ? Long.MAX_VALUE - " + params[1] + " > " + params[0] +
                        " : Long.MIN_VALUE - " + params[1] + " < " + params[0] + ") {";
            case "java.lang.Math.subtractExact(int,int)":
                return "if(" + params[1] + " > 0 ? Integer.MAX_VALUE - " + params[1] + " < " + params[0] +
                        " : Integer.MIN_VALUE - " + params[1] + " > " + params[0] + ") {";
            case "java.lang.Math.subtractExact(long,long)":
                return "if(" + params[1] + " > 0 ? Long.MAX_VALUE - " + params[1] + " < " + params[0] +
                        " : Long.MIN_VALUE - " + params[1] + " > " + params[0] + ") {";
            case "java.lang.Math.decrementExact(int)":
                return "if( " + params[0] + " > Integer.MIN_VALUE) {";
            case "java.lang.Math.decrementExact(long)":
                return "if( " + params[0] + " > Long.MIN_VALUE) {";
            case "java.lang.Math.incrementExact(int)":
                return "if( " + params[0] + " < Integer.MAX_VALUE) {";
            case "java.lang.Math.incrementExact(long)":
                return "if( " + params[0] + " < Long.MAX_VALUE) {";
            case "java.lang.Math.negateExact(int)":
                return "if( " + params[0] + " > Integer.MIN_VALUE) {";
            case "java.lang.Math.negateExact(long)":
                return "if( " + params[0] + " > Long.MIN_VALUE) {";
            case "java.lang.Math.toIntExact(long)":
                return "if( " + params[0] + " <= Integer.MAX_VALUE && " +
                        params[0] + " >= Integer.MIN_VALUE) {";
            case "java.lang.Math.multiplyExact(int,int)":
                return "if(" + params[0] + " == 0 || Math.abs(Integer.MIN_VALUE/" + params[0] + ") > Math.abs(" + params[1] + ")) {";
            case "java.lang.Math.multiplyExact(long,long)":
                return "if(" + params[0] + " == 0 || Math.abs(Long.MIN_VALUE/" + params[0] + ") > Math.abs(" + params[1] + ")) {";
            case "java.lang.Math.floorDiv(int,int)":
            case "java.lang.Math.floorDiv(long,int)":
            case "java.lang.Math.floorMod(int,int)":
            case "java.lang.Math.floorMod(long,int)":
                return "if(" + params[1] + " != 0) {";
            case "java.lang.Math.floorDiv(long,long)":
            case "java.lang.Math.floorMod(long,long)":
                return "if(" + params[1] + " != 0) {";
            default:
                return null;
        }
    }

    private static FieldVarType getType(char t) {
        switch (t) {
            case 'D':
                return FieldVarType.Double;
            case 'I':
                return FieldVarType.Int;
            case 'F':
                return FieldVarType.Float;
            case 'J':
                return FieldVarType.Long;
            default:
                return null;
        }
    }

    private static FieldVarType[] getParamTypes(String methodSignature) {
        List<FieldVarType> paramTypes = new ArrayList<>();
        for (int i = 1; i < methodSignature.length() - 2; i++) {
            paramTypes.add(getType(methodSignature.charAt(i)));
        }
        FieldVarType[] paramTypesArray = new FieldVarType[paramTypes.size()];
        return paramTypes.toArray(paramTypesArray);
    }

}