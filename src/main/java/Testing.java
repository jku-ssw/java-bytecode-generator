//import generators.FieldVarGenerator;
//import javassist.*;
//import utils.ClazzFileContainer;
//import utils.FieldVarType;
//
//
//public class Testing {
//
//    public static void main(String[] args) throws CannotCompileException {
//        ClazzFileContainer container = new ClazzFileContainer("MyTestingClazz");
//        FieldVarGenerator fieldVarGenerator = new FieldVarGenerator(container);
//        fieldVarGenerator.generateLocalVariable("x", FieldVarType.Int, fieldVarGenerator.getClazzLogger().getMain(), null);
//        fieldVarGenerator.generateLocalVariable("y", FieldVarType.String, fieldVarGenerator.getClazzLogger().getMain(), null);
//        fieldVarGenerator.generateLocalVariable("j", FieldVarType.String, fieldVarGenerator.getClazzLogger().getMain(), null);
//        fieldVarGenerator.generateField("z", FieldVarType.String, Modifier.STATIC, null);
//        CtClass myClazz = container.getClazzFile();
//        try {
//            CtMethod main = myClazz.getDeclaredMethod("main");
//            //main.insertAfter("int i; i = 3;i = i + 3;");
//            main.insertAfter("if(true){ if(true) {int i = x; j = \"Hallo\"; y = j; z = j;}}");
//            //main.insertAfter("System.out.println(x);");
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }
//        fieldVarGenerator.writeFile();
//
////       ClazzFileContainer container = new ClazzFileContainer("MyTestingClazz");
////       CtClass myClazz = container.getClazzFile();
////        try {
////            CtMethod main = myClazz.getDeclaredMethod("main");
////            main.insertAfter("if(true){if(true){}}");
////            //main.insertAfter("System.out.println(x);");
////        } catch (NotFoundException e) {
////            e.printStackTrace();
////        }
////        try {
////            myClazz.writeFile();
////        } catch (NotFoundException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        FieldVarGenerator fvgenerator = new FieldVarGenerator("TestStringException");
////        fvgenerator.generateLocalVariable("x", FieldVarType.String, fvgenerator.getClazzLogger().getMain(), RandomSupplier.getRandomValueAsString(FieldVarType.String));
////        fvgenerator.generateLocalVariable("y", FieldVarType.String, fvgenerator.getClazzLogger().getMain(), RandomSupplier.getRandomValueAsString(FieldVarType.String));
////        fvgenerator.assignVariableToVariable(fvgenerator.getClazzLogger().getMain().getVariable("x"), fvgenerator.getClazzLogger().getMain().getVariable("y"), fvgenerator.getClazzLogger().getMain());
//    }
//}
//
//
