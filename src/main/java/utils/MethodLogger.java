package utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stores information about a Method
 */
public class MethodLogger extends MyLogger {

    private String name;

    private int modifiers;
    private FieldVarType[] paramTypes;

//    private Map<FieldVarType, List<FieldVarLogger>> params;


    private FieldVarType returnType;

    public MethodLogger(String name, int modifiers, FieldVarType returnType, FieldVarType paramTypes[]) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        //this.params = new HashMap<>();
        variables = new HashMap<>();
        this.paramTypes = paramTypes;
    }

//    /**
//     * logs a Parameter of the logged method
//     *
//     * @param paramName
//     * @param type
//     */
////    public void logParam(String paramName, FieldVarType type) {
////        FieldVarLogger l = new FieldVarLogger(paramName, modifiers, type, true);
////        if (params.get(type) == null) {
////            Map<String, FieldVarLogger> m = new HashMap<>();
////            m.put(name, f);
////            variables.put(type, m);
////        } else {
////            variables.get(type).put(name, f);
////        }
////        params.add(l);
////    }

    /**
     * @return the name of the logged method
     */
    public String getName() {
        return name;
    }

    /**
     * @return @code{true} if the logged method is Static
     */
    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    public FieldVarType[] getParamsTypes() {
        return paramTypes;
    }

    public FieldVarType getReturnType() {
        return returnType;
    }

    public void removeVariable(String name, FieldVarType type) {
        variables.get(type).remove(name);
    }
}
