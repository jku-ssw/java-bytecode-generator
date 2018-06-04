package generators;

import javassist.*;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.FieldVarType;
import utils.ParamWrapper;
import utils.RandomSupplier;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MethodGenerator extends MethodCaller {

    private RandomCodeGenerator randomCodeGenerator;

    private Random random = new Random();

    public MethodGenerator(RandomCodeGenerator randomCodeGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
    }

    //============================================Method Generation=====================================================

    public MethodLogger generateMethod(String name, FieldVarType returnType, FieldVarType[] paramTypes, int modifiers) {
        MethodLogger ml = new MethodLogger(name, modifiers, returnType, paramTypes);
        StringBuilder paramsStr = new StringBuilder();
        if (paramTypes != null && paramTypes.length != 0) {
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
        } else {
            returnStatement = "return " + RandomSupplier.getRandomValue(returnType) + ";";
        }
        this.getClazzLogger().logMethod(ml);
        CtMethod newMethod;
        try {
            newMethod = CtNewMethod.make(modifiersToString(modifiers) +
                    returnType.getName() + " " + name + "(" + paramsStr.toString() + ") {" + returnStatement +
                    "} ", this.getClazzFile());
            this.getClazzFile().addMethod(newMethod);
            return ml;
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void generateRandomMethodWithBody(int maximumParameters) {
        MethodLogger method = this.generateRandomMethod(maximumParameters);
        RandomCodeGenerator.Context.methodContext.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.methodContext);
        this.overrideReturnStatement(method);
    }

    public void overLoadRandomMethodWithBody(int maximumParameters) {
        MethodLogger method = this.overloadRandomMethod(maximumParameters);
        if (method == null) {
            return;
        }
        RandomCodeGenerator.Context.methodContext.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.methodContext);
        this.overrideReturnStatement(method);
    }

    /**
     * @param modifiers the Integer-Representation of the modifiers
     * @return a String-Representation of these modifiers
     */
    private static String modifiersToString(int modifiers) {
        StringBuilder b = new StringBuilder();
        if (Modifier.isStatic(modifiers)) {
            b.append("static ");
        }
        if (Modifier.isFinal(modifiers)) {
            b.append("final ");
        }
        if (Modifier.isPrivate(modifiers)) {
            b.append("private ");
        }
        if (Modifier.isProtected(modifiers)) {
            b.append("protected ");
        }
        if (Modifier.isPublic(modifiers)) {
            b.append("public ");
        }
        return b.toString();
    }

    private static boolean equalParameterTypes(FieldVarType[] types1, FieldVarType[] types2) {
        if (types1.length == types2.length) {
            for (int i = 0; i < types1.length; i++) {
                if (types1[i] != types2[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //===============================================Method Calling=====================================================

    public MethodLogger generateRandomMethod(int maximumParameters) {
        String methodName = getRandomSupplier().getMethodName();
        return this.generateMethod(methodName, getRandomSupplier().getReturnType(),
                getRandomSupplier().getParameterTypes(maximumParameters), getRandomSupplier().getModifiers());
    }

    public MethodLogger overloadRandomMethod(int maximumNumberOfParams) {
        MethodLogger methodToOverload = getClazzLogger().getRandomMethod();
        if (methodToOverload == null) {
            return null;
        }
        List<MethodLogger> overLoadedMethods = this.getClazzLogger().getOverloadedMethods(methodToOverload.getName());
        FieldVarType[] paramTypes = this.getDifferentParamTypes(overLoadedMethods, maximumNumberOfParams);
        if (paramTypes == null) {
            return null;
        }
        MethodLogger method = this.generateMethod(methodToOverload.getName(),
                RandomSupplier.getReturnType(), paramTypes, RandomSupplier.getModifiers());
        return method;
    }

    public void overrideReturnStatement(MethodLogger method) {
        CtMethod ctMethod = this.getCtMethod(method);
        FieldVarType returnType = method.getReturnType();
        FieldVarLogger f;
        if (random.nextBoolean()) {
            f = this.getClazzLogger().getInitializedLocalVarOfType(method, returnType);
            if (f != null) this.getClazzLogger().getInitializedFieldOfTypeUsableInMethod(method, returnType);
        } else {
            f = this.getClazzLogger().getInitializedFieldOfTypeUsableInMethod(method, returnType);
            if (f != null) {
                this.getClazzLogger().getInitializedLocalVarOfType(method, returnType);
            }
        }
        try {
            if (f != null) {
                ctMethod.insertAfter("return " + f.getName() + ";");
            }
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void generateRandomMethodCall(MethodLogger method) {
        String src = srcGenerateRandomMethodCall(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGenerateRandomMethodCall(MethodLogger method) {
        if (this.getClazzLogger().hasMethods()) {
            MethodLogger calledMethod = getClazzLogger().getRandomCallableMethod(method);
            if (calledMethod == null) {
                return null;
            }
            FieldVarType[] paramTypes = calledMethod.getParamsTypes();
            ParamWrapper[] values = getClazzLogger().getParamValues(paramTypes, method);
            calledMethod.addMethodToExcludedForCalling(method);
            return this.generateMethodCallString(calledMethod.getName(), paramTypes, values);
        } else {
            return null;
        }
    }

    public void setRandomFieldToReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToReturnValue(MethodLogger method) {
        MethodLogger calledMethod = this.getClazzLogger().getRandomCallableMethod(method);
        if (calledMethod == null) {
            return null;
        } else if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().
                    getNonFinalFieldOfTypeUsableInMethod(method, calledMethod.getReturnType());
            if (fieldVar == null) {
                return null;
            } else {
                return srcSetVariableToReturnValue(calledMethod, method, fieldVar);
            }
        } else {
            return null;
        }
    }

    public void setRandomLocalVarToReturnValue(MethodLogger method) {
        String src = srcSetRandomLocalVarToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToReturnValue(MethodLogger method) {
        MethodLogger calledMethod = this.getClazzLogger().getRandomCallableMethod(method);
        if (calledMethod == null) {
            return null;
        } else if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().
                    getNonFinalLocalVarOfType(method, calledMethod.getReturnType());
            if (fieldVar == null) {
                return null;
            } else {
                return srcSetVariableToReturnValue(calledMethod, method, fieldVar);
            }
        } else {
            return null;
        }
    }


    //=================================================Utility==========================================================

    private String srcSetVariableToReturnValue(MethodLogger calledMethod, MethodLogger method, FieldVarLogger fieldVar) {
        FieldVarType[] paramTypes = calledMethod.getParamsTypes();
        ParamWrapper[] values = getClazzLogger().getParamValues(paramTypes, method);
        calledMethod.addMethodToExcludedForCalling(method);
        return this.srcSetFieldVarToReturnValue(fieldVar, calledMethod, values);
    }

    private String srcSetFieldVarToReturnValue(FieldVarLogger field, MethodLogger calledMethod, ParamWrapper[] paramValues) {
        field.setInitialized();
        return field.getName() + " = "
                + generateMethodCallString(calledMethod.getName(), calledMethod.getParamsTypes(), paramValues);
    }

    private FieldVarType[] getDifferentParamTypes(List<MethodLogger> overloadedMethods, int maximumNumberOfParams) {
        for (int i = 0; i < overloadedMethods.size(); i++) {
            FieldVarType[] parameterTypes = RandomSupplier.getParameterTypes(maximumNumberOfParams);
            List<MethodLogger> equalNumberOfParamMethods = overloadedMethods.stream().filter(
                    m -> m.getParamsTypes().length == parameterTypes.length).collect(Collectors.toList());
            if (equalOverloadedParamTypeExists(equalNumberOfParamMethods, parameterTypes)) {
                continue;
            } else {
                return parameterTypes;
            }
        }
        return null;
    }

    private boolean equalOverloadedParamTypeExists(List<MethodLogger> equalNumberOfParamMethods, FieldVarType[] parameterTypes) {
        for (MethodLogger ml : equalNumberOfParamMethods) {
            if (this.equalParameterTypes(parameterTypes, ml.getParamsTypes())) {
                return true;
            }
        }
        return false;
    }

    public MethodLogger generateAndCallRunMethod() {
        CtMethod main = this.getCtMethod(this.getClazzLogger().getMain());
        try {
            this.getClazzFile().addMethod(CtNewMethod.make("private void run() {}", this.getClazzFile()));
            CtConstructor constructor = CtNewConstructor.defaultConstructor(this.getClazzFile());
            this.getClazzFile().addConstructor(constructor);
            String fileName = this.getClazzContainer().getFileName();
            main.insertAfter(fileName + " " + fileName.toLowerCase() + " = new " + constructor.getName() + "();" +
                    fileName.toLowerCase() + ".run();");
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
        MethodLogger runLogger = new MethodLogger("run", Modifier.PRIVATE, FieldVarType.Void);
        this.getClazzLogger().setRun(runLogger);
        return runLogger;
    }
}
