
import utils.*;

import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ProbabilityParser parser = new ProbabilityParser(args);
        parser.parse();

        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("MyGeneratedClazz");

        RandomSupplier rs = new RandomSupplier();
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            int r = 1 + random.nextInt(100);

            //generate a new field with random type and value (may also be null)
            if (r <= parser.getFieldProbability()) randomCodeGenerator.generateField();


            //generate a new variable in Main method with random type and value (may also be null)
            if (r <= parser.getVariableProbability()) randomCodeGenerator.generateLocalVariable();


            //randomly assign a value to an existing variable
            if (r <= parser.getGlobalAssignProbability()) randomCodeGenerator.setFieldValue();


            if (r <= parser.getLocalAssignProbability()) randomCodeGenerator.setLocalVariableValue();


            if (r <= parser.getVariableToVariableAssignProbability()) {
                if (r <= parser.getGlobalAssignProbability()) {
                    if (random.nextBoolean()) randomCodeGenerator.assignFieldToField();
                    else randomCodeGenerator.assignVarToField();
                }

                if (r <= parser.getLocalAssignProbability()) {
                    if (random.nextBoolean()) randomCodeGenerator.assignVarToVar();
                    else randomCodeGenerator.assignFieldToVar();
                }
            }

            if (r <= parser.getMethodProbability()) randomCodeGenerator.generateMethod();


            if (r <= parser.getMethodCallProbability()) randomCodeGenerator.generateMethodCall();
        }


//        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
//            fld_generator.generatePrintFieldStatement(f.getName(), "main");
//        }
//
//        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
//            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
//        }

        randomCodeGenerator.writeFile();
    }
}
