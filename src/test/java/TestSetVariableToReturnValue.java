import generator.FieldVarGenerator;
import generator.MethodGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.MethodLogger;

import java.lang.reflect.Modifier;

public class TestSetVariableToReturnValue {

    private String[] args;
    //this.args = new String[]{"-l  40", "-f 20", "-v 40", "-ga 100", "-la 100", "-vtv 0", "-m 20", "-mc 0", " -ml 1", "-mp 10", "-p 0", "-mo 10"};

    @Test
    void testSetLocalVariableToReturnValue() {
        FieldVarGenerator fv_generator = new FieldVarGenerator("testSetLocalVariableToReturnValue");
        MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
        MethodLogger main = fv_generator.getClazzLogger().getMain();
        assertEquals(fv_generator.generateLocalVariable("x", FieldVarType.Int, main), true);
        FieldVarLogger x = main.getVariable("x");
        MethodLogger calledMethod = m_generator.generateMethod("testMethod", FieldVarType.Int, new FieldVarType[0], Modifier.STATIC);
        assertEquals(m_generator.setFieldVarToReturnValue(x, calledMethod, fv_generator.getClazzLogger().getMain()), true);
        assertEquals(fv_generator.generatePrintLocalVariableStatement(x, main), true);
        m_generator.writeFile();
        //TODO run file and check for expected output
    }

    @Test
    void testSetGlobalVariableToReturnValue() {
        FieldVarGenerator fv_generator = new FieldVarGenerator("testSetGlobalVariableToReturnValue");
        MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
        MethodLogger main = fv_generator.getClazzLogger().getMain();
        assertEquals(fv_generator.generateField("x", FieldVarType.Int, Modifier.STATIC), true);
        FieldVarLogger x = fv_generator.getClazzLogger().getVariable("x");
        MethodLogger calledMethod = m_generator.generateMethod("testMethod", FieldVarType.Int, new FieldVarType[0], Modifier.STATIC);
        assertEquals(m_generator.setFieldVarToReturnValue(x, calledMethod, fv_generator.getClazzLogger().getMain()), true);
        assertEquals(fv_generator.generatePrintLocalVariableStatement(x, main), true);
        m_generator.writeFile();
    }
}
