package at.jku.ssw.java.bytecode.generator.utils;

import java.util.List;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns.*;

public class StatementDSL {

    public static class Statements {
        public static final String Break = "break;";
        public static final String Return = "return;";

        public static String Return(String value) {
            return String.format(RETURN, value);
        }
    }

    public static class Assignments<T> {
        public static final String LOCAL_VAR = "%s %s %s %s";
        public static final String ANY = "%s %s %s";
        public static final String ASSIGN = "=";
        public static final String PASSIGN = "+=";

        private final T value;
        private final String type;

        private Assignments(T value, String type) {
            this.value = value;
            this.type = type;
        }

        public static <T> Assignments<T> assign(T value) {
            return new Assignments<>(value, ASSIGN);
        }

        public static <T> Assignments<T> pAssign(T value) {
            return new Assignments<>(value, PASSIGN);
        }

        public String toLocalVar(Class<?> type, String name) {
            return String.format(LOCAL_VAR, type.getCanonicalName(), name, this.type, value);
        }

        public String to(String name) {
            return String.format(ANY, name, type, value);
        }
    }

    public static class Casts {
        public static final String CAST = "(%s) %s";

        private final Object value;

        public Casts(Object value) {
            this.value = value;
        }

        public static Casts cast(Object value) {
            return new Casts(value);
        }

        public String to(Class<?> type) {
            return String.format(CAST, type.getCanonicalName(), value);
        }

        public String to(String type) {
            return String.format(CAST, type, value);
        }
    }

    public static class Conditions {
        public static final String NOT_NULL = "%s != null";

        public static String notNull(String str) {
            return String.format(NOT_NULL, str);
        }
    }

    public static class Patterns {
        public static final String SYSTEM_OUT_PRINTLN = "System.out.println(%s)";
        public static final String AS_STRING = "\"%s\"";
        public static final String AS_CHAR = "\'%c\'";
        public static final String STATEMENT = "%s; ";
        public static final String SUBTRACT = "%s - %s";
        public static final String CALL_NO_PARAMS = "%s()";
        public static final String CALL_PARAMS = "%s(%s)";
        public static final String IN_PAR = "(%s)";
        public static final String RETURN = "return %s;";
        public static final String INCR = "%s++";
        public static final String DECR = "%s--";
        public static final String GT = "%s > %s";
        public static final String LT = "%s < %s";
        public static final String NEW = "new %s(%s)";
        public static final String NEW_ARRAY = "new %s";
        public static final String ARRAY_DIM = "[%s]";
        public static final String TERNARY = "(%s ? %s : %s)";
        public static final String NULL = "null";
        public static final String ARRAY = "[%s]";
        public static final String FIELD_ACCESS = "%s.%s";
        public static final String METHOD_CALL_NO_ARGS = "%s.%s()";
        public static final String VOID = "void";
    }

    public static class Blocks {
        public static final String IF = "if (%s) { ";
        public static final String WHILE = "while (%s) { ";
        public static final String Do = "do { ";
        public static final String BlockEnd = "} ";
        public static final String DOWHILE = "} while (%s);";
        public static final String Else = "} else { ";
        public static final String ELSEIF = "} else if (%s) { ";
        public static final String FOR = "for (%s; %s; %s) { ";

        public static String If(String condition) {
            return String.format(IF, condition);
        }

        public static String While(String condition) {
            return String.format(WHILE, condition);
        }

        public static String DoWhile(String condition) {
            return String.format(DOWHILE, condition);
        }

        public static String ElseIf(String condition) {
            return String.format(ELSEIF, condition);
        }

        public static String For(String init, String cond, String chang) {
            return String.format(FOR, init, cond, chang);
        }
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

    public static String asChar(char c) {
        return String.format(AS_CHAR, c);
    }

    public static String subtract(String a, String b) {
        return String.format(SUBTRACT, a, b);
    }

    public static String call(String method) {
        return String.format(CALL_NO_PARAMS, method);
    }

    public static String call(String method, String paramString) {
        return String.format(CALL_PARAMS, method, paramString);
    }

    public static String concat(String... words) {
        return String.join(" + ", words);
    }

    public static String inPar(String str) {
        return String.format(IN_PAR, str);
    }

    public static String incr(String str) {
        return String.format(INCR, str);
    }

    public static String decr(String str) {
        return String.format(DECR, str);
    }

    public static String gt(Object a, Object b) {
        return String.format(GT, a, b);
    }

    public static String lt(Object a, Object b) {
        return String.format(LT, a, b);
    }

    public static String New(String descriptor, String... params) {
        return String.format(NEW, descriptor, String.join(", ", params));
    }

    public static String NewArray(String descriptor, List<Object> dim) {
        String name =
                dim.stream()
                        .reduce(
                                descriptor.substring(0, descriptor.indexOf('[')),
                                (n, d) -> n + String.format(ARRAY_DIM, d),
                                String::concat
                        );

        return String.format(NEW_ARRAY, name);
    }

    public static <T> String ternary(String cond, T t, T e) {
        return String.format(TERNARY, cond, t, e);
    }

    public static String array(String name, List<Object> dim) {
        return name + dim.stream()
                .map(d -> String.format(ARRAY, d))
                .collect(Collectors.joining());
    }

    public static String field(String owner, String field) {
        return String.format(FIELD_ACCESS, owner, field);
    }

    public static String method(String owner, String name) {
        return String.format(METHOD_CALL_NO_ARGS, owner, name);
    }

    public static String variable(String owner, String v) {
        return owner.equals("") ? v : field(owner, v);
    }

}
