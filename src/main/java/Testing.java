//import generator.FieldGenerator;
//import javassist.*;
//import utils.FieldVarType;
//import utils.RandomSupplier;
//
//
//public class Testing {
//
//    public static void main(String[] args) throws CannotCompileException {
//
//        //TODO handle UserInput Parameters: Probabilities, filename, optionals
//
//        FieldVarGenerator fld_generator = new FieldVarGenerator("MyTestClazz");
//
//        //generate global field
//        int[] modifier = {Modifier.STATIC};
//        boolean gf = fld_generator.generateField("x", FieldVarType.Char, modifier, 'c');
//        boolean sf = fld_generator.generateField("z", FieldVarType.String, modifier, "Hallo");
//        boolean sf2 = fld_generator.generateField("a", FieldVarType.String, modifier, null);
//
//        //generate local field
//        Object v = RandomSupplier.getValue(FieldVarType.Char);
//        System.out.println(v);
//        boolean lf = fld_generator.generateLocalVariable("y", FieldVarType.Char, "main", v);
//        boolean lp = fld_generator.generateLocalVariable("m", FieldVarType.Int, "main");
//
////        fld_generator.setFieldValue("x", false, "main");
//        fld_generator.setLocalVariableValue(fld_generator.getClazzLogger().getVariable("y", "main"), 'k', "main");
////        fld_generator.setFieldValue("a", "Hey", "main");
//
//        fld_generator.generatePrintFieldStatement("x", "main");
//        fld_generator.generatePrintFieldStatement("z", "main");
//        fld_generator.generatePrintFieldStatement("a", "main");
//        fld_generator.generatePrintLocalVariableStatement("y", "main");
//
//
//        //fld_generator.getMain().insertAfter("System.out.println(k);");
//
//        //generate field access
////        for (FieldVarLogger f : fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
////            fld_generator.generatePrintLocalVariableStatement(f.getName(), "main");
////        }
//
//        //write File
//        fld_generator.writeFile();
////
////        for(FieldVarLogger f: fld_generator.getClazzContainer().getClazzLogger().getVariables()) {
////            System.out.println(f.getName());
////        }
//
//    }
//}
//
////TODO make simple arithmetik operations and field access
////TODO randomize file with FieldVarGenerator
//
