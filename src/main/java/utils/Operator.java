package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Operator {

    PLUS(" + "),
    MINUS(" - "),
    MUL(" * "),
    DIV(" / "),
    MOD(" % "),

    UN_PLUS("+"),
    UN_MINUS("-"),
    PLUS_PLUS("++"),
    MINUS_MINUS("--"),
    COMPLEMENT("!"),
    UN_BIT_COMP("~"),

    EQ(" == "),
    UNEQ(" != "),
    GRT(" > "),
    GRTE(" >= "),
    LT(" < "),
    LTE(" <= "),

    COND_AND(" && "),
    COND_OR(" || "),

    SHIFT_L(" << "),
    SHIFT_R(" >> "),
    US_SHIFT_R(" >>> "),
    BIT_AND(" & "),
    BIT_EX_OR(" ^ "),
    BIT_OR(" | ");

    public enum OpStatKind {
        ARITHMETIC,
        LOGICAL,
        BITWISE,
        ARITHMETIC_LOGICAL,
        ARITHMETIC_BITWISE,
        BITWISE_LOGICAL,
        ARITHMETIC_LOGICAL_BITWISE,
        RELATIONAL
    }

    private final String operator;

    private static final List<Operator> unaryOperators = Arrays.asList(
            UN_PLUS, UN_MINUS, PLUS_PLUS, MINUS_MINUS, COMPLEMENT, UN_BIT_COMP);

    private static final List<Operator> arithmeticOperators = Arrays.asList(
            PLUS, MINUS, DIV, MUL, MOD, UN_PLUS, UN_MINUS, PLUS_PLUS, MINUS_MINUS);

    private static final List<Operator> bitwiseOperators = Arrays.asList(
            SHIFT_L, SHIFT_R, US_SHIFT_R, BIT_AND, BIT_EX_OR, BIT_OR, UN_BIT_COMP);

    private static final List<Operator> logicalOperators = Arrays.asList(
            COMPLEMENT, COND_AND, COND_OR);

    private static final List<Operator> relationalOperators = Arrays.asList(
            EQ, UNEQ, GRT, GRTE, LT, LTE);

    Operator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return operator;
    }

    public boolean isUnary() {
        return unaryOperators.contains(this);
    }

    public static List<Operator> getNonUnaryOperatorsOfKind(OpStatKind opStatKind) {
        List<Operator> operators = new ArrayList<>(getOperatorsOfKind(opStatKind));
        operators.removeAll(unaryOperators);
        return operators;
    }

    public static List<Operator> getOperatorsOfKind(OpStatKind opStatKind) {
        switch (opStatKind) {
            case ARITHMETIC:
                return arithmeticOperators;
            case LOGICAL:
                return logicalOperators;
            case BITWISE:
                return bitwiseOperators;
            case RELATIONAL:
                return relationalOperators;
            default:
                return null;
        }
    }
}