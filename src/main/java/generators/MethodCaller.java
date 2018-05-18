package generators;

import utils.ClazzFileContainer;
import logger.FieldVarLogger;
import utils.FieldVarType;
import utils.ParamWrapper;

abstract class MethodCaller extends Generator {
    public MethodCaller(ClazzFileContainer clazzContainer) {
        super(clazzContainer);
    }

    /**
     * @param methodName  the method that gets called
     * @param paramTypes  the paramTypes of the method that is called given by FieldVarType
     * @param paramValues the values used to call the method
     * @return a String-Representation of the methodCall
     */
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

    /**
     * @param paramType the FieldVarType of the given value
     * @param param     the parameter value to be returned as String
     * @return the correct String-Format for this value
     */
     static String paramToCorrectStringFormat(FieldVarType paramType, ParamWrapper param) {
        if (param.isVariable()) {
            FieldVarLogger fvl = (FieldVarLogger) param.getParamValue();
            if (paramType == fvl.getType()) return fvl.getName();
            else {
                System.err.println("Invalid parameter value for parameter type " + paramType.getName());
                return null;
            }
        } else if (param.isValue()) return (String) param.getParamValue();
        else {
            System.err.println("Incorrect Parameter type: Can either be of FieldVarLogger or String");
            return "";
        }
    }
}
