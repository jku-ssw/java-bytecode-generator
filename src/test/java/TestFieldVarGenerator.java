//import generators.FieldVarGenerator;
//import logger.MethodLogger;
//import org.junit.jupiter.api.Test;
//import utils.ClazzFileContainer;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class TestFieldVarGenerator extends TestGenerator {
//
//    @Test
//    void testFieldGeneration() throws IOException, InterruptedException {
//        String fileName = "TestFieldGeneration";
//        ClazzFileContainer container = new ClazzFileContainer(fileName);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        //add 20 random fields
//        addFieldOrVars(fieldVarGenerator, null);
//
//        fieldVarGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(fileName));
//    }
//
//    @Test
//    void tesLocalVarGeneration() throws IOException, InterruptedException {
//        String fileName = "TestLocalVarGeneration";
//        ClazzFileContainer container = new ClazzFileContainer(fileName);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        MethodLogger main = container.getClazzLogger().getMain();
//        addFieldOrVars(fieldVarGenerator, main);
//        fieldVarGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(fileName));
//    }
//
//    private void addFieldOrVars(FieldVarGenerator fieldVarGenerator, MethodLogger method) {
//        if(method == null) {
//            //add 20 random local variables
//            for (int i = 0; i < 20; i++) {
//                fieldVarGenerator.generateField();
//            }
//        } else {
//            //add 20 random local variables
//            for(int i = 0; i < 20; i++) {
//                fieldVarGenerator.generateLocalVariable(method);
//            }
//        }
//    }
//
//    @Test
//    void testPrintStatementGeneration() throws IOException, InterruptedException {
//        String fileName = "testPrintStatementGeneration";
//        ClazzFileContainer container = new ClazzFileContainer(fileName);
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        MethodLogger main = container.getClazzLogger().getMain();
//
//        //add LocalVars
//        addFieldOrVars(fieldVarGenerator, main);
//
//        //add Fields
//        addFieldOrVars(fieldVarGenerator, null);
//
//        //add 20 random local variables
//        for(int i = 0; i < 20; i++) {
//            fieldVarGenerator.generatePrintStatement(main);
//        }
//
//        fieldVarGenerator.writeFile("src/test/resources/generated_test_files");
//        assertEquals(true, executeFile(fileName));
//    }
//
//    @Test
//    void testSetFieldValue() {
//
//    }
//
//    @Test
//    void setLocalVarValue() {
//
//    }
//
//    @Test
//    void assignFieldToField() {
//
//    }
//
//    @Test
//    void assignLocalVarToField() {
//
//    }
//
//    @Test
//    void assignLOcalVarToLocalVar() {
//
//    }
//
//    @Test
//    void assignFieldToLocalVar() {
//
//    }
//
//}
