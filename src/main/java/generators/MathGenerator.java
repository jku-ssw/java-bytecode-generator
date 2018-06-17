package generators;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.FieldVarType;
import utils.ParamWrapper;
import utils.RandomSupplier;

import java.util.*;

import static generators.MathGenerator.Operator.*;

public class MathGenerator extends MethodCaller {

    private static Map<String, String> OVERFLOW_METHODS;

    enum Operator {
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
        TERNARY("?: "),

        SHIFT_L(" << "),
        SHIFT_R(" >> "),
        US_SHIFT_R(" >>> "),
        BIT_AND(" & "),
        BIT_EX_OR(" ^ "),
        BIT_OR(" | ");

        //instance of?

        private final String operator;
        private static final List<Operator> unaryOperators = Arrays.asList(UN_PLUS, UN_MINUS, PLUS_PLUS, MINUS_MINUS, COMPLEMENT, UN_BIT_COMP);
        private static final List<Operator> arithmeticOperators = Arrays.asList(PLUS, MINUS, DIV, MUL, MOD, UN_PLUS, UN_MINUS, PLUS_PLUS, MINUS_MINUS);
        private static final List<Operator> relationalOperators = Arrays.asList(EQ, UNEQ, GRT, GRTE, LT, LTE);
        private static final List<Operator> bitwiseOperators = Arrays.asList(SHIFT_L, SHIFT_R, US_SHIFT_R, BIT_AND, BIT_EX_OR, BIT_OR, UN_BIT_COMP);
        private static final List<Operator> logicalOperators = Arrays.asList(COMPLEMENT, COND_AND, COND_OR); //TERNARY);

        Operator(String operator) {
            this.operator = operator;
        }

        @Override
        public String toString() {
            return operator;
        }

        boolean isUnary() {
            return unaryOperators.contains(this);
        }

        static List<Operator> getNonUnaryOperatorsOfOpStatKind(OpStatKind opStatKind) {
            List<Operator> operators = new ArrayList<>(getOperatorsOfOpStatKind(opStatKind));
            operators.removeAll(unaryOperators);
            return operators;
        }

        static List<Operator> getOperatorsOfOpStatKind(OpStatKind opStatKind) {
            switch (opStatKind) {
                case ARITHMETIC:
                    return arithmeticOperators;
                case LOGICAL:
                    return logicalOperators;
                case BITWISE:
                    return bitwiseOperators;
                case ARITHMETIC_LOGICAL:
                    List<Operator> arithmeticLogicalOperators = new ArrayList<>(arithmeticOperators);
                    arithmeticLogicalOperators.addAll(logicalOperators);
                    return arithmeticLogicalOperators;
                case ARITHMETIC_BITWISE:
                    List<Operator> arithmeticBitwiseOperators = new ArrayList<>(arithmeticOperators);
                    arithmeticBitwiseOperators.addAll(bitwiseOperators);
                    return arithmeticBitwiseOperators;
                case BITWISE_LOGICAL:
                    List<Operator> bitwiseLogicalOperators = new ArrayList<>(bitwiseOperators);
                    bitwiseLogicalOperators.addAll(bitwiseOperators);
                    return bitwiseLogicalOperators;
                case ARITHMETIC_LOGICAL_BITWISE:
                    List<Operator> arithmeticLogicalBitwiseOperators = new ArrayList<>(arithmeticOperators);
                    arithmeticLogicalBitwiseOperators.addAll(logicalOperators);
                    arithmeticLogicalBitwiseOperators.addAll(bitwiseOperators);
                    return arithmeticLogicalBitwiseOperators;
                default:
                    return null;
            }
        }
    }

    enum OpStatKind {
        ARITHMETIC,
        LOGICAL,
        BITWISE,
        ARITHMETIC_LOGICAL,
        ARITHMETIC_BITWISE,
        BITWISE_LOGICAL,
        ARITHMETIC_LOGICAL_BITWISE
    }

    private static void initOverflowMethods() {
        OVERFLOW_METHODS = new HashMap<>();
        OVERFLOW_METHODS.put("java.lang.Math.addExact(int,int)", "if(%2$s > 0 ? Integer.MAX_VALUE - %2$s > %1$s : Integer.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.addExact(long,long)", "if(%2$s > 0 ? Long.MAX_VALUE - %2$s > %1$s : Long.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(int)", "if( %s < Integer.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(long)", "if( %s < Long.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(int,int)", "if(%2$s > 0 ? Integer.MAX_VALUE - %2$s < %1$s: Integer.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(long,long)", "if(%2$s > 0 ? Long.MAX_VALUE - %2$s < %1$s : Long.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.toIntExact(long)", "if( %1$s <= Integer.MAX_VALUE && %1$s >= Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(int,int)", "if(%1$s == 0 || Math.abs(Integer.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,int)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,long)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s)) {");
        String modDivCondition = "if(%s != 0) {";
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,long)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,long)", modDivCondition);
    }

    private static CtClass mathClazz;

    public MathGenerator(ClazzFileContainer cf) {
        super(cf);
        this.getClazzContainer().getClazzPool().importPackage("java.lang.Math");
        includeMathPackage();
        initOverflowMethods();
    }

    private void includeMathPackage() {
        try {
            mathClazz = this.getClazzContainer().getClazzPool().get("java.lang.Math");
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public void generateRandomMathMethodCall(MethodLogger method, boolean noOverflow) {
        String callString = srcGenerateRandomMathMethodCall(method, noOverflow);
        insertIntoMethodBody(method, callString);
    }

    public String srcGenerateRandomMathMethodCall(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String methodName = mathMethod.getName();
        String signature = mathMethod.getSignature();
        FieldVarType[] paramTypes = getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) {
                return null;
            }
            String src = noOverFlowIf + "Math." +
                    this.generateMethodCallString(methodName, paramTypes, paramValues) + "}";
            return src;
        } else {
            return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
        }
    }

    public void setRandomFieldToMathReturnValue(MethodLogger method, boolean noOverflow) {
        String src = srcSetRandomFieldToMathReturnValue(method, noOverflow);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleFieldUsableInMethod(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar, noOverflow);
        } else {
            return null;
        }
    }


    private String srcSetVariableToMathReturnValue(CtMethod mathMethod, MethodLogger method, FieldVarLogger fieldVar, boolean noOverflow) {
        FieldVarType[] paramTypes = getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoOverFlowIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + fieldVar.getName() + " = " + "Math." +
                    this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
            return src;
        } else return fieldVar.getName() + " = " + "Math." +
                this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public void setRandomLocalVarToMathReturnValue(MethodLogger method, boolean noOverflow) {
        String src = srcSetRandomLocalVarToMathReturnValue(method, noOverflow);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToMathReturnValue(MethodLogger method, boolean noOverflow) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleLocalVar(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar, noOverflow);
        } else {
            return null;
        }
    }

    //================================================Utility===========================================================

    private static CtMethod getRandomMathMethod() {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        return methods[random.nextInt(methods.length)];
    }

    private static String getNoOverFlowIf(String longName, ParamWrapper[] paramValues, FieldVarType[] paramTypes) {
        String[] params = new String[2];
        params[0] = paramToCorrectStringFormat(paramTypes[0], paramValues[0]);
        if (paramTypes.length == 2) {
            params[1] = paramToCorrectStringFormat(paramTypes[1], paramValues[1]);
        }
        switch (longName) {
            case "java.lang.Math.addExact(int,int)":
            case "java.lang.Math.addExact(long,long)":
            case "java.lang.Math.subtractExact(int,int)":
            case "java.lang.Math.subtractExact(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[1], params[0]);
            case "java.lang.Math.decrementExact(int)":
            case "java.lang.Math.decrementExact(long)":
            case "java.lang.Math.incrementExact(int)":
            case "java.lang.Math.incrementExact(long)":
            case "java.lang.Math.negateExact(int)":
            case "java.lang.Math.negateExact(long)":
            case "java.lang.Math.toIntExact(long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[0]);
            case "java.lang.Math.multiplyExact(int,int)":
            case "java.lang.Math.multiplyExact(long,int)":
            case "java.lang.Math.multiplyExact(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[0], params[1]);
            case "java.lang.Math.floorDiv(int,int)":
            case "java.lang.Math.floorDiv(long,int)":
            case "java.lang.Math.floorMod(int,int)":
            case "java.lang.Math.floorMod(long,int)":
            case "java.lang.Math.floorDiv(long,long)":
            case "java.lang.Math.floorMod(long,long)":
                return String.format(OVERFLOW_METHODS.get(longName), params[1]);
            default:
                return null;
        }
    }

    private static FieldVarType getType(char t) {
        switch (t) {
            case 'D':
                return FieldVarType.DOUBLE;
            case 'I':
                return FieldVarType.INT;
            case 'F':
                return FieldVarType.FLOAT;
            case 'J':
                return FieldVarType.LONG;
            default:
                return null;
        }
    }

    private static FieldVarType[] getParamTypes(String methodSignature) {
        List<FieldVarType> paramTypes = new ArrayList<>();
        for (int i = 1; i < methodSignature.length() - 2; i++) {
            paramTypes.add(getType(methodSignature.charAt(i)));
        }
        FieldVarType[] paramTypesArray = new FieldVarType[paramTypes.size()];
        return paramTypes.toArray(paramTypesArray);
    }


    public void generateRandomOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind, boolean avoidDivByZero) {
        StringBuilder src = srcGenerateRandomOperatorStatement(method, maxOperations, opStatKind, avoidDivByZero);
        insertIntoMethodBody(method, src.toString());
    }
//
//
//    public void generateRandomOperatorStatementToLocal(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
//        String src = srcGenerateRandomOperatorStatementToLocal(method, maxOperations);
//        insertIntoMethodBody(method, src);
//    }
//
//    public void generateRandomOperatorStatementToField(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
//        String src = srcGenerateRandomOperatorStatementToField(method, maxOperations);
//        insertIntoMethodBody(method, src);
//    }
//
//    public String srcGenerateRandomOperatorStatementToField(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
//        if (this.getClazzLogger().hasVariables()) {
//            return null;
//        }
//        FieldVarLogger f1 = this.getClazzLogger().getNonFinalFieldUsableInMethod(method);
//        if (f1 == null) {
//            return null;
//        }
//        return srcGenerateRandomOperatorStatement(method, maxOperations, f1);
//    }
//
//    public String srcGenerateRandomOperatorStatementToLocal(MethodLogger method, int maxOperations) {
//        if (!method.hasVariables()) {
//            return null;
//        }
//        FieldVarLogger f1 = this.getClazzLogger().getNonFinalLocalVar(method);
//        if (f1 == null) {
//            return null;
//        }
//        return srcGenerateRandomOperatorStatement(method, maxOperations, f1);
//    }

    public StringBuilder srcGenerateRandomOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind, boolean avoidDivByZero) {
        int nr_ops = 1 + RANDOM.nextInt(maxOperations);
        StringBuilder operatorStatement = new StringBuilder();
        Operator operator = null;
        boolean useNonUnary;
        List<String> checkForDivByZero = new ArrayList<>();
        boolean addToDivByZero = false;
        for (int i = 0; i < nr_ops; i++) {
            useNonUnary = false;
            FieldVarLogger f = fetchOperand(method, opStatKind);
            String operand;
            if (f == null) {
                operand = fetchOperandValue(opStatKind);
                if (!(operand.equals("true") || operand.equals("false"))) {
                    useNonUnary = true;
                }
            } else {
                operand = f.getName();
                if (f.isFinal()) {
                    useNonUnary = true;
                }
            }
            if (addToDivByZero) {
                checkForDivByZero.add(operand);
                addToDivByZero = false;
            }
            operator = getOperator(opStatKind, useNonUnary);
            if (avoidDivByZero && (operator == MOD || operator == DIV)) {
                addToDivByZero = true;
            }
            switch (opStatKind) {
                case ARITHMETIC:
                case LOGICAL:
                case BITWISE:
                case ARITHMETIC_BITWISE:
                    if (operator.isUnary()) {
                        operatorStatement.append(operator + operand);
                        operator = getOperator(opStatKind, true);
                        operatorStatement.append(operator);
                    } else {
                        operatorStatement.append(operand + operator);
                    }
                case ARITHMETIC_LOGICAL:
                case BITWISE_LOGICAL:
                case ARITHMETIC_LOGICAL_BITWISE:
                    //TODO
            }
        }
        System.out.println(operatorStatement.toString());
        operatorStatement.delete(operatorStatement.length() - operator.toString().length(), operatorStatement.length());
        System.out.println(operatorStatement.toString());
        operatorStatement.append(";");
        if (!checkForDivByZero.isEmpty()) {
            operatorStatement = addIfToStatement(operatorStatement, checkForDivByZero);
        }
        return operatorStatement;
    }

    private static StringBuilder addIfToStatement(StringBuilder statement, List<String> checkForDivByZero) {
        StringBuilder ifStatement = new StringBuilder("if(");
        ifStatement.append(checkForDivByZero.get(0) + UNEQ + "0");
        for (int i = 1; i < checkForDivByZero.size(); i++) {
            ifStatement.append(COND_AND + checkForDivByZero.get(i) + UNEQ + "0");
        }
        ifStatement.append(") {");
        statement.insert(0, ifStatement);
        statement.append("}");
        return statement;
    }

    private static Operator getOperator(OpStatKind opStatKind, boolean nonUnary) {
        List<Operator> operators;
        if (nonUnary) {
            operators = Operator.getNonUnaryOperatorsOfOpStatKind(opStatKind);
        } else {
            operators = Operator.getOperatorsOfOpStatKind(opStatKind);
        }
        return operators.get(RANDOM.nextInt(operators.size()));
    }

//    public String srcGenerateRandomOperatorStatement(MethodLogger method, int maxOperations, FieldVarLogger f1) {
//        int nr_ops = 1 + RANDOM.nextInt(maxOperations);
//        List<FieldVarType> compatible_types = FieldVarType.getCompatibleTypes(f1.getType());
//        boolean different_types = false;
//        StringBuilder arithmeticStatement = new StringBuilder();
//        if (f1.getType() == FieldVarType.STRING) {
//            arithmeticStatement.append(RANDOM.nextBoolean() ? f1.getName() + " = " : f1.getName() + " += ");
//            for (int i = 0; i < nr_ops; i++) {
//                FieldVarLogger f2 = this.getClazzLogger().getGlobalOrLocalInitializedOfTypeUsableInMethod(method, f1.getType());
//                if (f2 != null) {
//                    arithmeticStatement.append(f2.getName() + " + ");
//                } else {
//                    arithmeticStatement.append(RandomSupplier.getRandomValue(f1.getType()) + " + ");
//                }
//            }
//        } else if (f1.getType() == FieldVarType.BOOLEAN) {
//            return null; //TODO logical statement
//        } else { //make arithmetic/bitwise statement
//            arithmeticStatement.append(f1.getName() + " " + getRandomArithmeticAssignOperator() + " ");
//            for (int i = 0; i < nr_ops; i++) {
//                FieldVarLogger f2 = this.getClazzLogger().getGlobalOrLocalCompatibleUsableInMethod(method, f1.getType());
//                if (!different_types && f1.getType() != f2.getType()) {
//                    different_types = true;
//                }
//                if (f2 != null) {
//                    arithmeticStatement.append(f2.getName() + getRandomArithmeticOperator());
//                } else {
//                    arithmeticStatement.append(RandomSupplier.getRandomValueNotNull(
//                            compatible_types.get(RANDOM.nextInt(compatible_types.size()))));
//                }
//            }
//        }
//        arithmeticStatement.delete(arithmeticStatement.length() - 3, arithmeticStatement.length());
//        if (different_types) { //cast
//            arithmeticStatement.deleteCharAt(arithmeticStatement.indexOf("=") + 1);
//            arithmeticStatement.insert(arithmeticStatement.indexOf("=") + 2, "(" + f1.getType().getName() + ")" + " (");
//            arithmeticStatement.append(");");
//        } else {
//            arithmeticStatement.append(";");
//        }
//        System.out.println(f1.getType());
//        System.out.println(arithmeticStatement.toString());
//        return arithmeticStatement.toString();
//    }

    private String fetchOperandValue(OpStatKind opStatKind) {
        switch (opStatKind) {
            case LOGICAL: //fetch boolean
                return RandomSupplier.getRandomValueNotNull(FieldVarType.BOOLEAN);
            case ARITHMETIC_LOGICAL:
            case BITWISE_LOGICAL:
            case ARITHMETIC_LOGICAL_BITWISE://fetch numeric or boolean
                List<FieldVarType> numericOrBoolean = FieldVarType.getNumericTypes();
                numericOrBoolean.add(FieldVarType.BOOLEAN);
                return RandomSupplier.getRandomValueNotNullOfTypes(numericOrBoolean);
            case ARITHMETIC:
            case BITWISE:
            case ARITHMETIC_BITWISE: //fetch numeric
                return RandomSupplier.getRandomValueNotNullOfTypes(FieldVarType.getNumericTypes());
        }
        return null;
    }

    private FieldVarLogger fetchOperand(MethodLogger method, OpStatKind opStatKind) {
        switch (opStatKind) {
            case LOGICAL: //fetch boolean
                return this.getClazzLogger().getGlobalOrLocalInitializedOfTypeUsableInMethod(method, FieldVarType.BOOLEAN);
            case ARITHMETIC_LOGICAL:
            case BITWISE_LOGICAL:
            case ARITHMETIC_LOGICAL_BITWISE://fetch numeric or boolean
                List<FieldVarType> numericOrBoolean = FieldVarType.getNumericTypes();
                numericOrBoolean.add(FieldVarType.BOOLEAN);
                return this.getClazzLogger().getGlobalOrLocalInitializedOfTypesUsableInMethod(method, numericOrBoolean);
            case ARITHMETIC: //fetch numeric
            case BITWISE:
            case ARITHMETIC_BITWISE: //fetch numeric
                return this.getClazzLogger().getGlobalOrLocalInitializedOfTypesUsableInMethod(method, FieldVarType.getNumericTypes());
        }
        return null;
    }


    //    public static String getRandomUnaryOperator(boolean arithmetic, boolean bitwise) {
//        String[] unOps = {"+", "-", "++", "--", "~"};
//        return unOps[RANDOM.nextInt(unOps.length)];
//    }
//
//    public static String getRandomArithmeticAssignOperator() {
//        String[] assOps = {" = ", " += ", " -= ", " /= ", " *= ", " %= "};
//        return assOps[RANDOM.nextInt(assOps.length)];
//    }
//
//    public static String getRandomBitAssignOperator(boolean arithmetic, boolean bitwise) {
//        String[] assOps = {" &= ", " |= ", " ^= ", " <<= ", " >>= ", " >>>= "}; //ok
//        return assOps[RANDOM.nextInt(assOps.length)];
//    }
//
//    public static String getRandomArithmeticOperator() {
//        String[] aritOps = {" + ", " - ", " * ", " / ", " % "}; //ok
//        return aritOps[RANDOM.nextInt(aritOps.length)];
//    }
    public static String getRandomRelOperator() {
        return Operator.relationalOperators.get(RANDOM.nextInt(Operator.relationalOperators.size())).toString();
    }
//
//    public static String getRandomBoolschOperator() {
//        String[] relOps = {" !", " && ", " || ", " ^ "}; //instanceof ??
//        return relOps[RANDOM.nextInt(relOps.length)];
//    }
//
//    public static String getRandomBitOperator() {
//        String[] bitOps = {" & ", " | ", " ^ ", ">>", "<<", ">>>",};
//        return bitOps[RANDOM.nextInt(bitOps.length)];
//    }

    //TODO arith Statement probability

    //TODO bitwise Statement probability

    //TODO if both mix according to probabilities

    //TODO logical Statement probability

    //TODO random arithmetic Statement

    //TODO random arithmetic and btiwise Statement

    //TODO random boolsch Statement

    //TODO random boolsch arithmetic bitwise

}