package utils;

import generator.FieldVarGenerator;
import generator.MethodGenerator;

import java.util.ArrayList;
import java.util.List;

public class RandomCodeGenerator {
    private FieldVarGenerator fv_generator = new FieldVarGenerator("MyGeneratedClazz");
    private MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
    private RandomSupplier randomSupplier;
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
            fv_generator.setFieldValue(f, RandomSupplier.getValue(f.getType()), "main");
        } else return;
    }

    public void setLocalVariableValue() {
        if (fv_generator.getClazzLogger().hasLocals("main")) {
            FieldVarLogger f = fv_generator.getClazzLogger().getRandomVariable("main");
            fv_generator.setLocalVariableValue(f, RandomSupplier.getValue(f.getType()), "main");
        }
    }

    public void assignFieldToField() {
        //assign value of a field to another field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignFieldToField(f1, f2, "main");
            }
        }
    }

    public void assignVarToField() {
        //assign value of a variable to a field
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomField();
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVarToField(f1, f2, "main");
            }
        }
    }

    public void assignVarToVar() {
        //assign value of a variable to another variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable("main");
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleVariable(f1.getType(), "main");
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignVarToVar(f1, f2, "main");
            }
        }
    }

    public void assignFieldToVar() {
        //assign value of a field to a variable
        FieldVarLogger f1 = fv_generator.getClazzLogger().getRandomVariable("main");
        if (f1 != null && !f1.isFinal()) {
            FieldVarLogger f2 = fv_generator.getClazzLogger().getRandomCompatibleField(f1.getType());
            if (f2 != null && f2.isInitialized()) {
                fv_generator.assignFieldToVar(f1, f2, "main");
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
            FieldVarType[] paramTypes = l.getParamsTypes();
            List<Object> values = new ArrayList<>();
            for (FieldVarType t : paramTypes) {
                FieldVarLogger fvl = fv_generator.getClazzLogger().getRandomVariableOfType(t);
                if (fvl != null) values.add(fvl);
                else values.add(RandomSupplier.getValue(t));
            }
            m_generator.generateMethodCall(l.getName(), "main", values.toArray());
        } else return;
    }

    public void writeFile() {
        fv_generator.writeFile();
    }
}
