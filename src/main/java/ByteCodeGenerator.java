import generator.FieldGenerator;
import utils.Field;
import utils.FieldType;
import utils.ProbabilityParser;
import utils.RandomSupplier;

import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ProbabilityParser parser = new ProbabilityParser(args);
        parser.parse();

        FieldGenerator fld_generator = new FieldGenerator("MyGeneratedClazz");
        //MethodGenerator m_generator = new MethodGenerator(fld_generator.getClazzContainer());


        RandomSupplier rs = new RandomSupplier();
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            int r = 1 + random.nextInt(100);

            //generate a new field with random type and value (may also be null)
            if (r <= parser.getFieldProbability()) {
                FieldType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fld_generator.generateField(rs.getVarName(), ft, value, rs.getModifiers());
            }

            //generate a new variable in Main method with random type and value (may also be null)
            if (r <= parser.getVariableProbability()) {
                FieldType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fld_generator.generateLocalVariable(rs.getVarName(), ft, value, "main");
            }

            //randomly assign a value to an existing variable
            if (r <= parser.getGlobalAssignProbability()) {
                if (fld_generator.getClazzLogger().noVariables()) continue;
                Field f = fld_generator.getClazzLogger().getRandomVariable();
                fld_generator.setFieldValue(f, RandomSupplier.getValue(f.getType()), "main");
            }

            if (r <= parser.getLocalAssignProbability()) {
                if (fld_generator.getClazzLogger().noLocals("main")) continue;
                Field f = fld_generator.getClazzLogger().getRandomVariable("main");
                fld_generator.setLocalVariableValue(f, RandomSupplier.getValue(f.getType()), "main");
            }

            if (r <= parser.getVariableToVariableAssignProbability()) {
                System.out.println("here");
                if (r <= parser.getGlobalAssignProbability()) {
                    if (random.nextBoolean()) {
                        Field f1 = fld_generator.getClazzLogger().getRandomVariable();
                        Field f2 = fld_generator.getClazzLogger().getRandomVariable();
                        if (f1 != null && f2 != null) fld_generator.assignFieldToField(f1, f2, "main");
                    } else {
                        Field f1 = fld_generator.getClazzLogger().getRandomVariable();
                        Field f2 = fld_generator.getClazzLogger().getRandomVariable("main");
                        if (f1 != null && f2 != null) fld_generator.assignVarToField(f1, f2, "main");
                    }
                }

                if (r <= parser.getLocalAssignProbability()) {

                }
            }
        }

        for (Field f : fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
            fld_generator.generatePrintFieldStatement(f.getName());
        }

        for (Field f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
        }

        fld_generator.writeFile();
    }
}
