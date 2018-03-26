import generator.FieldAndVarGenerator;
import utils.FieldType;
import utils.ProbabilityParser;
import utils.RndmSupplier;
import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ProbabilityParser parser = new ProbabilityParser(args);
        parser.parse();

        FieldAndVarGenerator fldV_generator = new FieldAndVarGenerator("MyGeneratedClazz");
        //MethodGenerator m_generator = new MethodGenerator(fldV_generator.getClazzContainer());


        RndmSupplier rs = new RndmSupplier();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int r= 1 + random.nextInt(100);
            if(r <= parser.getFieldProbability()) {
                FieldType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fldV_generator.generateField(rs.getVarName(), ft, value, rs.getModifiers());
            }

            if(r <= parser.getVariableProbability()) {
                FieldType ft = rs.getFieldType();
                Object value = rs.getValue(ft);
                fldV_generator.generateVariable(rs.getVarName(), ft, value, "main");
            }

        }

        fldV_generator.writeFile();
    }
}
