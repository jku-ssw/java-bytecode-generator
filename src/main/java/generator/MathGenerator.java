package generator;

import javassist.*;
import utils.*;
import utils.logger.FieldVarLogger;
import utils.logger.MethodLogger;

import java.util.Arrays;
import java.util.Random;


public class MathGenerator extends MethodCaller {
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
        CtMethod ctMethod = getRandomRandomMathMethod();
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        FieldVarType[] paramTypes = FieldVarType.getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
    }

    public boolean setRandomFieldToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToMathReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomRandomMathMethod();
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
        return fieldVar.getName() + " = " + "Math." + this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public boolean setRandomLocalVarToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomLocalVarToMathReturnValue(method);
        return insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = FieldVarType.getType(signature.charAt(signature.length() - 1));
        if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalLocalVarOfType(method, returnType);
            if (fieldVar == null) return null;
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else return null;
    }


    //================================================Utility===========================================================
    private static CtMethod getRandomRandomMathMethod() {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        return methods[random.nextInt(methods.length)];
    }

}