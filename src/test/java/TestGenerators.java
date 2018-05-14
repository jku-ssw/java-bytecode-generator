
import generator.*;
import org.junit.jupiter.api.Test;
import utils.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestGenerators {

    private FieldVarGenerator fv_generator = new FieldVarGenerator("TestGenerators");
    private MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
    private MathGenerator math_generator = new MathGenerator(fv_generator.getClazzContainer());
    private ControlFlowGenerator cf_generator = new ControlFlowGenerator(fv_generator.getClazzContainer());

}
