package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.exceptions.MethodCompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.VoidType.VOID;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Assignments.pAssign;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statements.Return;

class MethodGenerator extends MethodCaller {

    private static final Logger logger = LogManager.getLogger();

    private final RandomCodeGenerator randomCodeGenerator;

    public MethodGenerator(Random rand, RandomCodeGenerator randomCodeGenerator) {
        super(rand, randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
    }

    public MetaType[] getParameterTypes(int maxParameters) {
        int n = maxParameters == 0 ? 0 : rand.nextInt(maxParameters);
        return getNParameterTypes(n);
    }

    public MetaType[] getNParameterTypes(int n) {
        MetaType[] types = new MetaType[n];
        for (int i = 0; i < n; i++) {
            types[i] = getRandomSupplier().type();
        }
        return types;
    }

    //============================================Method Generation=====================================================

    private MethodLogger generateMethod(String name, MetaType<?> returnType, MetaType[] paramTypes, int modifiers) {
        MethodLogger ml = new MethodLogger(rand, getClazzLogger().name, name, modifiers, returnType, paramTypes);
        StringBuilder paramsStr = new StringBuilder();
        if (paramTypes != null && paramTypes.length != 0) {
            String paramName = this.getRandomSupplier().getParVarName(1);
            paramsStr.append(paramTypes[0]).append(" ").append(paramName);
            ml.logVariable(paramName, clazzContainer.getFileName(), paramTypes[0], 0, true, false);
            for (int i = 1; i < paramTypes.length; i++) {
                paramsStr.append(", ");
                paramName = this.getRandomSupplier().getParVarName(i + 1);
                ml.logVariable(paramName, clazzContainer.getFileName(), paramTypes[i], 0, true, false);
                paramsStr.append(paramTypes[i]).append(" ").append(paramName);
            }
        }
        String returnStatement;
        if (returnType.kind == MetaType.Kind.VOID) {
            returnStatement = "";
        } else {
            returnStatement = "return " + getRandomSupplier().castedValue(returnType) + ";";
        }
        this.getClazzLogger().logMethod(ml);
        CtMethod newMethod;
        String methodStr = java.lang.reflect.Modifier.toString(modifiers) + " " +
                returnType + " " + name + "(" + paramsStr.toString() + ") {" + returnStatement +
                "} ";
        try {
            newMethod = CtNewMethod.make(methodStr, this.getClazzFile());
            this.getClazzFile().addMethod(newMethod);
            return ml;
        } catch (CannotCompileException e) {
            logger.fatal("Could not compile source code: {}", methodStr);
            throw new CompilationFailedException(e);
        }
    }

    public void generateMethodBody(MethodLogger method) {
        RandomCodeGenerator.Context.METHOD_CONTEXT.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.METHOD_CONTEXT);
        this.insertReturn(method);
    }

    public MethodLogger generateMethod(int maximumParameters) {
        String methodName = getRandomSupplier().getMethodName();
        return this.generateMethod(methodName, getRandomSupplier().returnType(),
                getParameterTypes(maximumParameters), getRandomSupplier().getMethodModifiers());
    }

    public MethodLogger overloadMethod(int maximumParameters) {
        MethodLogger methodToOverload = getClazzLogger().getRandomMethod();
        if (methodToOverload == null) {
            return null;
        }

        List<MethodLogger> overLoadedMethods = this.getClazzLogger().getOverloadedMethods(methodToOverload.getName());
        MetaType[] paramTypes = this.getDifferentParamTypes(overLoadedMethods, maximumParameters);
        if (paramTypes == null) {
            return null;
        }

        return this.generateMethod(methodToOverload.getName(),
                getRandomSupplier().returnType(), paramTypes, getRandomSupplier().getMethodModifiers());
    }

    public void insertReturn(MethodLogger method) {
        CtMethod ctMethod = getCtMethod(method);
        MetaType<?> returnType = method.getReturnType();

        if (returnType == VOID) {
            try {
                ctMethod.insertAfter(Return);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        } else {

            new Randomizer(rand).oneNotNullOf(
                    () -> getClazzLogger().getInitializedLocalVarOfType(method, returnType),
                    () -> getClazzLogger().getInitializedFieldOfTypeUsableInMethod(method, returnType)
            ).ifPresent(
                    f -> {
                        try {
                            ctMethod.insertAfter(Return(f.access()));
                        } catch (CannotCompileException e) {
                            throw new MethodCompilationFailedException(method, e);
                        }
                    }
            );
        }
    }

    //===============================================Method Calling=====================================================

    private String srcCallMethod(MethodLogger calledMethod, MethodLogger method) {
        MetaType[] paramTypes = calledMethod.getParamsTypes();
        ParamWrapper[] values = getClazzLogger().randomParameterValues(paramTypes, method);
        Set<MethodLogger> excludedForCalling = method.getMethodsExcludedForCalling();
        excludedForCalling.add(method);
        calledMethod.addToExcludedForCalling(excludedForCalling);
        Set<MethodLogger> calledByThisMethod = calledMethod.getMethodsCalledByThisMethod();
        calledByThisMethod.add(calledMethod);
        method.addMethodToCalledByThisMethod(calledByThisMethod);
        String caller = calledMethod.isStatic() ? clazzContainer.getFileName() : "this";

        return caller + "." + generateMethodCallString(calledMethod.getName(), paramTypes, values);
    }

    public void generateMethodCall(MethodLogger method) {
        String src = srcGenerateMethodCall(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGenerateMethodCall(MethodLogger method) {
        if (this.getClazzLogger().hasMethods()) {
            MethodLogger calledMethod = getClazzLogger().getRandomCallableMethod(method);
            if (calledMethod == null) {
                return null;
            }
            return srcCallMethod(calledMethod, method);
        } else {
            return null;
        }
    }

    public void setFieldToReturnValue(MethodLogger method) {
        String src = srcSetFieldToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    private String setVariableToReturnValue(FieldVarLogger fieldVar, MethodLogger method) {
        List<? extends MetaType<?>> compatibleTypes = fieldVar.getType().getAssignableTypes();

        MethodLogger calledMethod = this.getClazzLogger().getRandomCallableMethodOfType(
                method, compatibleTypes.get(rand.nextInt(compatibleTypes.size())));
        if (calledMethod == null) {
            return null;
        }
        fieldVar.setInitialized();
        return fieldVar.access() + " = (" + fieldVar.getType() + ") " + srcCallMethod(calledMethod, method);
    }

    public String srcSetFieldToReturnValue(MethodLogger method) {
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (fieldVar == null) {
                return null;
            } else {
                return setVariableToReturnValue(fieldVar, method);
            }
        } else {
            return null;
        }
    }

    public void setLocalVarToReturnValue(MethodLogger method) {
        String src = srcSetLocalVarToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    public String srcSetLocalVarToReturnValue(MethodLogger method) {

        if (method.hasVariables()) {
            FieldVarLogger fieldVar = method.getVariableWithPredicate(v -> !v.isFinal());
            if (fieldVar != null)
                return setVariableToReturnValue(fieldVar, method);
        }

        return null;
    }

    MethodLogger generateRunMethod() {
        try {
            this.getClazzFile().addMethod(CtNewMethod.make("private void run() {}", this.getClazzFile()));
            CtConstructor constructor = CtNewConstructor.defaultConstructor(this.getClazzFile());
            this.getClazzFile().addConstructor(constructor);
        } catch (CannotCompileException e) {
            throw new CompilationFailedException(e);
        }
        MethodLogger runLogger = new MethodLogger(rand, getClazzLogger().name, "run", Modifier.PRIVATE, VOID);
        this.getClazzLogger().setRun(runLogger);
        return runLogger;
    }

    /**
     * Returns a computable value from this variable and this type.
     *
     * @param variable The variable (field, local variable)
     * @return a string describing how to get a hash value from this value
     */
    public String getHashComputation(FieldVarLogger variable) {
        return Statement(pAssign(cast(variable.getType().getHashCode(variable)).to(long.class)).to("hashValue"));
    }

    public void generateHashMethod() {
        StringBuilder src = new StringBuilder("long hashValue = 0; ");
        List<FieldVarLogger> initGlobals = this.getClazzLogger().getVariablesWithPredicate(FieldVarLogger::isInitialized);
        if (this.getClazzLogger().hasVariables()) {
            src.append(
                    initGlobals.stream()
                            .map(this::getHashComputation)
                            .collect(Collectors.joining())
            );
        }
        String computeHashStr =
                spaced(
                        src.toString(),
                        Statement(
                                SystemOutPrintln(
                                        concat(
                                                asStr("#############   GLOBAL HASH: "),
                                                "hashValue",
                                                asStr("  #############")
                                        )
                                )
                        )
                );

        try {
            CtMethod computeHash = CtNewMethod.make("private void computeHash() {}", this.getClazzFile());
            computeHash.insertAfter(computeHashStr);
            this.getClazzFile().addMethod(computeHash);
        } catch (CannotCompileException e) {
            logger.fatal("Could not compile code to compute the hash value: {}", computeHashStr);
            throw new CompilationFailedException(e);
        }
    }

    public void callRunAndHashMethods(int xRuns) {
        String fileName = this.getClazzContainer().getFileName();
        CtMethod main = this.getCtMethod(this.getClazzLogger().getMain());
        try {
            if (xRuns <= 1) {
                main.insertAfter(fileName + " " + fileName.toLowerCase() + " = new " + fileName + "();"
                        + fileName.toLowerCase() + ".run();" +
                        fileName.toLowerCase() + ".computeHash();");
            } else {
                main.insertAfter(fileName + " " + fileName.toLowerCase() + " = new " + fileName + "();" +
                        "for(int xRuns = 0; xRuns < " + xRuns + "; xRuns++) {" + fileName.toLowerCase() + ".run();" +
                        "}" +
                        fileName.toLowerCase() + ".computeHash();");
            }
        } catch (CannotCompileException e) {
            throw new CompilationFailedException(e);
        }
    }

    //=================================================Utility==========================================================

    private MetaType[] getDifferentParamTypes(List<MethodLogger> overloadedMethods, int maximumNumberOfParams) {
        for (int i = 0; i < overloadedMethods.size(); i++) {
            MetaType[] parameterTypes = getParameterTypes(maximumNumberOfParams);
            Stream<MethodLogger> equalNumberOfParamMethods = overloadedMethods.stream().filter(
                    m -> m.getParamsTypes().length == parameterTypes.length);
            if (!equalOverloadedParamTypesExists(equalNumberOfParamMethods, parameterTypes)) {
                return parameterTypes;
            }
        }
        return null;
    }

    private boolean equalOverloadedParamTypesExists(Stream<MethodLogger> equalNumberOfParamMethods, MetaType[] parameterTypes) {
        return equalNumberOfParamMethods.anyMatch(ml -> equalParameterTypes(parameterTypes, ml.getParamsTypes()));
    }

    private static boolean equalParameterTypes(MetaType[] types1, MetaType[] types2) {
        return types1.length == types2.length &&
                IntStream.range(0, types1.length)
                        .allMatch(i -> types1[i].equals(types2[i]));
    }

}
