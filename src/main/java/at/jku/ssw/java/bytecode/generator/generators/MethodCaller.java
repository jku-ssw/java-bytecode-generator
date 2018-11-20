package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.call;

abstract class MethodCaller extends Generator {

    public MethodCaller(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }

    static String generateMethodCallString(String methodName, MetaType<?>[] paramTypes, ParamWrapper[] paramValues) {
        String params = Optional.ofNullable(paramValues)
                .map(v -> IntStream.range(0, paramTypes.length)
                        .mapToObj(i ->
                                cast(paramValues[i].getParamValue())
                                        .to(paramTypes[i].clazz()))
                        .collect(Collectors.joining(", ")))
                .orElse("");
        return Statement(call(methodName, params));
    }

}
