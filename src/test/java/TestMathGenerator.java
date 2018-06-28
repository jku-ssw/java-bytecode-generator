//import generators.FieldVarGenerator;
//import generators.MathGenerator;
//import javassist.CannotCompileException;
//import logger.MethodLogger;
//import org.junit.jupiter.api.Test;
//import utils.ClazzFileContainer;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//public class TestMathGenerator extends TestGenerator {
//
//    private static String ARITHMETIC_EXCEPTIONS = "java.lang.ArithmeticException";
//
//    @Test
//    void testGenerationOfJavaLangMathMethodsAvoidOverflows() throws CannotCompileException {
//        generateMathMethodCalls("MathTestWithoutOverflow", true, true);
//    }
//
//    @Test
//    void testGenerationOfJavaLangMathMethodsNotAvoidOverflows() throws CannotCompileException {
//        generateMathMethodCalls("MathTestWithOverflow", false, false);
//    }
//
//    private static MathGenerator createMathGeneratorWithFieldsAndLocals(String clazzName, boolean noOverflows, boolean noDivByZero) {
//        ClazzFileContainer container = new ClazzFileContainer(clazzName);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        MethodLogger main = container.getClazzLogger().getMain();
//
//        //generate some fields and variables
//        for (int i = 1; i < 20; i++) {
//            fieldVarGenerator.generateLocalVariable(main);
//            fieldVarGenerator.generateField();
//        }
//
//        return new MathGenerator(container, noOverflows, noDivByZero);
//    }
//
//    private void generateMathMethodCalls(String clazzName, boolean noOverflows, boolean noDivByZero) throws CannotCompileException {
//        MathGenerator mathGenerator = createMathGeneratorWithFieldsAndLocals(clazzName, noOverflows, noDivByZero);
//        MethodLogger main = mathGenerator.getClazzLogger().getMain();
//
//        //call some methods
//        for (int i = 0; i < 10; i++) {
//            String notAssign = mathGenerator.srcGenerateMathMethodCall(main);
//            if (notAssign != null) {
//                mathGenerator.getCtMethod(main).insertAfter(notAssign);
//            }
//            String assignToField = mathGenerator.srcSetFieldToMathReturnValue(main);
//            if (assignToField != null) {
//                mathGenerator.getCtMethod(main).insertAfter(assignToField);
//            }
//            String assignToLocal = mathGenerator.srcSetLocalVarToMathReturnValue(main);
//            if (assignToLocal != null) {
//                mathGenerator.getCtMethod(main).insertAfter(assignToLocal);
//            }
//            System.out.println(notAssign + "\n" + assignToField + "\n" + assignToLocal);
//        }
//
//        mathGenerator.writeFile("src/test/resources/generated_test_files");
//        execute(clazzName, noOverflows, ARITHMETIC_EXCEPTIONS);
//    }
//
//    //avoid Divide by zero
//    @Test
//    void testGenerateArithmeticOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC, true);
//    }
//
//    @Test
//    void testGenerateBitwiseOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("BitwiseOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.BITWISE, true);
//    }
//
//    @Test
//    void testGenerateLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("LogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.LOGICAL, true);
//    }
//
//    @Test
//    void testGenerateArithmeticBitwiseOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticBitwiseOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_BITWISE, true);
//    }
//
//    @Test
//    void testGenerateArithmeticLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL, true);
//    }
//
//    @Test
//    void testGenerateBitwiseLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("BitwiseLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.BITWISE_LOGICAL, true);
//    }
//
//    @Test
//    void testGenerateArithmeticLogicalBitwiseOperatorStatementsAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticBitwiseLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL_BITWISE, true);
//    }
//
//    //not avoid Divide by zero
//    @Test
//    void testGenerateArithmeticOperatorStatementsNotAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticOperatorStatementsNotAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC, false);
//    }
//
//    @Test
//    void testGenerateArithmeticBitwiseOperatorStatementsNotAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticBitwiseOperatorStatementsNotAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_BITWISE, false);
//    }
//
//    @Test
//    void testGenerateArithmeticLogicalOperatorStatementsNotAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticLogicalOperatorStatementsNotAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL, false);
//    }
//
//    @Test
//    void testGenerateArithmeticLogicalBitwiseOperatorStatementsNotAvoidDivByZero() throws CannotCompileException {
//        testGenerateOperatorStatements("ArithmeticBitwiseLogicalOperatorStatementsNotAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL_BITWISE, false);
//    }
//
//    private void testGenerateOperatorStatements(String clazzName, MathGenerator.OpStatKind opStatKind, boolean noDivByZero) throws CannotCompileException {
//        MathGenerator mathGenerator = createMathGeneratorWithFieldsAndLocals(clazzName, true, noDivByZero);
//        MethodLogger main = mathGenerator.getClazzLogger().getMain();
//
//        for (int i = 0; i < 10; i++) {
//            String notAssign = mathGenerator.srcGenerateOperatorStatement(main, 1, opStatKind);
//            if (notAssign != null) {
//                mathGenerator.getCtMethod(main).insertAfter(notAssign);
//            }
//            String assignToField = mathGenerator.srcSetFieldToOperatorStatement(main, 1, opStatKind);
//            if (assignToField != null) {
//                mathGenerator.getCtMethod(main).insertAfter(assignToField);
//            }
//            String assignToLocal = mathGenerator.srcSetLocalVarToOperatorStatement(main, 1, opStatKind);
//            if (assignToLocal != null) {
//                mathGenerator.getCtMethod(main).insertAfter(assignToLocal);
//            }
//            System.out.println(notAssign + "\n" + assignToField + "\n" + assignToLocal);
//        }
//        mathGenerator.writeFile("src/test/resources/generated_test_files");
//        execute(clazzName, noDivByZero, ARITHMETIC_EXCEPTIONS);
//    }
//
//    private void execute(String clazzName, boolean avoidException, String... excludedExceptions) {
//        try {
//            if (avoidException) {
//                assertEquals(true, executeFile(clazzName));
//            } else {
//                assertEquals(true, executeFile(clazzName, excludedExceptions));
//            }
//        } catch (IOException | InterruptedException e) {
//            throw new AssertionError(e);
//        }
//    }
//}
