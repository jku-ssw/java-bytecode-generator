import generator.FieldGenerator;
import generator.MethodGenerator;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;
import utils.FieldVarContainer;
import utils.FieldVarType;
import utils.ProbabilityParser;
import utils.RandomSupplier;

import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) throws CannotCompileException {
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
                FieldVarType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fld_generator.generateField(rs.getVarName(), ft, value, rs.getModifiers());
            }

            //generate a new variable in Main method with random type and value (may also be null)
            if (r <= parser.getVariableProbability()) {
                FieldVarType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fld_generator.generateLocalVariable(rs.getVarName(), ft, value, "main");
            }
        }

        for (int i = 0; i < 10; i++) {
            int r = 1 + random.nextInt(100);
            //randomly assign a value to an existing variable
            if (r <= parser.getGlobalAssignProbability()) {
                if (fld_generator.getClazzLogger().noVariables()) continue;
                FieldVarContainer f = fld_generator.getClazzLogger().getRandomField();
                fld_generator.setFieldValue(f, RandomSupplier.getValue(f.getType()), "main");
            }

            if (r <= parser.getLocalAssignProbability()) {
                if (fld_generator.getClazzLogger().noLocals("main")) continue;
                FieldVarContainer f = fld_generator.getClazzLogger().getRandomVariable("main");
                fld_generator.setLocalVariableValue(f, RandomSupplier.getValue(f.getType()), "main");
            }
        }

        for (int i = 0; i < 20; i++) {
            int r = 1 + random.nextInt(100);
            if (r <= parser.getVariableToVariableAssignProbability()) {
                if (r <= parser.getGlobalAssignProbability()) {
                    if (random.nextBoolean()) { //assign value of a field to another field
                        FieldVarContainer f1 = fld_generator.getClazzLogger().getRandomField();
                        if(f1 == null || f1.isFinal()) continue;
                        FieldVarContainer f2 = fld_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
                        if (f2 != null && f2.isInitialized()) {
                            fld_generator.assignFieldToField(f1, f2, "main");
                        }
                    }
                } else { //assign value of a variable to a field
                    FieldVarContainer f1 = fld_generator.getClazzLogger().getRandomField();
                    if(f1 == null || f1.isFinal()) continue;
                    FieldVarContainer f2 = fld_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignVarToField(f1, f2, "main");
                    }
                }
            }

            if (r <= parser.getLocalAssignProbability()) {
                if (random.nextBoolean()) { //assign value of a variable to another variable
                    FieldVarContainer f1 = fld_generator.getClazzLogger().getRandomVariable("main");
                    if(f1 == null || f1.isFinal()) continue;
                    FieldVarContainer f2 = fld_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignVarToVar(f1, f2, "main");
                    }
                } else { //assign value of a field to a variable
                    FieldVarContainer f1 = fld_generator.getClazzLogger().getRandomVariable("main");
                    if(f1 == null || f1.isFinal()) continue;
                    FieldVarContainer f2 = fld_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
                    if (f2 != null && f2.isInitialized()) {
                        fld_generator.assignFieldToVar(f1, f2, "main");
                    }
                }
            }
        }

        m_generator.generateMethod("methodA" , null, rs.getModifiers());

        m_generator.getMain().insertAfter("testPrint();");
        try {
            CtMethod m = fld_generator.getClazzFile().getDeclaredMethod("testPrint");
            System.out.println(m.getName());
        } catch (NotFoundException e) {
            System.err.println("Method " + "testPrint" + " not found");
            e.printStackTrace();
        }

        for (FieldVarContainer f : fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
            fld_generator.generatePrintFieldStatement(f.getName());
        }

        for (FieldVarContainer f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
        }

        fld_generator.writeFile();
    }
}
