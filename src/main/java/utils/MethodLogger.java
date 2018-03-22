package utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodLogger {

    private String name;

    private Map<FieldType, List<Field>> locals;

    //TODO maybe add field for Parameters

    public MethodLogger(String name) {
        this.name = name;
        this.locals = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<FieldType, List<Field>> getLocals() {
        return locals;
    }

}
