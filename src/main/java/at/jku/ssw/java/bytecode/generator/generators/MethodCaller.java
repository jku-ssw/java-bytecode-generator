package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.call;

abstract class MethodCaller extends Generator {

    public MethodCaller(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }

    static String generateMethodCallString(String methodName, FieldVarType[] paramTypes, ParamWrapper[] paramValues) {
        String params = Optional.ofNullable(paramValues)
                .map(v ->
                        IntStream.range(0, paramTypes.length)
                                .mapToObj(i ->
                                        paramToCorrectStringFormat(paramTypes[i], paramValues[i]))
                                .collect(Collectors.joining(", ")))
                .orElse("");
        return Statement(call(methodName, params));
    }

    static String paramToCorrectStringFormat(FieldVarType<?> paramType, ParamWrapper param) {
        if (param.isVariable()) {
            FieldVarLogger fvl = (FieldVarLogger) param.getParamValue();
            if (paramType == fvl.getType()) {
                return fvl.getName();
            } else {
                throw new AssertionError("Invalid parameter value for parameter type " + paramType);
            }
        } else if (param.isValue()) {
            return param.getParamValue().toString();
        } else {
            throw new AssertionError("Incorrect Parameter type: Can either be of FieldVarLogger or String");
        }
    }
}
