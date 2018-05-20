import cli.ControlValueParser;
import cli.GenerationController;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGenerationMethods extends TestGenerator {

    private String[] args = new String[]{
            "-l", "10", "-f", "20", "-lv", "100", "-ga", "40", "-la", "60", "-m", "50", "-mc", "100",
            "-ml", "3", "-mp", "7", "-mo", "100", "-p", "5", "jlm", "100", "-cf", "30", "-cl", "3", "-cd", "3"};
    private ControlValueParser parser = new ControlValueParser(args);
    private GenerationController controller = parser.parse();

//    @Test
//    void testSetLocalVariableToReturnValue() {
//        FieldVarGenerator fv_generator = new FieldVarGenerator("testSetLocalVariableToReturnValue");
//        MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
//        MethodLogger main = fv_generator.getClazzLogger().getMain();
//        assertEquals(fv_generator.generateLocalVariable("x", FieldVarType.Int, main, "3"), true);
//        FieldVarLogger x = main.getVariable("x");
//        MethodLogger calledMethod = m_generator.generateMethod("testMethod", FieldVarType.Int, new FieldVarType[0], Modifier.STATIC);
//        assertEquals(m_generator.setFieldVarToReturnValue(x, calledMethod, fv_generator.getClazzLogger().getMain()), true);
//        assertEquals(fv_generator.generatePrintStatement(x, main), true);
//        m_generator.writeFile();
//        //TODO run file and check for expected output
//    }
//
//    @Test
//    void testSetGlobalVariableToReturnValue() {
//        FieldVarGenerator fv_generator = new FieldVarGenerator("testSetGlobalVariableToReturnValue");
//        MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
//        MethodLogger main = fv_generator.getClazzLogger().getMain();
//        assertEquals(fv_generator.generateField("x", FieldVarType.Int, Modifier.STATIC, null), true);
//        FieldVarLogger x = fv_generator.getClazzLogger().getVariable("x");
//        MethodLogger calledMethod = m_generator.generateMethod("testMethod", FieldVarType.Int, new FieldVarType[0], Modifier.STATIC);
//        assertEquals(m_generator.setFieldVarToReturnValue(x, calledMethod, fv_generator.getClazzLogger().getMain()), true);
//        assertEquals(fv_generator.generatePrintStatement(x, main), true);
//        m_generator.writeFile();
//    }


//    /**
//     * shows that javasist cannot cope with with assigning the return value of a method to a non initialized local
//     * variable in an if-statement. But this only leads to trouble if there are more than one such if-statements
//     * This for instance does not happen with normal value assignments, only if methods are called.
//     * Solved this issue, by only using already initialized local variables to assign return values
//     *
//     * @throws IOException
//     * @throws InterruptedException
//     * @throws CannotCompileException
//     */
//    @Test
////    void setNonInitializedLocalVariableToReturnValueInControlContext() throws IOException, InterruptedException {
////        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(
////                "setNonInitializedLocalVariableToReturnValueInControlContext", controller);
////        ClazzFileContainer container = randomCodeGenerator.getClazzFileContainer();
////        MethodGenerator methodGenerator = new MethodGenerator(randomCodeGenerator);
////        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
////
////        FieldVarType[] paramTypes = new FieldVarType[]{FieldVarType.Char};
////        MethodLogger method = methodGenerator.generateMethod("method2", FieldVarType.Int, paramTypes, 0);
////
////        for (int i = 0; i < 5; i++) {
////            MethodLogger methodToCall = methodGenerator.generateRandomMethod(3);
////            fieldVarGenerator.generateRandomLocalVariable(method);
////            ControlFlowGenerator2 controlFlowGenerator = new ControlFlowGenerator2(randomCodeGenerator);
////            controlFlowGenerator.openIfStatement(methodToCall);
////            String src = methodGenerator.srcSetRandomLocalVarToReturnValue(method);
////            if (src != null) controlFlowGenerator.addCodeToControlSrc(src);
////            controlFlowGenerator.closeIfWhileStatement();
////            controlFlowGenerator.insertControlSrcIntoMethod(method);
////        }
////
////        fieldVarGenerator.writeFile("src/test/generated_test_files");
////        assertEquals(true, executeAndDeleteFile("setNonInitializedLocalVariableToReturnValueInControlContext"));
////    }

//    @Test
//    void javassistBugSetNonInitLocalVarToRetInIf() throws IOException, InterruptedException, NotFoundException, CannotCompileException {
//        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(
//                "javassistBugSetNonInitLocalVarToRetInIf", controller);
//        ClazzFileContainer container = randomCodeGenerator.getClazzFileContainer();
//        MethodGenerator methodGenerator = new MethodGenerator(randomCodeGenerator);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//
//        FieldVarType[] paramTypes = new FieldVarType[]{FieldVarType.Char};
//        MethodLogger method = methodGenerator.generateMethod("method", FieldVarType.Double, paramTypes, 0);
//
//
//        fieldVarGenerator.generateLocalVariable("x", FieldVarType.Double, method, null);
//        fieldVarGenerator.generateLocalVariable("y", FieldVarType.Double, method, null);
//        //fieldVarGenerator.generateLocalVariable("z", FieldVarType.Double, method, null);
//
//        paramTypes = new FieldVarType[]{FieldVarType.Double, FieldVarType.String};
//
//        methodGenerator.generateMethod("method2", FieldVarType.Double, paramTypes, 0);
//        methodGenerator.generateMethod("method3", FieldVarType.Double, paramTypes, 0);
//
//        CtMethod m = container.getClazzFile().getDeclaredMethod(method.getName());
//
//        m.insertAfter("if(true) {x = method2(14853D, \"2385jlksdf\");}if(true) {x = method3(34698D, \"jklsdf\"); " +
//                "if(true)x = method2(345986D, \"349058klsdf\") + method3(34634D, \"jkkjgdlfkjgf\");}");
//
//        m.insertAfter("if(true) {y = method2(14853D, \"2385jlksdf\");}if(true) {y = method3(34698D, \"jklsdf\"); " +
//                "if(true)y = method2(345986D, \"349058klsdf\") + method3(34634D, \"jkkjgdlfkjgf\");}");
//
//
//        container.getClazzFile().writeFile("src/test/generated_test_files");
//        assertEquals(true, executeAndDeleteFile("javassistBugSetNonInitLocalVarToRetInIf"));
//    }
}
