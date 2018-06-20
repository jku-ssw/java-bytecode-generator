import generators.FieldVarGenerator;
import generators.MathGenerator;
import javassist.CannotCompileException;
import logger.MethodLogger;
import org.junit.jupiter.api.Test;
import utils.ClazzFileContainer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestMathGenerator extends TestGenerator {

    private static String ARITHMETIC_EXCEPTION = "java.lang.ArithmeticException";

    @Test
    void testGenerationOfJavaLangMathMethodsAvoidingOverflows() throws CannotCompileException {
        generateMathMethodCalls("MathTestWithoutOverflow",true);
    }

    @Test
    void testGenerationOfJavaLangMathMethodsNotAvoidingOverflows() throws CannotCompileException {
        generateMathMethodCalls("MathTestWithOverflow", false);
    }

    private static MathGenerator createMathGeneratorWithFieldsAndLocals(String clazzName) {
        ClazzFileContainer container = new ClazzFileContainer(clazzName);
        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
        MethodLogger main = container.getClazzLogger().getMain();

        //generate some fields and variables
        for (int i = 1; i < 20; i++) {
            fieldVarGenerator.generateRandomLocalVariable(main);
            fieldVarGenerator.generateRandomField();
        }

        return new MathGenerator(container);
    }

    private void generateMathMethodCalls(String clazzName, boolean avoidOverflows) throws CannotCompileException {
        MathGenerator mathGenerator = createMathGeneratorWithFieldsAndLocals(clazzName);
        MethodLogger main = mathGenerator.getClazzLogger().getMain();

        //call some methods
        for (int i = 0; i < 20; i++) {
            String notAssign = mathGenerator.srcGenerateRandomMathMethodCall(main, avoidOverflows);
            if(notAssign != null) {
                mathGenerator.getCtMethod(main).insertAfter(notAssign);
            }
            String assignToField = mathGenerator.srcSetRandomFieldToMathReturnValue(main, avoidOverflows);
            if(assignToField != null) {
                mathGenerator.getCtMethod(main).insertAfter(assignToField);
            }
            String assignToLocal = mathGenerator.srcSetRandomLocalVarToMathReturnValue(main, avoidOverflows);
            if(assignToLocal != null) {
                mathGenerator.getCtMethod(main).insertAfter(assignToLocal);
            }
            System.out.println(notAssign + "\n" + assignToField + "\n" + assignToLocal);
        }

        mathGenerator.writeFile("src/test/generated_test_files");
        try {
            if(avoidOverflows) {
                assertEquals(true, executeAndDeleteFile(clazzName));
            } else {
                assertEquals(true, executeAndDeleteFile(clazzName, ARITHMETIC_EXCEPTION));
            }
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void testGenerateArithmeticOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("ArithmeticOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC, true);
    }

    @Test
    void testGenerateBitwiseOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("BitwiseOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.BITWISE, true);
    }

    @Test
    void testGenerateLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("LogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.LOGICAL, true);
    }

    @Test
    void testGenerateArithmeticLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("ArithmeticLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL, true);
    }

    @Test
    void testGenerateBitwiseLogicalOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("BitwiseLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.BITWISE_LOGICAL, true);
    }

    @Test
    void testGenerateArithmeticLogicalBitwiseOperatorStatementsAvoidDivByZero() throws CannotCompileException {
        testGenerateOperatorStatements("ArithmeticBitwiseLogicalOperatorStatementsAvoidDivByZero", MathGenerator.OpStatKind.ARITHMETIC_LOGICAL_BITWISE, true);
    }

    private void testGenerateOperatorStatements(String clazzName, MathGenerator.OpStatKind opStatKind, boolean avoidDivByZero) throws CannotCompileException {
        MathGenerator mathGenerator = createMathGeneratorWithFieldsAndLocals(clazzName);
        MethodLogger main = mathGenerator.getClazzLogger().getMain();

        for (int i = 0; i < 20; i++) {
            String notAssign = mathGenerator.srcGenerateRandomOperatorStatement(main, 20, opStatKind, avoidDivByZero);
            if(notAssign != null) {
                mathGenerator.getCtMethod(main).insertAfter(notAssign);
            }
            String assignToField = mathGenerator.srcGenerateRandomOperatorStatementToField(main, 20, opStatKind, avoidDivByZero);
            if(assignToField != null) {
                mathGenerator.getCtMethod(main).insertAfter(assignToField);
            }
            String assignToLocal = mathGenerator.srcGenerateRandomOperatorStatementToLocal(main, 20, opStatKind, avoidDivByZero);
            if(assignToLocal != null) {
                mathGenerator.getCtMethod(main).insertAfter(assignToLocal);
            }
            System.out.println(notAssign + "\n" + assignToField + "\n" + assignToLocal);
        }
    }


}
