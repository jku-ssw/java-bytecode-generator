package utils;

import java.util.HashMap;

public class MethodLogger extends MyLogger{

    private String name;


    //TODO maybe add field for Parameters

    public MethodLogger(String name) {
        this.name = name;
        this.variables = new HashMap<>();
    }

    public String getName() {
        return name;
    }
}
