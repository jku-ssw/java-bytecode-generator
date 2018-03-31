import generator.FieldGenerator;
import javassist.*;
import utils.FieldVarType;
import utils.RandomSupplier;


public class Testing {

    public static void main(String[] args) throws CannotCompileException {

        //TODO handle UserInput Parameters: Probabilities, filename, optionals

        FieldGenerator fld_generator = new FieldGenerator("MyTestClazz");

        //generate global field
        int[] modifier = {Modifier.STATIC};
        boolean gf = fld_generator.generateField("x", FieldVarType.Char, 'c', modifier);
        boolean sf = fld_generator.generateField("z", FieldVarType.String, "Hallo", modifier);
        boolean sf2 = fld_generator.generateField("a", FieldVarType.String, null, modifier);

        //generate local field
        Object v = RandomSupplier.getValue(FieldVarType.Char);
        System.out.println(v);
        boolean lf = fld_generator.generateLocalVariable("y", FieldVarType.Char, v, "main");
        boolean lp = fld_generator.generateLocalVariable("m", FieldVarType.Int, -1000345, "main");

//        fld_generator.setFieldValue("x", false, "main");
        fld_generator.setLocalVariableValue(fld_generator.getClazzLogger().getVariable("y", "main"), 'k', "main");
//        fld_generator.setFieldValue("a", "Hey", "main");

        fld_generator.generatePrintFieldStatement("x");
        fld_generator.generatePrintFieldStatement("z");
        fld_generator.generatePrintFieldStatement("a");
        fld_generator.generatePrintLocalVariableStatement("y", "main");


        //fld_generator.getMain().insertAfter("System.out.println(k);");

        //generate field access
//        for (FieldVarContainer f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
//            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
//        }

        //write File
        fld_generator.writeFile();
//
//        for(FieldVarContainer f: fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
//            System.out.println(f.getName());
//        }


    }
}

//TODO make simple arithmetik operations and field access
//TODO randomize file with FieldGenerator

