//import cli.ControlValueParser;
//import cli.GenerationController;
//import generators.ControlFlowGenerator;
//import generators.FieldVarGenerator;
//import generators.MathGenerator;
//import generators.RandomCodeGenerator;
//import logger.MethodLogger;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//public class TestControlFlowGenerator extends TestGenerator {
//
//    private static final String[] args = new String[]{
//            "-l", "15", "-f", "20", "-lv", "50", "-ga", "40", "-la", "60", "-m", "100", "-mc", "100",
//            "-ml", "3", "-mp", "7", "-mo", "0", "-p", "0", "jlm", "100", "-cf", "50", "-cl", "0", "-cd", "1", "-os", "30"};
//
//    private static ControlFlowGenerator createControlFlowGeneratorWithFieldsAndLocals(String clazzName) {
//        ControlValueParser parser = new ControlValueParser(args);
//        GenerationController controller = parser.parse();
//        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(clazzName, controller);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(randomCodeGenerator.getClazzFileContainer());
//        MathGenerator mathGenerator = new MathGenerator(randomCodeGenerator.getClazzFileContainer(), true, true);
//        MethodLogger main = randomCodeGenerator.getClazzFileContainer().getClazzLogger().getMain();
//
//        //generate some fields and variables
//        for (int i = 1; i < 20; i++) {
//            fieldVarGenerator.generateLocalVariable(main);
//            fieldVarGenerator.generateField();
//        }
//        return new ControlFlowGenerator(randomCodeGenerator, mathGenerator);
//    }
//
//    @Test
//    void generateIfElseStatement() throws IOException, InterruptedException {
//        String clazzName = "IfElseStatement";
//        ControlFlowGenerator controlFlowGenerator = createControlFlowGeneratorWithFieldsAndLocals(clazzName);
//        controlFlowGenerator.generateIfElseStatement(controlFlowGenerator.getClazzContainer().getClazzLogger().getMain());
//        controlFlowGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(clazzName));
//    }
//
//    @Test
//    void generateDoWhileStatement() throws IOException, InterruptedException {
//        String clazzName = "DoWhileStatement";
//        ControlFlowGenerator controlFlowGenerator = createControlFlowGeneratorWithFieldsAndLocals(clazzName);
//        controlFlowGenerator.generateDoWhileStatement(controlFlowGenerator.getClazzContainer().getClazzLogger().getMain());
//        controlFlowGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(clazzName));
//    }
//
//    @Test
//    void generateWhileStatement() throws IOException, InterruptedException {
//        String clazzName = "WhileStatement";
//        ControlFlowGenerator controlFlowGenerator = createControlFlowGeneratorWithFieldsAndLocals(clazzName);
//        controlFlowGenerator.generateWhileStatement(controlFlowGenerator.getClazzContainer().getClazzLogger().getMain());
//        controlFlowGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(clazzName));
//    }
//
//    @Test
//    void generateForStatement() throws IOException, InterruptedException {
//        String clazzName = "ForStatement";
//        ControlFlowGenerator controlFlowGenerator = createControlFlowGeneratorWithFieldsAndLocals(clazzName);
//        controlFlowGenerator.generateForStatement(controlFlowGenerator.getClazzContainer().getClazzLogger().getMain());
//        controlFlowGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(clazzName));
//    }
//}
//
