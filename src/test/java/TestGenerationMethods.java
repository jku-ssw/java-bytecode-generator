import generator.FieldVarGenerator;
import generator.MethodGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import utils.*;
import java.lang.reflect.Modifier;

public class TestGenerationMethods {

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
}
