package at.jku.ssw.java.bytecode.generator.utils;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns.*;

public class StatementDSL {

    public static class Statements {
        public static final String Break = "break;";
        public static final String Return = "return;";
    }

    public static class Patterns {
        public static final String SYSTEM_OUT_PRINTLN = "System.out.println(%s)";
        public static final String AS_STRING = "\"%s\"";
        public static final String ASSIGN = "%s = %s";
        public static final String STATEMENT = "%s;";
        public static final String SUBTRACT = "%s - %s";
        public static final String CALL_NO_PARAMS = "%s()";
        public static final String IN_PAR = "(%s)";
        public static final String RETURN = "return %s;";
    }

    public static String spaced(String... words) {
        return String.join(" ", words);
    }

    public static String Statement(String str) {
        return String.format(STATEMENT, str);
    }

    public static String SystemOutPrintln(String str) {
        return String.format(SYSTEM_OUT_PRINTLN, str);
    }

    public static String asStr(String str) {
        return String.format(AS_STRING, str);
    }

    public static String assign(String dest, String src) {
        return String.format(ASSIGN, dest, src);
    }

    public static String subtract(String a, String b) {
        return String.format(SUBTRACT, a, b);
    }

    public static String call(String method) {
        return String.format(CALL_NO_PARAMS, method);
    }

    public static String concat(String... words) {
        return String.join(" + ", words);
    }

    public static String inPar(String str) {
        return String.format(IN_PAR, str);
    }

    public static String Return(String value) {
        return String.format(RETURN, value);
    }
}
