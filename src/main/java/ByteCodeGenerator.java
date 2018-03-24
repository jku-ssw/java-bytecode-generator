import generator.FieldAndVarGenerator;
import generator.MethodGenerator;
import utils.ProbabilityParser;

public class ByteCodeGenerator {

    public static void main(String[] args) {
        ProbabilityParser p = new ProbabilityParser(args);
        int [] probability_values = p.parse();

        FieldAndVarGenerator fldV_generator = new FieldAndVarGenerator("MyGeneratedClazz");

        MethodGenerator m_generator = new MethodGenerator(fldV_generator.getClazzContainer());




    }
}
