package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;

public class ParamWrapper<T> {

    //the value of the parameter, either an object of FieldVarLogger or a random value in string-format
    private final T paramValue;
    private final boolean isValue;

    public T getParamValue() {
        return paramValue;
    }

    public boolean isValue() {
        return isValue;
    }

    public boolean isVariable() {
        return !isValue;
    }

    public ParamWrapper(T paramValue) {
        if (paramValue instanceof String) {
            isValue = true;
        } else if (paramValue instanceof FieldVarLogger) {
            isValue = false;
        } else {
            throw new AssertionError("Wrong type of paramValue for ParamWrapper");
        }
        this.paramValue = paramValue;
    }

}
