package generator;

import javassist.*;
import utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MathGenerator extends Generator {
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
        try {
            mathClazz = this.getClazzContainer().getClazzPool().get("java.lang.Math");
        } catch (NotFoundException e) {
            System.err.println("Cannot fetch CtClass-Object of java.lang.Math");
            e.printStackTrace();
        }
    }

    public boolean callRandomJavaLangMathMethod(MethodLogger method, ClazzLogger clazzLogger, boolean assignToFieldOrVar) {
        String callString = srcCallRandomJavaLangMathMethod(method, clazzLogger, assignToFieldOrVar);
        CtMethod callerMethod = this.getCtMethod(method);
        try {
            callerMethod.insertAfter(callString);
        } catch (CannotCompileException e) {
            System.err.println("Cannot insert call: " + callString);
            e.printStackTrace();
        }
        return true;
    }

    public String srcCallRandomJavaLangMathMethod(MethodLogger method, ClazzLogger clazzLogger, boolean assignToFieldOrVar) {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        CtMethod ctMethod = methods[random.nextInt(methods.length)];
        String methodName = ctMethod.getName();
        String signature = ctMethod.getSignature();
        List<FieldVarType> paramTypes = getParamTypes(signature);
        FieldVarType returnType = FieldVarType.getType(signature.charAt(signature.length() - 1));
        List<Object> parameters = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            Object p;
            if (random.nextBoolean()) { //try to fetch field
                p = getGlobalUsableVariableOfType(method, t, clazzLogger);
                if (p == null) p = method.getVariableWithPredicate(v -> v.getType() == t);
                if (p == null) p = RandomSupplier.getRandomValueAsString(t);
            } else { //try to fetch local variable
                p = method.getVariableWithPredicate(v -> v.getType() == t);
                if (p == null) p = getGlobalUsableVariableOfType(method, t, clazzLogger);
                if (p == null) p = RandomSupplier.getRandomValueAsString(t);
            }
            parameters.add(p);
        }
        StringBuilder callString = null;
        if (assignToFieldOrVar) {
            FieldVarLogger l = null;
            if (random.nextBoolean()) { //fetch field
                if (method.isStatic()) {
                    clazzLogger.getCompatibleVariableWithPredicate(v -> v.isStatic(), returnType);
                } else {
                    l = clazzLogger.getCompatibleVariable(returnType);
                }
            } else { //fetch local variable
                l = method.getCompatibleVariable(returnType);
            }
            if (l != null) callString = new StringBuilder(l.getName() + " = " + "Math." + methodName + "(");
        }
        if (callString == null) callString = new StringBuilder("Math." + methodName + "(");
        boolean first = true;
        for (Object o : parameters) {
            if (!first) callString.append(", ");
            else first = false;
            if (o instanceof FieldVarLogger) {
                callString.append(((FieldVarLogger) o).getName());
            } else callString.append(o);
        }
        callString.append(");");
        return callString.toString();
    }


    //TODO in clazzLogger geben
    private FieldVarLogger getGlobalUsableVariableOfType(MethodLogger method, FieldVarType type, ClazzLogger clazzLogger) {
        FieldVarLogger l;
        if (method.isStatic()) l = clazzLogger.getVariableWithPredicate(v -> v.isStatic() && v.getType() == type);
        else l = clazzLogger.getVariableWithPredicate(v -> v.getType() == type);
        return l;
    }

    private static List<FieldVarType> getParamTypes(String signature) {
        List<FieldVarType> paramTypes = new ArrayList<>();
        for (int i = 1; i < signature.length() - 2; i++) {
            paramTypes.add(FieldVarType.getType(signature.charAt(i)));
        }
        return paramTypes;
    }

}
