package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.exceptions.MethodCompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.resolvers.JavassistResolver;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.VoidType.VOID;
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

    @SuppressWarnings("unchecked")
    private <T> MethodLogger<T> generateMethod(String name, MetaType<T> returnType, MetaType[] paramTypes, int modifiers) {
        MethodLogger<T> ml = new MethodLogger<>(rand, getClazzLogger(), name, modifiers, returnType, paramTypes);
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
        if (returnType.kind() == MetaType.Kind.VOID) {
            returnStatement = "";
        } else {
            returnStatement = "return " + new JavassistResolver().resolve(getClazzLogger().valueOf(returnType, ml)) + ";";
        }
        getClazzLogger().register(ml);
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

    public void generateMethodBody(MethodLogger<?> method) {
        RandomCodeGenerator.Context.METHOD_CONTEXT.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.METHOD_CONTEXT);
        this.insertReturn(method);
    }

    public MethodLogger<?> generateMethod(int maximumParameters) {
        String methodName = getRandomSupplier().getMethodName();
        return this.generateMethod(methodName, getRandomSupplier().returnType(),
                getParameterTypes(maximumParameters), getRandomSupplier().getMethodModifiers());
    }

    public Optional<MethodLogger<?>> overloadMethod(int maximumParameters) {
        return getClazzLogger().randomGeneratedMethod()
                .map(methodToOverload -> {

                    List<MethodLogger<?>> overLoadedMethods = this.getClazzLogger().getOverloadedMethods(methodToOverload.name());
                    MetaType[] paramTypes = this.getDifferentParamTypes(overLoadedMethods, maximumParameters);
                    if (paramTypes == null) {
                        return null;
                    }

                    return this.generateMethod(methodToOverload.name(),
                            getRandomSupplier().returnType(), paramTypes, getRandomSupplier().getMethodModifiers());
                });
    }

    public <T> void insertReturn(MethodLogger<T> method) {
        CtMethod ctMethod = getCtMethod(method);
        MetaType<T> returnType = method.returns();

        if (returnType == VOID) {
            try {
                ctMethod.insertAfter(Return);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        } else {

            new Randomizer(rand).<FieldVarLogger<?>>oneNotNullOf(
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

    private String srcCallMethod(MethodBuilder<?> calledMethod, MethodLogger<?> method) {
        List<? extends MetaType<?>> paramTypes = calledMethod.argumentTypes();
        Stream<ParamWrapper<?>> values = getClazzLogger().randomParameterValues(paramTypes.stream(), method);
        calledMethod.exclude(method);
        String caller = calledMethod.isStatic() ? clazzContainer.getFileName() : "this";

        return caller + "." + generateMethodCallString(
                calledMethod.name(),
                paramTypes.toArray(new MetaType[0]),
                values.toArray(ParamWrapper[]::new)
        );
    }

    public void generateMethodCall(MethodLogger<?> method) {
        String src = srcGenerateMethodCall(method);
        insertIntoMethodBody(method, src);
    }

    public String srcGenerateMethodCall(MethodLogger<?> method) {
        return getClazzLogger().randomCallableMethod(method)
                .map(calledMethod -> srcCallMethod(calledMethod, method))
                .orElse(null);
    }

    public void setFieldToReturnValue(MethodLogger<?> method) {
        String src = srcSetFieldToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    @SuppressWarnings("unchecked")
    private <T> String setVariableToReturnValue(FieldVarLogger<T> fieldVar, MethodLogger<?> method) {
        List<? extends MetaType<?>> compatibleTypes = fieldVar.getType().getAssignableTypes();

        MethodLogger<T> calledMethod = (MethodLogger<T>) this.getClazzLogger().getRandomCallableMethodOfType(
                method, compatibleTypes.get(rand.nextInt(compatibleTypes.size())));
        if (calledMethod == null) {
            return null;
        }
        fieldVar.setInitialized();
        return fieldVar.access() + " = (" + fieldVar.getType() + ") " + srcCallMethod(calledMethod, method);
    }

    public String srcSetFieldToReturnValue(MethodLogger<?> method) {
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger<?> fieldVar = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
            if (fieldVar == null) {
                return null;
            } else {
                return setVariableToReturnValue(fieldVar, method);
            }
        } else {
            return null;
        }
    }

    public void setLocalVarToReturnValue(MethodLogger<?> method) {
        String src = srcSetLocalVarToReturnValue(method);
        this.insertIntoMethodBody(method, src);
    }

    public String srcSetLocalVarToReturnValue(MethodLogger<?> method) {

        if (method.hasVariables()) {
            FieldVarLogger<?> fieldVar = method.getVariableWithPredicate(v -> !v.isFinal());
            if (fieldVar != null)
                return setVariableToReturnValue(fieldVar, method);
        }

        return null;
    }

    void generateRunMethod() {
        try {
            this.getClazzFile().addMethod(CtNewMethod.make("private void run() {}", this.getClazzFile()));
            CtConstructor constructor = CtNewConstructor.defaultConstructor(this.getClazzFile());
            this.getClazzFile().addConstructor(constructor);
        } catch (CannotCompileException e) {
            throw new CompilationFailedException(e);
        }
    }

    /**
     * Returns a computable value from this variable and this type.
     *
     * @param variable The variable (field, local variable)
     * @return a string describing how to get a hash value from this value
     */
    public <T> String getHashComputation(FieldVarLogger<T> variable) {
        return Statement(pAssign(cast(variable.getType().getHashCode(variable)).to(long.class)).to("hashValue"));
    }

    public void generateHashMethod() {
        StringBuilder src = new StringBuilder("long hashValue = 0; ");
        List<FieldVarLogger<?>> initGlobals = this.getClazzLogger().getVariablesWithPredicate(FieldVarLogger::isInitialized);
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
        CtMethod main = this.getCtMethod(this.getClazzLogger().main());
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

    private MetaType[] getDifferentParamTypes(List<MethodLogger<?>> overloadedMethods, int maximumNumberOfParams) {
        for (int i = 0; i < overloadedMethods.size(); i++) {
            MetaType[] parameterTypes = getParameterTypes(maximumNumberOfParams);
            Stream<MethodLogger<?>> equalNumberOfParamMethods = overloadedMethods.stream().filter(
                    m -> m.getParamTypes().length == parameterTypes.length);
            if (!equalOverloadedParamTypesExists(equalNumberOfParamMethods, parameterTypes)) {
                return parameterTypes;
            }
        }
        return null;
    }

    private boolean equalOverloadedParamTypesExists(Stream<MethodLogger<?>> equalNumberOfParamMethods, MetaType[] parameterTypes) {
        return equalNumberOfParamMethods.anyMatch(ml -> equalParameterTypes(parameterTypes, ml.getParamTypes()));
    }

    private static boolean equalParameterTypes(MetaType[] types1, MetaType[] types2) {
        return types1.length == types2.length &&
                IntStream.range(0, types1.length)
                        .allMatch(i -> types1[i].equals(types2[i]));
    }

}
