package generators;

import javassist.*;
import utils.*;
import logger.FieldVarLogger;
import logger.MethodLogger;

import java.util.*;


public class MathGenerator extends MethodCaller {
    private static List<String> OVERFLOW_METHODS = initBorders();

    private static List<String> initBorders() {
        List<String> borders = new ArrayList<>();
        borders.add("java.lang.Math.addExact(int,int)");
        borders.add("java.lang.Math.addExact(long,long)");
        borders.add("java.lang.Math.decrementExact(int)");
        borders.add("java.lang.Math.decrementExact(long)");
        borders.add("java.lang.Math.incrementExact(int)");
        borders.add("java.lang.Math.incrementExact(long)");
        borders.add("java.lang.Math.negateExact(int)");
        borders.add("java.lang.Math.negateExact(long)");
        borders.add("java.lang.Math.subtractExact(int,int)");
        borders.add("java.lang.Math.subtractExact(long,long)");
        borders.add("java.lang.Math.toIntExact(long)");
        borders.add("java.lang.Math.multiplyExact(int,int)");
        borders.add("java.lang.Math.multiplyExact(long,int)");
        borders.add("java.lang.Math.multiplyExact(long,long)");
        borders.add("java.lang.Math.floorDiv(int,int)");
        borders.add("java.lang.Math.floorDiv(long,int)");
        borders.add("java.lang.Math.floorDiv(long,long)");
        borders.add("java.lang.Math.floorMod(int,int)");
        borders.add("java.lang.Math.floorMod(long,int)");
        borders.add("java.lang.Math.floorMod(long,long)");
        return borders;
    }


    private static CtClass mathClazz;

    public MathGenerator(ClazzFileContainer cf) {
        super(cf);
        this.getClazzContainer().getClazzPool().importPackage("java.lang.Math");
        includeMathPackage();
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
        FieldVarType[] paramTypes = FieldVarType.getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.contains(mathMethod.getLongName())) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues) + "}";
            System.out.println(src);
            return src;
        } else return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
    }

    private static String getNoOverFlowIf(String longName, ParamWrapper[] paramValues) {
        switch (longName) {
//            case "java.lang.Math.addExact(int,int)":
//                //case "java.lang.Math.subtractExact(int,int)":
//                return "if((" + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) +
//                        " < 0 && " + paramToCorrectStringFormat(FieldVarType.Int, paramValues[1]) +
//                        " > 0) || (" + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) +
//                        " > 0 && " + paramToCorrectStringFormat(FieldVarType.Int, paramValues[1]) +
//                        " < 0) || (" + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " > 0 && (((" +
//                        paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " & 1 << 30) == 0) || ((" +
//                        paramToCorrectStringFormat(FieldVarType.Int, paramValues[1]) + " & 1 << 30) == 0))) || (" +
//                        paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " < 0 && (((" +
//                        paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " & 1 << 30) != 0) || ((" +
//                        paramToCorrectStringFormat(FieldVarType.Int, paramValues[1]) + " & 1 << 30) != 0)))) {";
//            case "java.lang.Math.addExact(long,long)":
//                //case "java.lang.Math.subtractExact(long,long)":
//                return "if((" + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " < 0L && " +
//                        paramToCorrectStringFormat(FieldVarType.Long, paramValues[1]) + " > 0L) " +
//                        "|| (" + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " > 0L && " +
//                        paramToCorrectStringFormat(FieldVarType.Long, paramValues[1]) + " < 0L) " +
//                        "|| ((" + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " > 0L) && ((("
//                        + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " & 1L << 62) == 0L) " +
//                        "|| ((" + paramToCorrectStringFormat(FieldVarType.Long, paramValues[1]) + " & 1L << 62) == 0L)))) {";
            case "java.lang.Math.decrementExact(int)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " > Integer.MIN_VALUE) {";
            case "java.lang.Math.decrementExact(long)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " > Long.MIN_VALUE) {";
            case "java.lang.Math.incrementExact(int)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " < Integer.MAX_VALUE) {";
            case "java.lang.Math.incrementExact(long)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " < Long.MAX_VALUE) {";
            case "java.lang.Math.negateExact(int)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Int, paramValues[0]) + " > Integer.MIN_VALUE) {";
            case "java.lang.Math.negateExact(long)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " > Long.MIN_VALUE) {";
            case "java.lang.Math.toIntExact(long)":
                return "if( " + paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " <= Integer.MAX_VALUE && " +
                         paramToCorrectStringFormat(FieldVarType.Long, paramValues[0]) + " >= Integer.MIN_VALUE) {";
//            case "java.lang.Math.multiplyExact(int,int)":
//            case "java.lang.Math.multiplyExact(long,long)":
            default:
                return null;

        }
    }

    public boolean setRandomFieldToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToMathReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = FieldVarType.getType(signature.charAt(signature.length() - 1));
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalFieldOfTypeUsableInMethod(method, returnType);
            if (fieldVar == null) return null;
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else return null;
    }


    private String srcSetVariableToMathReturnValue(CtMethod mathMethod, MethodLogger method, FieldVarLogger fieldVar) {
        FieldVarType[] paramTypes = FieldVarType.getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.contains(mathMethod.getLongName())) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + fieldVar.getName() + " = " + "Math." +
                    this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
            System.out.println(src);
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
        FieldVarType returnType = FieldVarType.getType(signature.charAt(signature.length() - 1));
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

}