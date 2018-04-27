package generator;

import javassist.*;
import utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


class MathGenerator extends Generator {
    private CtClass mathClazz;

    public MathGenerator(ClazzFileContainer cf) {
        super(cf);
        this.getClazzContainer().getClazzPool().importPackage("java.lang.Math");
        try {
            mathClazz = this.getClazzContainer().getClazzPool().get("java.lang.Math");
        } catch (NotFoundException e) {
            System.err.println("Cannot fetch CtClass-Object of java.lang.Math");
            e.printStackTrace();
        }
    }

    public MathGenerator(String filename) {
        super(filename);
        this.getClazzContainer().getClazzPool().importPackage("java.lang.Math");
    }

    public boolean callJavaLangMathMethod(MethodLogger method, ClazzLogger clazzLogger, FieldVarLogger... assignVar) {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        methods =Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        CtMethod ctMethod = methods[random.nextInt(methods.length)];
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        List<FieldVarType> paramTypes = getParamTypes(signature);
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        List<Object> parameters = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            Object p;
            if (random.nextBoolean()) {
                p = getGlobalUsableVariable(method, t, clazzLogger);
                if (p == null) p = method.getVariableOfType(t);
                if (p == null) p = RandomSupplier.getRandomValue(t);
            } else {
                p = method.getVariableOfType(t);
                if (p == null) p = getGlobalUsableVariable(method, t, clazzLogger);
                if (p == null) p = RandomSupplier.getRandomValue(t);
            }
            parameters.add(p);
        }
        StringBuilder callString = new StringBuilder("Math." + methodName + "(");
        boolean first = true;
        for (Object o : parameters) {
            if (!first) callString.append(", ");
            else first = false;
            if (o instanceof FieldVarLogger) {
                callString.append(((FieldVarLogger) o).getName());
            } else callString.append(o);
        }
        callString.append(");");
        CtMethod callerMethod = this.getCtMethod(method);
        try {
            callerMethod.insertAfter(callString.toString());
        } catch (CannotCompileException e) {
            System.err.println("Cannot insert call: " + callString.toString());
            e.printStackTrace();
        }
        return true;
    }

    private FieldVarLogger getGlobalUsableVariable(MethodLogger method, FieldVarType type, ClazzLogger clazzLogger) {
        FieldVarLogger l;
        if (method.isStatic()) l = clazzLogger.getStaticVariableOfType(type);
        else l = clazzLogger.getVariableOfType(type);
        return l;
    }

    private static List<FieldVarType> getParamTypes(String signature) {
        List<FieldVarType> paramTypes = new ArrayList<>();
        for (int i = 1; i < signature.length() - 2; i++) {
            paramTypes.add(getType(signature.charAt(i)));
        }
        return paramTypes;
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

}
