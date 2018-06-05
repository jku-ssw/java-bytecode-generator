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

import java.util.*;


public class MathGenerator extends MethodCaller {
    private static Map<String, String> OVERFLOW_METHODS;

    private static void initOverflowMethods() {
        OVERFLOW_METHODS = new HashMap<>();
        OVERFLOW_METHODS.put("java.lang.Math.addExact(int,int)", "if(%2$s > 0 ? Integer.MAX_VALUE - %2$s > %1$s : Integer.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.addExact(long,long)", "if(%2$s > 0 ? Long.MAX_VALUE - %2$s > %1$s : Long.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(int)", "if( %s < Integer.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(long)", "if( %s < Long.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(int,int)", "if(%2$s > 0 ? Integer.MAX_VALUE - %2$s < %1$s: Integer.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(long,long)", "if(%2$s > 0 ? Long.MAX_VALUE - %2$s < %1$s : Long.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.toIntExact(long)", "if( %1$s <= Integer.MAX_VALUE && %1$s >= Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(int,int)", "if(%1$s == 0 || Math.abs(Integer.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,int)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,long)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        String modDifCondition = "if(%s != 0) {";
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(int,int)", modDifCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,int)", modDifCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,long)", modDifCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(int,int)", modDifCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,int)", modDifCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,long)", modDifCondition);
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
            throw new AssertionError(e);
        }
    }

    public void generateRandomMathMethodCall(MethodLogger method, boolean noOverflow) {
        String callString = srcGenerateRandomMathMethodCall(method, noOverflow);
        insertIntoMethodBody(method, callString);
    }

    public String srcGenerateRandomMathMethodCall(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String methodName = mathMethod.getName();
        String signature = mathMethod.getSignature();
        FieldVarType[] paramTypes = getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) {
                return null;
            }
            String src = noOverFlowIf + "Math." +
                    this.generateMethodCallString(methodName, paramTypes, paramValues) + "}";
            return src;
        } else {
            return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
        }
    }

    public void setRandomFieldToMathReturnValue(MethodLogger method, boolean noOverflow) {
        String src = srcSetRandomFieldToMathReturnValue(method, noOverflow);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleFieldUsableInMethod(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar, noOverflow);
        } else {
            return null;
        }
    }


    private String srcSetVariableToMathReturnValue(CtMethod mathMethod, MethodLogger method, FieldVarLogger fieldVar, boolean noOverflow) {
        FieldVarType[] paramTypes = getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + fieldVar.getName() + " = " + "Math." +
                    this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
            return src;
        } else return fieldVar.getName() + " = " + "Math." +
                this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public void setRandomLocalVarToMathReturnValue(MethodLogger method, boolean noOverflow) {
        String src = srcSetRandomLocalVarToMathReturnValue(method, noOverflow);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToMathReturnValue(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleLocalVar(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar, noOverflow);
        } else {
            return null;
        }
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
            case "java.lang.Math.addExact(long,long)":
            case "java.lang.Math.subtractExact(int,int)":
            case "java.lang.Math.subtractExact(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[1], params[0]);
            case "java.lang.Math.decrementExact(int)":
            case "java.lang.Math.decrementExact(long)":
            case "java.lang.Math.incrementExact(int)":
            case "java.lang.Math.incrementExact(long)":
            case "java.lang.Math.negateExact(int)":
            case "java.lang.Math.negateExact(long)":
            case "java.lang.Math.toIntExact(long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[0]);
            case "java.lang.Math.multiplyExact(int,int)":
            case "java.lang.Math.multiplyExact(long,int)":
            case "java.lang.Math.multiplyExact(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[0], params[1]);
            case "java.lang.Math.floorDiv(int,int)":
            case "java.lang.Math.floorDiv(long,int)":
            case "java.lang.Math.floorMod(int,int)":
            case "java.lang.Math.floorMod(long,int)":
            case "java.lang.Math.floorDiv(long,long)":
            case "java.lang.Math.floorMod(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[1]);
            default:
                return null;
        }
    }

    private static FieldVarType getType(char t) {
        switch (t) {
            case 'D':
                return FieldVarType.DOUBLE;
            case 'I':
                return FieldVarType.INT;
            case 'F':
                return FieldVarType.FLOAT;
            case 'J':
                return FieldVarType.LONG;
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