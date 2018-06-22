package generators;

import javassist.*;
import logger.FieldVarLogger;
import logger.MethodLogger;
import utils.*;

import java.util.*;

import static generators.MathGenerator.OpStatKind.*;
import static utils.Operator.*;

public class MathGenerator extends MethodCaller {

    private static boolean noDivByZero;
    private static boolean noOverflow;

    private final static Map<String, String> OVERFLOW_METHODS = new HashMap<>();

    private final Set<String> checkForDivByZero = new HashSet<>();
    private final Set<FieldVarLogger> incDecrementOperands = new HashSet<>();

    private static CtClass mathClazz;


    {
        try {
            mathClazz = ClassPool.getDefault().get("java.lang.Math");
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
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
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(int,int)", "if(%1$s == 0 || Math.abs(Integer.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,int)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,long)", "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Long.MIN_VALUE) {");
        String modDivCondition = "if(%s != 0) {";
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,long)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,long)", modDivCondition);
    }

    public MathGenerator(ClazzFileContainer cf, boolean noOverflow, boolean noDivByZero) {
        super(cf);
        this.noOverflow = noOverflow;
        this.noDivByZero = noDivByZero;
    }

    public enum OpStatKind {
        ARITHMETIC,
        LOGICAL,
        BITWISE,
        ARITHMETIC_LOGICAL,
        ARITHMETIC_BITWISE,
        BITWISE_LOGICAL,
        ARITHMETIC_LOGICAL_BITWISE
    }

    public void generateRandomMathMethodCall(MethodLogger method) {
        String callString = srcGenerateRandomMathMethodCall(method);
        insertIntoMethodBody(method, callString);
    }

    public String srcGenerateRandomMathMethodCall(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String methodName = mathMethod.getName();
        String signature = mathMethod.getSignature();
        FieldVarType[] paramTypes = getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && (noOverflow || noDivByZero)) {
            String noExceptionIf = getNoExceptionIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noExceptionIf == null) {
                return null;
            }
            String src = noExceptionIf + "Math." +
                    this.generateMethodCallString(methodName, paramTypes, paramValues) + "}";
            return src;
        } else {
            return "Math." + this.generateMethodCallString(methodName, paramTypes, paramValues);
        }
    }

    public void setRandomFieldToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomFieldToMathReturnValue(method);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomFieldToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (this.getClazzLogger().hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleFieldUsableInMethod(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else {
            return null;
        }
    }


    private String srcSetVariableToMathReturnValue(CtMethod mathMethod, MethodLogger method, FieldVarLogger fieldVar) {
        FieldVarType[] paramTypes = getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().getParamValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoExceptionIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            String src = noOverFlowIf + fieldVar.getName() + " = " + "Math." +
                    this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
            return src;
        } else return fieldVar.getName() + " = " + "Math." +
                this.generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public void setRandomLocalVarToMathReturnValue(MethodLogger method) {
        String src = srcSetRandomLocalVarToMathReturnValue(method);
        insertIntoMethodBody(method, src);
    }

    public String srcSetRandomLocalVarToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getRandomMathMethod();
        String signature = mathMethod.getSignature();
        FieldVarType returnType = getType(signature.charAt(signature.length() - 1));
        if (method.hasVariables()) {
            FieldVarLogger fieldVar = this.getClazzLogger().getNonFinalCompatibleLocalVar(method, returnType);
            if (fieldVar == null) {
                return null;
            }
            return srcSetVariableToMathReturnValue(mathMethod, method, fieldVar);
        } else {
            return null;
        }
    }

    public void generateRandomOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcGenerateRandomOperatorStatement(method, maxOperations, opStatKind);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcGenerateRandomOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        int numberOfOperands = 1 + RANDOM.nextInt(maxOperations);
        StringBuilder src = new StringBuilder();
        switch (opStatKind) {
            case ARITHMETIC:
            case LOGICAL:
            case BITWISE:
                src = srcGenerateOperatorStatement(method, numberOfOperands, opStatKind);
                break;
            case ARITHMETIC_BITWISE:
                src = generateArithmeticBitwiseStatement(method, numberOfOperands);
                break;
            case ARITHMETIC_LOGICAL:
                src = generateCombinedWithLogicalStatement(ARITHMETIC, method, numberOfOperands);
                break;
            case BITWISE_LOGICAL:
                src = generateCombinedWithLogicalStatement(BITWISE, method, numberOfOperands);
                break;
            case ARITHMETIC_LOGICAL_BITWISE:
                src = generateCombinedWithLogicalStatement(ARITHMETIC_BITWISE, method, numberOfOperands);
        }
        if (!checkForDivByZero.isEmpty()) {
            src = addIfToOperatorStatement(src, checkForDivByZero);
            checkForDivByZero.clear();
        }
        incDecrementOperands.clear();
        return src.toString();
    }

    public void generateRandomOperatorStatementToLocal(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcGenerateRandomOperatorStatementToLocal(method, maxOperations, opStatKind);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcGenerateRandomOperatorStatementToLocal(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        FieldVarLogger f = fetchLocalAssignVarForOperandStatement(method, opStatKind);
        if (f == null) {
            return null;
        }
        StringBuilder src = new StringBuilder(srcGenerateRandomOperatorStatement(method, maxOperations, opStatKind));
        if (src.indexOf("if") != -1) {
            src.insert(src.indexOf("{") + 1, f.getName() + " = (" + f.getType() + ") (");
        } else {
            src.insert(0, f.getName() + " = (" + f.getType() + ") (");
        }
        src.insert(src.indexOf(";"), ")");
        return src.toString();
    }

    public void generateRandomOperatorStatementToField(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcGenerateRandomOperatorStatementToField(method, maxOperations, opStatKind);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcGenerateRandomOperatorStatementToField(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        FieldVarLogger f = fetchGlobalAssignVarForOperandStatement(method, opStatKind);
        if (f == null) {
            return null;
        }
        StringBuilder src = new StringBuilder(srcGenerateRandomOperatorStatement(method, maxOperations, opStatKind));
        if (src.indexOf("if") != -1) {
            src.insert(src.indexOf("{") + 1, f.getName() + " = (" + f.getType() + ") (");
        } else {
            src.insert(0, f.getName() + " = (" + f.getType() + ") (");
        }
        src.insert(src.indexOf(";"), ")");
        return src.toString();
    }

    //================================================Utility===========================================================

    private static CtMethod getRandomMathMethod() {
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        methods = Arrays.stream(methods).filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1).toArray(CtMethod[]::new);
        Random random = new Random();
        return methods[random.nextInt(methods.length)];
    }

    private static String getNoExceptionIf(String longName, ParamWrapper[] paramValues, FieldVarType[] paramTypes) {
        String[] params = new String[2];
        params[0] = paramToCorrectStringFormat(paramTypes[0], paramValues[0]);
        if (paramTypes.length == 2) {
            params[1] = paramToCorrectStringFormat(paramTypes[1], paramValues[1]);
        }
        if (noOverflow) {
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
            }
        }
        if (noDivByZero) {
            switch (longName) {
                case "java.lang.Math.floorDiv(int,int)":
                case "java.lang.Math.floorDiv(long,int)":
                case "java.lang.Math.floorMod(int,int)":
                case "java.lang.Math.floorMod(long,int)":
                case "java.lang.Math.floorDiv(long,long)":
                case "java.lang.Math.floorMod(long,long)":
                    return String.format(OVERFLOW_METHODS.get(longName), params[1]);
            }
        }
        return null;
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


    private FieldVarLogger fetchLocalAssignVarForOperandStatement(MethodLogger method, OpStatKind opStatKind) {
        FieldVarType type = fetchAssignVarTypeForOperandStatement(opStatKind);
        return this.getClazzLogger().getNonFinalLocalVarOfType(method, type);
    }

    private FieldVarLogger fetchGlobalAssignVarForOperandStatement(MethodLogger method, OpStatKind opStatKind) {
        FieldVarType type = fetchAssignVarTypeForOperandStatement(opStatKind);
        return this.getClazzLogger().getNonFinalFieldOfTypeUsableInMethod(method, type);
    }

    private FieldVarType fetchAssignVarTypeForOperandStatement(OpStatKind opStatKind) {
        List<FieldVarType> types = new ArrayList<>();
        switch (opStatKind) {
            case LOGICAL:
                types.add(FieldVarType.BOOLEAN);
                break;
            case ARITHMETIC_LOGICAL:
            case BITWISE_LOGICAL:
            case ARITHMETIC_LOGICAL_BITWISE:
                types.add(FieldVarType.BOOLEAN);
            case BITWISE:
            case ARITHMETIC_BITWISE:
            case ARITHMETIC:
                types.addAll(FieldVarType.getNumericTypes());
        }
        return types.get(RANDOM.nextInt(types.size()));
    }

    private StringBuilder generateArithmeticBitwiseStatement(MethodLogger method, int numberOfOperands) {
        StringBuilder src = new StringBuilder();
        int maxPartitionSize = 1 + numberOfOperands / 2;
        Operator operator = null;
        while (numberOfOperands > 0) {
            int operandsInPartition = 1 + RANDOM.nextInt(maxPartitionSize);
            StringBuilder statement;
            FieldVarType type;
            if (RANDOM.nextBoolean()) {
                statement = srcGenerateOperatorStatement(method, operandsInPartition, ARITHMETIC);
                operator = getNonDivNonUnaryArithmeticOperator();
            } else {
                statement = srcGenerateOperatorStatement(method, operandsInPartition, BITWISE);
                operator = getOperator(BITWISE, true);
            }
            type = getOperandType(BITWISE);
            statement.insert(0, "(");
            statement.replace(statement.indexOf(";"), statement.indexOf(";") + 1, ")");
            statement.insert(0, "(" + type + ")");
            statement.append(operator);
            src.append(statement);
            numberOfOperands -= operandsInPartition;
        }
        src.delete(src.length() - operator.toString().length(), src.length());
        src.append(";");
        return src;
    }

    private StringBuilder generateCombinedWithLogicalStatement(OpStatKind bitAndOrArithmetic, MethodLogger method, int numberOfOperands) {
        StringBuilder src = new StringBuilder();
        int maxPartitionSize = 1 + numberOfOperands / 2;
        boolean openRel = false;
        boolean openLog = false;
        Operator operator = null;
        while (numberOfOperands > 0) {
            int operandsInPartition = 1 + RANDOM.nextInt(maxPartitionSize);
            StringBuilder statement;
            if ((RANDOM.nextBoolean() && !openRel) || openLog) {
                statement = srcGenerateOperatorStatement(method, operandsInPartition, LOGICAL);
                operator = getOperator(LOGICAL, true);
                openLog = !openLog;
                if (!openLog) {
                    operator = Operator.getRandomRelationalOperator();
                    openRel = true;
                }
            } else {
                if (bitAndOrArithmetic == ARITHMETIC_BITWISE) {
                    statement = generateArithmeticBitwiseStatement(method, numberOfOperands);
                } else {
                    statement = srcGenerateOperatorStatement(method, operandsInPartition, bitAndOrArithmetic);
                }
                List<Operator> relOperators = Operator.getRelationalOperators();
                operator = relOperators.get(RANDOM.nextInt(relOperators.size()));
                openRel = !openRel;
                if (!openRel) {
                    operator = getOperator(LOGICAL, true);
                    openLog = true;
                }
            }
            statement.insert(0, "(");
            statement.replace(statement.indexOf(";"), statement.indexOf(";") + 1, ")");
            statement.append(operator);
            src.append(statement);
            numberOfOperands -= operandsInPartition;
        }
        src.delete(src.length() - operator.toString().length(), src.length());
        src.append(";");
        return src;
    }

    private StringBuilder srcGenerateOperatorStatement(MethodLogger method, int nbrOfOperands, OpStatKind opStatKind) {
        Operator operator = null;
        StringBuilder operatorStatement = new StringBuilder();
        boolean useNonUnary;
        boolean addToCheckForDivByZero = false;
        for (int i = 0; i < nbrOfOperands; i++) {
            useNonUnary = false;
            FieldVarLogger f = fetchOperand(method, opStatKind, operator);
            String operand;
            FieldVarType type;
            if (f == null || (operator == DIV || operator == MOD) && incDecrementOperands.contains(f)) {
                type = getOperandType(opStatKind);
                if (type == FieldVarType.BOOLEAN) {
                    operand = RandomSupplier.getRandomCastedValue(type);
                } else {
                    if (operator == DIV || operator == MOD) {
                        operand = RandomSupplier.getRandomNumericValue(type, true);
                    } else {
                        operand = RandomSupplier.getRandomNumericValue(type, false);
                    }
                }
                if (!(operand.equals("true") || operand.equals("false"))) {
                    useNonUnary = true;
                }
                addToCheckForDivByZero = false;
            } else {
                operand = f.getName();
                if (f.isFinal() || (operator == MOD || operator == DIV)) {
                    useNonUnary = true;
                }
                if (addToCheckForDivByZero) {
                    checkForDivByZero.add(operand);
                    addToCheckForDivByZero = false;
                }
            }
            operator = getOperator(opStatKind, useNonUnary);
            if (operator.isUnary()) {
                operatorStatement.append(operator + operand);
                if (f != null && (operator == PLUS_PLUS || operator == MINUS_MINUS)) {
                    incDecrementOperands.add(f);
                }
                operator = getOperator(opStatKind, true);
                operatorStatement.append(operator);
            } else {
                operatorStatement.append(operand + operator);
            }

            if (noDivByZero && (operator == MOD || operator == DIV)) {
                addToCheckForDivByZero = true;
            }
        }
        operatorStatement.delete(operatorStatement.length() - operator.toString().length(), operatorStatement.length());
        operatorStatement.append(";");
        return operatorStatement;
    }

    private static StringBuilder addIfToOperatorStatement(StringBuilder statement, Set<String> checkForDivByZero) {
        String[] values = checkForDivByZero.toArray(new String[0]);
        StringBuilder ifStatement = new StringBuilder("if(");
        ifStatement.append(values[0] + UNEQ + "0");
        for (int i = 1; i < checkForDivByZero.size(); i++) {
            ifStatement.append(COND_AND + values[i] + UNEQ + "0");
        }
        ifStatement.append(") {");
        statement.insert(0, ifStatement);
        statement.append("}");
        return statement;
    }

    private static Operator getOperator(OpStatKind opStatKind, boolean nonUnary) {
        List<Operator> operators;
        if (nonUnary) {
            operators = Operator.getNonUnaryOperatorsOfKind(opStatKind);
        } else {
            operators = Operator.getOperatorsOfKind(opStatKind);
        }
        return operators.get(RANDOM.nextInt(operators.size()));
    }

    private static Operator getNonDivNonUnaryArithmeticOperator() {
        List<Operator> operators = Operator.getNonUnaryOperatorsOfKind(ARITHMETIC);
        operators.remove(MOD);
        operators.remove(DIV);
        return operators.get(RANDOM.nextInt(operators.size()));
    }

    private FieldVarType getOperandType(OpStatKind opStatKind) {
        List<FieldVarType> types = new ArrayList<>();
        switch (opStatKind) {
            case LOGICAL:
                types.add(FieldVarType.BOOLEAN);
                break;
            case ARITHMETIC:
                types = new ArrayList<>(FieldVarType.getNumericTypes());
                break;
            case BITWISE:
                types = new ArrayList<>(FieldVarType.getNumericTypes());
                types.remove(FieldVarType.FLOAT);
                types.remove(FieldVarType.DOUBLE);
                types.remove(FieldVarType.LONG);
                break;
        }
        return types.get(RANDOM.nextInt(types.size()));
    }

    private FieldVarLogger fetchOperand(MethodLogger method, OpStatKind opStatKind, Operator operator) {
        FieldVarType type = getOperandType(opStatKind);
        return this.getClazzLogger().getGlobalOrLocalVarInitializedOfTypeUsableInMethod(method, type);
    }

}