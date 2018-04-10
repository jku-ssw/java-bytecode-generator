package generator;

import utils.FieldVarLogger;
import utils.FieldVarType;
import utils.MethodLogger;
import utils.RandomSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCodeGenerator {
    private final FieldVarGenerator fv_generator;
    private final MethodGenerator m_generator;
    private final RandomSupplier randomSupplier;
    private final Random random = new Random();
//    private ControlFlowGenerator cf_generator;
//    private MathGenerator math_generator;


    public RandomCodeGenerator(String fileName) {
        this.randomSupplier = new RandomSupplier();
        this.fv_generator = new FieldVarGenerator(fileName);
        this.m_generator = new MethodGenerator(fv_generator.getClazzContainer());
    }

    public void generateField() {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateField(randomSupplier.getVarName(), ft, randomSupplier.getModifiers(), value);
    }

    public void generateLocalVariable() {
        FieldVarType ft = randomSupplier.getFieldVarType();
        Object value = randomSupplier.getValue(ft);
        fv_generator.generateLocalVariable(randomSupplier.getVarName(), ft, "main", value);
    }

    public void setFieldValue() {
        if (fv_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomField();
            fv_generator.setFieldVarValue(f,"main", RandomSupplier.getValue(f.getType()));
        } else return;
    }

    public void setLocalVariableValue() {
        if (fv_generator.getClazzLogger().hasLocals("main")) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomVariable("main");
            fv_generator.setFieldVarValue(f, "main", RandomSupplier.getValue(f.getType()));
        }
    }

    public void assignFieldToField() {
        //assign value of a field to another field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, "main");
            }
        }
    }

    public void assignVarToField() {
        //assign value of a variable to a field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, "main");
            }
        }
    }

    public void assignVarToVar() {
        //assign value of a variable to another variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable("main");
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, "main");
            }
        }
    }

    public void assignFieldToVar() {
        //assign value of a field to a variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable("main");
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVariableToVariable(f1, f2, "main");
            }
        }
    }

    public void generateMethod() {
        m_generator.generateMethod(RandomSupplier.getMethodName(), RandomSupplier.getReturnType(),
                RandomSupplier.getFieldVarTypes(), RandomSupplier.getModifiers());
    }

    public void generateMethodCall() {
        if (fv_generator.getClazzLogger().hasMethods()) {
            MethodLogger l = fv_generator.getClazzLogger().getRandomMethod();
            if (l == null) return;
            FieldVarType[] paramTypes = l.getParamsTypes();
            List<Object> values = getParamValues(paramTypes);
            m_generator.generateMethodCall(l.getName(), "main", values.toArray());
        } else return;
    }

    public void writeFile() {
        fv_generator.writeFile();
    }

    public void generatePrintStatement() {
        if (random.nextBoolean()) { //print local Variable
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariable("main");
            if (fvl != null) fv_generator.generatePrintLocalVariableStatement(fvl.getName(), "main");
        } else { //print global Variable
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomField();
            if (fvl != null) fv_generator.generatePrintFieldStatement(fvl.getName(), "main");
        }
    }

    public void setFieldToReturnValue() {
        if (m_generator.getClazzLogger().hasVariables()) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomField();
            MethodLogger l = fv_generator.getClazzLogger().getMethodWithReturnType(f.getType());
            if (l == null) return;
            FieldVarType[] paramTypes = l.getParamsTypes();
            List<Object> values = getParamValues(paramTypes);
            m_generator.setFieldToReturnValue(f, l.getName(), "main", values.toArray());
        } else return;
    }

    private List<Object> getParamValues(FieldVarType[] paramTypes) {
        List<Object> values = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariableOfType(t);
            if (fvl != null) values.add(fvl);
            else values.add(RandomSupplier.getValue(t));
        }
        return values;
    }

    public void setLocalVariableValueToReturnValue() {
        //FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariable("main");
    }
}
