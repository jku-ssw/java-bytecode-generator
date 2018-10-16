package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;

import java.util.Random;

abstract class MethodCaller extends Generator {

    public MethodCaller(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }

    static String generateMethodCallString(String methodName, FieldVarType[] paramTypes, ParamWrapper[] paramValues) {
        StringBuilder statement = new StringBuilder(methodName + "(");
        if (paramValues != null && paramValues.length != 0) {
            statement.append(paramToCorrectStringFormat(paramTypes[0], paramValues[0]));
        }
        for (int i = 1; i < paramValues.length; i++) {
            statement.append(", ");
            statement.append(paramToCorrectStringFormat(paramTypes[i], paramValues[i]));
        }
        statement.append(");");
        return statement.toString();
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
