package utils;

import logger.FieldVarLogger;

public class ParamWrapper<T> {

    //the value of the parameter, either an object of FieldVarLogger or a random value in string format
    private T paramValue;
    private boolean isValue = true;

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
        this.paramValue = paramValue;
        if (paramValue instanceof String) isValue = true;
        else if (paramValue instanceof FieldVarLogger) isValue = false;
    }

}
