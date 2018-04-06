import generator.FieldGenerator;
import generator.MethodGenerator;
import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.ProbabilityParser;
import utils.RandomSupplier;

import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ProbabilityParser parser = new ProbabilityParser(args);
        parser.parse();

        FieldGenerator fld_generator = new FieldGenerator("MyGeneratedClazz");
        MethodGenerator m_generator = new MethodGenerator(fld_generator.getClazzContainer());


        RandomSupplier rs = new RandomSupplier();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int r = 1 + random.nextInt(100);

            //generate a new field with random type and value (may also be null)
            if (r <= parser.getFieldProbability()) {
                FieldVarType ft = rs.getFieldVarType();
                Object value = rs.getValue(ft);
                fld_generator.generateField(rs.getVarName(), ft, rs.getModifiers(), value);
            }

            //generate a new variable in Main method with random type and value (may also be null)
            if (r <= parser.getVariableProbability()) {
                FieldVarType ft = rs.getFieldVarType();
                Object value = rs.getValue(ft);
                fld_generator.generateLocalVariable(rs.getVarName(), ft, "main", value);
            }


            //randomly assign a value to an existing variable
            if (r <= parser.getGlobalAssignProbability()) {
                if (fld_generator.getClazzLogger().noVariables()) continue;
                FieldVarLogger f = fld_generator.getClazzLogger().getRandomField();
                fld_generator.setFieldValue(f, RandomSupplier.getValue(f.getType()), "main");
            }

            if (r <= parser.getLocalAssignProbability()) {
                if (fld_generator.getClazzLogger().noLocals("main")) continue;
                FieldVarLogger f = fld_generator.getClazzLogger().getRandomVariable("main");
                fld_generator.setLocalVariableValue(f, RandomSupplier.getValue(f.getType()), "main");
            }


            if (r <= parser.getVariableToVariableAssignProbability()) {
                if (r <= parser.getGlobalAssignProbability()) {
                    if (random.nextBoolean()) { //assign value of a field to another field
                        FieldVarLogger f1 = fld_generator.getClazzLogger().getRandomField();
                        if (f1 == null || f1.isFinal()) continue;
                        FieldVarLogger f2 = fld_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
                        if (f2 != null && f2.isInitialized()) {
                            fld_generator.assignFieldToField(f1, f2, "main");
                        }
                    }
                } else { //assign value of a variable to a field
                    FieldVarLogger f1 = fld_generator.getClazzLogger().getRandomField();
                    if (f1 == null || f1.isFinal()) continue;
                    FieldVarLogger f2 = fld_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignVarToField(f1, f2, "main");
                    }
                }
            }

            if (r <= parser.getLocalAssignProbability()) {
                if (random.nextBoolean()) { //assign value of a variable to another variable
                    FieldVarLogger f1 = fld_generator.getClazzLogger().getRandomVariable("main");
                    if (f1 == null || f1.isFinal()) continue;
                    FieldVarLogger f2 = fld_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignVarToVar(f1, f2, "main");
                    }
                } else { //assign value of a field to a variable
                    FieldVarLogger f1 = fld_generator.getClazzLogger().getRandomVariable("main");
                    if (f1 == null || f1.isFinal()) continue;
                    FieldVarLogger f2 = fld_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignFieldToVar(f1, f2, "main");
                    }
                }
            }

            if (r <= parser.getMethodProbability()) {
                m_generator.generateMethod(RandomSupplier.getMethodName(), RandomSupplier.getReturnType(),
                        RandomSupplier.getFieldVarTypes(), RandomSupplier.getModifiers());
            }
        }


        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
            fld_generator.generatePrintFieldStatement(f.getName(), "main");
        }

        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
        }

        fld_generator.writeFile();
    }
}
