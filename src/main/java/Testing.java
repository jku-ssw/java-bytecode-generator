//
//import generator.FieldVarGenerator;
//import generator.MethodGenerator;
//import javassist.*;
//import utils.FieldVarLogger;
//import utils.FieldVarType;
//import utils.RandomSupplier;
//
//
//public class Testing {
//
//    public static void main(String[] args) throws CannotCompileException {
//
//        FieldVarGenerator fv_generator = new FieldVarGenerator("MyTestClazz");
//        MethodGenerator m_generator = new MethodGenerator(fv_generator.getClazzContainer());
//        int[] modifiers = new int[]{Modifier.STATIC};
//        fv_generator.generateField("x", FieldVarType.Int, modifiers);
//        fv_generator.generateField("y", FieldVarType.Int, modifiers, RandomSupplier.getValue(FieldVarType.Int));
//        fv_generator.generateField("z", FieldVarType.Double, modifiers, RandomSupplier.getValue(FieldVarType.Double));
//        FieldVarType[] paramTypes = new FieldVarType[]{FieldVarType.Int, FieldVarType.Double};
//        m_generator.generateMethod("methodA", FieldVarType.Int, paramTypes, modifiers);
//        FieldVarLogger[] params = {fv_generator.getClazzLogger().getVariable("y"), fv_generator.getClazzLogger().getVariable("z")};
//        m_generator.setFieldToReturnValue(fv_generator.getClazzLogger().getVariable("x"), "methodA", "main", params);
//        m_generator.writeFile();
//    }
//}
//
////TODO make simple arithmetik operations and field access
////TODO randomize file with FieldVarGenerator
//
////TODO merge modifiers from Beginning
//
