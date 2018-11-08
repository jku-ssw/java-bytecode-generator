package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.Operator;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import javassist.*;

import java.util.*;

import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.*;
import static at.jku.ssw.java.bytecode.generator.utils.Operator.*;
import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind.*;

class MathGenerator extends MethodCaller {

    private static final Set<String> NON_DETERMINISTIC_MATH_METHODS =
            Collections.singleton(
                    "java.lang.Math.random()"
            );

    private static boolean noDivByZero;
    private static boolean noOverflow;

    private final static Map<String, String> OVERFLOW_METHODS = new HashMap<>();

    private final Set<String> checkForDivByZero = new HashSet<>();
    private final Set<FieldVarLogger> incDecrementOperands = new HashSet<>();

    private static CtClass mathClazz;

    static {
        try {
            mathClazz = ClassPool.getDefault().get("java.lang.Math");
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
        OVERFLOW_METHODS.put("java.lang.Math.addExact(int,int)", "if(%2$s > 0 ? " +
                "Integer.MAX_VALUE - %2$s > %1$s : Integer.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.addExact(long,long)", "if(%2$s > 0 ? " +
                "Long.MAX_VALUE - %2$s > %1$s : Long.MIN_VALUE - %2$s < %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.decrementExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(int)", "if( %s < Integer.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.incrementExact(long)", "if( %s < Long.MAX_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(int)", "if( %s > Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.negateExact(long)", "if( %s > Long.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(int,int)", "if(%2$s > 0 ? " +
                "Integer.MAX_VALUE - %2$s < %1$s: Integer.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.subtractExact(long,long)", "if(%2$s > 0 ? " +
                "Long.MAX_VALUE - %2$s < %1$s : Long.MIN_VALUE - %2$s > %1$s) {");
        OVERFLOW_METHODS.put("java.lang.Math.toIntExact(long)",
                "if( %1$s <= Integer.MAX_VALUE && %1$s >= Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(int,int)",
                "if(%1$s == 0 || Math.abs(Integer.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,int)", "" +
                "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Integer.MIN_VALUE) {");
        OVERFLOW_METHODS.put("java.lang.Math.multiplyExact(long,long)",
                "if(%1$s == 0 || Math.abs(Long.MIN_VALUE/%1$s) > Math.abs(%2$s) && %2$s != Long.MIN_VALUE) {");
        String modDivCondition = "if(%s != 0) {";
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorDiv(long,long)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(int,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,int)", modDivCondition);
        OVERFLOW_METHODS.put("java.lang.Math.floorMod(long,long)", modDivCondition);
    }

    public MathGenerator(Random rand, ClazzFileContainer cf, boolean noOverflow, boolean noDivByZero) {
        super(rand, cf);
        MathGenerator.noOverflow = noOverflow;
        MathGenerator.noDivByZero = noDivByZero;
    }

    //===============================================CALL MATH METHODS==================================================

    public void generateMathMethodCall(MethodLogger method) {
        String callString = srcGenerateMathMethodCall(method);
        insertIntoMethodBody(method, callString);
    }

    public String srcGenerateMathMethodCall(MethodLogger method) {
        CtMethod mathMethod = getMathMethod();
        String methodName = mathMethod.getName();
        String signature = mathMethod.getSignature();
        PrimitiveType[] paramTypes = getParamTypes(signature);
        ParamWrapper[] paramValues = getClazzLogger().randomParameterValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && (noOverflow || noDivByZero)) {
            String noExceptionIf = getNoExceptionIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noExceptionIf == null) {
                return null;
            }
            return noExceptionIf + "Math." +
                    generateMethodCallString(methodName, paramTypes, paramValues) + "}";
        } else {
            return "Math." + generateMethodCallString(methodName, paramTypes, paramValues);
        }
    }

    public void setFieldToMathReturnValue(MethodLogger method) {
        String src = srcSetFieldToMathReturnValue(method);
        insertIntoMethodBody(method, src);
    }

    public String srcSetFieldToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getMathMethod();
        String signature = mathMethod.getSignature();
        PrimitiveType<?> returnType = getType(signature.charAt(signature.length() - 1));
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
        PrimitiveType[] paramTypes = getParamTypes(mathMethod.getSignature());
        ParamWrapper[] paramValues = getClazzLogger().randomParameterValues(paramTypes, method);
        if (OVERFLOW_METHODS.containsKey(mathMethod.getLongName()) && noOverflow) {
            String noOverFlowIf = getNoExceptionIf(mathMethod.getLongName(), paramValues, paramTypes);
            if (noOverFlowIf == null) return null;
            return noOverFlowIf + fieldVar.access() + " = " + "Math." +
                    generateMethodCallString(mathMethod.getName(), paramTypes, paramValues) + "}";
        } else
            return fieldVar.access() + " = (" + fieldVar.getType() + ") " + "Math." +
                    generateMethodCallString(mathMethod.getName(), paramTypes, paramValues);
    }

    public void setLocalVarToMathReturnValue(MethodLogger method) {
        String src = srcSetLocalVarToMathReturnValue(method);
        insertIntoMethodBody(method, src);
    }

    public String srcSetLocalVarToMathReturnValue(MethodLogger method) {
        CtMethod mathMethod = getMathMethod();
        String signature = mathMethod.getSignature();
        PrimitiveType<?> returnType = getType(signature.charAt(signature.length() - 1));
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

    //=============================================OPERATOR STATEMENTS==================================================

    public void generateOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcGenerateOperatorStatement(method, maxOperations, opStatKind, false);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcGenerateOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        return srcGenerateOperatorStatement(method, maxOperations, opStatKind, false);
    }

    public String srcGenerateOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind, boolean useNoVars) {
        int numberOfOperands = 2 + ((maxOperations > 1) ? rand.nextInt(maxOperations - 1) : 0);
        StringBuilder src = new StringBuilder();
        switch (opStatKind) {
            case ARITHMETIC:
            case LOGICAL:
            case BITWISE:
                src = srcGenerateOperatorStatementOfKind(method, numberOfOperands, opStatKind, useNoVars);
                break;
            case ARITHMETIC_BITWISE:
                src = arithmeticBitwiseStatement(method, numberOfOperands, useNoVars);
                break;
            case ARITHMETIC_LOGICAL:
                src = combinedWithLogicalStatement(ARITHMETIC, method, numberOfOperands, useNoVars);
                break;
            case BITWISE_LOGICAL:
                src = combinedWithLogicalStatement(BITWISE, method, numberOfOperands, useNoVars);
                break;
            case ARITHMETIC_LOGICAL_BITWISE:
                src = combinedWithLogicalStatement(ARITHMETIC_BITWISE, method, numberOfOperands, useNoVars);
        }
        if (!checkForDivByZero.isEmpty()) {
            src = addIfToOperatorStatement(src, checkForDivByZero);
            checkForDivByZero.clear();
        }
        incDecrementOperands.clear();
        return src.toString();
    }

    public void setLocalVarToOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcSetLocalVarToOperatorStatement(method, maxOperations, opStatKind);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcSetLocalVarToOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        FieldVarLogger f = fetchLocalAssignVarForOperandStatement(method, opStatKind);
        if (f == null) {
            return null;
        }
        StringBuilder src = new StringBuilder(srcGenerateOperatorStatement(method, maxOperations, opStatKind, false));
        if (src.indexOf("if") != -1) {
            src.insert(src.indexOf("{") + 1, f.access() + " = (" + f.getType() + ") (");
        } else {
            src.insert(0, f.access() + " = (" + f.getType() + ") (");
        }
        src.insert(src.indexOf(";"), ")");
        return src.toString();
    }

    public void setFieldToOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        String src = srcSetFieldToOperatorStatement(method, maxOperations, opStatKind);
        if (src != null) {
            insertIntoMethodBody(method, src);
        }
    }

    public String srcSetFieldToOperatorStatement(MethodLogger method, int maxOperations, OpStatKind opStatKind) {
        FieldVarLogger f = fetchGlobalAssignVarForOperandStatement(method, opStatKind);
        if (f == null) {
            return null;
        }
        StringBuilder src = new StringBuilder(srcGenerateOperatorStatement(method, maxOperations, opStatKind, false));
        if (src.indexOf("if") != -1) {
            src.insert(src.indexOf("{") + 1, f.access() + " = (" + f.getType() + ") (");
        } else {
            src.insert(0, f.access() + " = (" + f.getType() + ") (");
        }
        src.insert(src.indexOf(";"), ")");
        return src.toString();
    }

    //================================================UTILITY===========================================================

    private CtMethod getMathMethod() {
        CtMethod[] methods = Arrays.stream(mathClazz.getDeclaredMethods())
                .filter(m -> !NON_DETERMINISTIC_MATH_METHODS.contains(m.getLongName()))
                .filter(m -> (m.getModifiers() & Modifier.PUBLIC) == 1)
                .toArray(CtMethod[]::new);
        return methods[rand.nextInt(methods.length)];
    }

    private static String getNoExceptionIf(String longName, ParamWrapper[] paramValues, PrimitiveType[] paramTypes) {
        String[] params = new String[2];
        params[0] = paramValues[0].getParamValue().toString();
        if (paramTypes.length == 2) {
            params[1] = paramValues[1].getParamValue().toString();
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

    private static PrimitiveType<?> getType(char t) {
        switch (t) {
            case 'D':
                return DOUBLE;
            case 'I':
                return INT;
            case 'F':
                return FLOAT;
            case 'J':
                return LONG;
            default:
                throw new AssertionError("Unexpected math data type requested: " + t);
        }
    }

    private static PrimitiveType[] getParamTypes(String methodSignature) {
        List<PrimitiveType<?>> paramTypes = new ArrayList<>();
        for (int i = 1; i < methodSignature.length() - 2; i++) {
            paramTypes.add(getType(methodSignature.charAt(i)));
        }
        PrimitiveType[] paramTypesArray = new PrimitiveType[paramTypes.size()];
        return paramTypes.toArray(paramTypesArray);
    }


    private FieldVarLogger fetchLocalAssignVarForOperandStatement(MethodLogger method, OpStatKind opStatKind) {
        PrimitiveType<?> type = fetchAssignVarTypeForOperandStatement(opStatKind);
        return this.getClazzLogger().getNonFinalLocalVarOfType(method, type);
    }

    private FieldVarLogger fetchGlobalAssignVarForOperandStatement(MethodLogger method, OpStatKind opStatKind) {
        PrimitiveType<?> type = fetchAssignVarTypeForOperandStatement(opStatKind);
        return this.getClazzLogger().getNonFinalFieldOfTypeUsableInMethod(method, type);
    }

    private PrimitiveType<?> fetchAssignVarTypeForOperandStatement(OpStatKind opStatKind) {
        List<PrimitiveType<?>> types = new ArrayList<>();
        switch (opStatKind) {
            case LOGICAL:
                types.add(BOOLEAN);
                break;
            case ARITHMETIC_LOGICAL:
            case BITWISE_LOGICAL:
            case ARITHMETIC_LOGICAL_BITWISE:
                types.add(BOOLEAN);
            case BITWISE:
            case ARITHMETIC_BITWISE:
            case ARITHMETIC:
                types.addAll(PrimitiveType.numeric());
        }
        return types.get(rand.nextInt(types.size()));
    }

    private StringBuilder arithmeticBitwiseStatement(MethodLogger method, int numberOfOperands, boolean useNoVars) {
        StringBuilder src = new StringBuilder();
        int maxPartitionSize = numberOfOperands / 2;
        Operator operator = null;
        while (numberOfOperands > 0) {
            int operandsInPartition = 1 + ((maxPartitionSize > 1) ? rand.nextInt(maxPartitionSize - 1) : 0);
            StringBuilder statement;
            PrimitiveType<?> type;
            if (rand.nextBoolean()) {
                statement = srcGenerateOperatorStatementOfKind(method, operandsInPartition, ARITHMETIC, useNoVars);
                operator = getNonDivNonUnaryArithmeticOperator();
            } else {
                statement = srcGenerateOperatorStatementOfKind(method, operandsInPartition, BITWISE, useNoVars);
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

    private StringBuilder combinedWithLogicalStatement(OpStatKind bitAndOrArithmetic, MethodLogger method, int numberOfOperands, boolean useNoVars) {
        StringBuilder src = new StringBuilder();
        List<Operator> relOperators = Operator.getOperatorsOfKind(RELATIONAL);
        int maxPartitionSize = numberOfOperands / 2;
        boolean openRel = false;
        Operator operator = null;
        while (numberOfOperands > 0 || openRel) {
            int operandsInPartition = 1 + ((maxPartitionSize > 1) ? rand.nextInt(maxPartitionSize - 1) : 0);
            StringBuilder statement = new StringBuilder();
            if ((rand.nextBoolean() && !openRel)) {
                statement.append("(");
                statement.append(srcGenerateOperatorStatementOfKind(method, operandsInPartition, LOGICAL, useNoVars));
                statement.replace(statement.indexOf(";"), statement.indexOf(";") + 1, ")");
                operator = getOperator(LOGICAL, true);
            } else {
                if (!openRel) {
                    src.append("(");
                }
                statement.append("(");
                if (bitAndOrArithmetic == ARITHMETIC_BITWISE) {
                    statement.append(arithmeticBitwiseStatement(method, operandsInPartition, useNoVars));
                } else {
                    statement.append(srcGenerateOperatorStatementOfKind(method, operandsInPartition, bitAndOrArithmetic, useNoVars));
                }
                openRel = !openRel;
                statement.replace(statement.indexOf(";"), statement.indexOf(";") + 1, ")");
                if (openRel) {
                    operator = relOperators.get(rand.nextInt(relOperators.size()));
                } else {
                    operator = getOperator(LOGICAL, true);
                    statement.append(")");
                }
            }
            src.append(statement);
            src.append(operator);
            numberOfOperands -= operandsInPartition;
        }
        src.delete(src.length() - operator.toString().length(), src.length());
        src.append(";");
        return src;
    }

    private StringBuilder srcGenerateOperatorStatementOfKind(MethodLogger method, int nbrOfOperands, OpStatKind opStatKind, boolean useNoVars) {
        Operator operator = null;
        StringBuilder operatorStatement = new StringBuilder();
        boolean useNonUnary;
        FieldVarLogger f = null;
        boolean addToCheckForDivByZero = false;
        for (int i = 0; i < nbrOfOperands; i++) {
            useNonUnary = false;
            if (!useNoVars) {
                f = fetchOperand(method, opStatKind);
            }
            String operand;
            PrimitiveType<?> type;
            if (f == null || (operator == DIV || operator == MOD) && incDecrementOperands.contains(f)) {
                type = getOperandType(opStatKind);
                if (type == BOOLEAN) {
                    operand = getRandomSupplier().castedValue(type);
                } else if (operator == DIV || operator == MOD) {
                    operand = getRandomSupplier().getRandomNumericValue(type, true);
                } else {
                    operand = getRandomSupplier().getRandomNumericValue(type, false);
                }

                if (!(operand.equals("true") || operand.equals("false"))) {
                    useNonUnary = true;
                }
                addToCheckForDivByZero = false;
            } else {
                operand = f.access();
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
                if (f != null && (operator == PLUS_PLUS || operator == MINUS_MINUS)) {
                    incDecrementOperands.add(f);
                    if (rand.nextBoolean()) {
                        operatorStatement.append(operand).append(operator);
                    } else {
                        operatorStatement.append(operator).append(operand);
                    }
                } else {
                    operatorStatement.append(operator).append(operand);
                }
                operator = getOperator(opStatKind, true);
                operatorStatement.append(operator);
            } else {
                operatorStatement.append(operand).append(operator);
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
        ifStatement.append(values[0]).append(UNEQ).append("0");
        for (int i = 1; i < checkForDivByZero.size(); i++) {
            ifStatement.append(COND_AND).append(values[i]).append(UNEQ).append("0");
        }
        ifStatement.append(") {");
        statement.insert(0, ifStatement);
        statement.append("}");
        return statement;
    }

    private Operator getOperator(OpStatKind opStatKind, boolean nonUnary) {
        List<Operator> operators;
        if (nonUnary) {
            operators = Operator.getNonUnaryOperatorsOfKind(opStatKind);
        } else {
            operators = Operator.getOperatorsOfKind(opStatKind);
        }
        return operators.get(rand.nextInt(operators.size()));
    }

    private Operator getNonDivNonUnaryArithmeticOperator() {
        List<Operator> operators = Operator.getNonUnaryOperatorsOfKind(ARITHMETIC);
        operators.remove(MOD);
        operators.remove(DIV);
        return operators.get(rand.nextInt(operators.size()));
    }

    private PrimitiveType<?> getOperandType(OpStatKind opStatKind) {
        List<PrimitiveType<?>> types = new ArrayList<>();
        switch (opStatKind) {
            case LOGICAL:
                types.add(BOOLEAN);
                break;
            case ARITHMETIC:
                types = new ArrayList<>(PrimitiveType.numeric());
                break;
            case BITWISE:
                types = new ArrayList<>(PrimitiveType.numeric());
                types.remove(FLOAT);
                types.remove(DOUBLE);
                types.remove(LONG);
                break;
        }
        return types.get(rand.nextInt(types.size()));
    }

    private FieldVarLogger fetchOperand(MethodLogger method, OpStatKind opStatKind) {
        PrimitiveType<?> type = getOperandType(opStatKind);
        return this.getClazzLogger().getGlobalOrLocalVarInitializedOfTypeUsableInMethod(method, type);
    }

}
