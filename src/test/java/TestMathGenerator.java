import generators.FieldVarGenerator;
import generators.MathGenerator;
import javassist.CannotCompileException;
import logger.MethodLogger;
import org.junit.jupiter.api.Test;
import utils.ClazzFileContainer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestMathGenerator extends TestGenerator {
    private String[] args = new String[]{
            "-l", "10", "-f", "20", "-lv", "100", "-ga", "40", "-la", "60", "-m", "100", "-mc", "100",
            "-ml", "3", "-mp", "7", "-mo", "0", "-p", "0", "jlm", "100", "-cf", "0", "-cl", "3", "-cd", "3"};


//    @Test
//    void testGenerateRandomMathMethodCall() throws CannotCompileException, IOException {
//        ClazzFileContainer container = new ClazzFileContainer("MathTest");
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        MethodLogger main = container.getClazzLogger().getMain();
//
//        //generate some fields and variables
//        for (int i = 1; i < 20; i++) {
//            fieldVarGenerator.generateRandomLocalVariable(main);
//            fieldVarGenerator.generateRandomField();
//        }
//
//        MathGenerator mathGenerator = new MathGenerator(container);
//        for (int i = 0; i < 10; i++) {
//            mathGenerator.generateRandomMathMethodCall(main, true);
//            mathGenerator.setRandomFieldToMathReturnValue(main, true);
//            mathGenerator.setRandomLocalVarToMathReturnValue(main, true);
//        }
//
//        mathGenerator.getClazzFile().writeFile("src/test/generated_test_files");
//        try {
//            assertEquals(true, executeAndDeleteFile("MathTest"));
//        } catch (IOException | InterruptedException e) {
//            throw new AssertionError(e);
//        }
//    }

    @Test
    void testGenerationOfOverflowMethodsAvoidingOverflows() {
        ClazzFileContainer container = new ClazzFileContainer("MathTestWithoutOverflow");
        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
        MethodLogger main = container.getClazzLogger().getMain();

        //generate some fields and variables
        for (int i = 1; i < 20; i++) {
            fieldVarGenerator.generateRandomLocalVariable(main);
            fieldVarGenerator.generateRandomField();
        }

        MathGenerator mathGenerator = new MathGenerator(container);
        for (int i = 0; i < 10; i++) {
            mathGenerator.generateRandomMathMethodCall(main, true);
            mathGenerator.setRandomFieldToMathReturnValue(main, true);
            mathGenerator.setRandomLocalVarToMathReturnValue(main, true);
        }

        mathGenerator.writeFile("src/test/generated_test_files");
        try {
            assertEquals(true, executeAndDeleteFile("MathTestWithoutOverflow"));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void testGenerationOfOverflowMethodsNotAvoidingOverflows() {
        ClazzFileContainer container = new ClazzFileContainer("MathTestWithOverflow");
        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
        MethodLogger main = container.getClazzLogger().getMain();

        //generate some fields and variables
        for (int i = 1; i < 20; i++) {
            fieldVarGenerator.generateRandomLocalVariable(main);
            fieldVarGenerator.generateRandomField();
        }

        MathGenerator mathGenerator = new MathGenerator(container);
        for (int i = 0; i < 10; i++) {
            mathGenerator.generateRandomMathMethodCall(main, false);
            mathGenerator.setRandomFieldToMathReturnValue(main, false);
            mathGenerator.setRandomLocalVarToMathReturnValue(main, false);
        }

        mathGenerator.writeFile("src/test/generated_test_files");
        try {
            assertEquals(true, executeAndDeleteFile("MathTestWithOverflow", "java.lang.ArithmeticException"));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void testGenerateOperatorStatements() {

    }


}
