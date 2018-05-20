//package generators;
//
//import javassist.CannotCompileException;
//import javassist.CtMethod;
//import logger.MethodLogger;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//
//public class ControlFlowGenerator2 extends Generator {
//    private enum IfContext {
//        noContext,
//        ifContext,
//        closeIfContext,
//        elseContext,
//        elseIfContext,
//        whileContext,
//        doWhileContext,
//        forContext,
//        switchContext;
//    }
//
//
//
//    private IfContext context = IfContext.noContext;
//    private LinkedList<IfContext> openIfContexts = new LinkedList<>();
//    private StringBuilder controlSrc = new StringBuilder();
//    private int deepness = 0;
//    private RandomCodeGenerator randomCodeGenerator;
//
//    public ControlFlowGenerator2(RandomCodeGenerator randomCodeGenerator) {
//        super(randomCodeGenerator.getClazzFileContainer());
//        this.randomCodeGenerator = randomCodeGenerator;
//    }
//
//    public void generateRandomIfElseStatement(MethodLogger contextMethod) {
//        System.out.println(context);
//        if (context == IfContext.noContext || context == IfContext.elseContext) {
//            this.openIfStatement(contextMethod);
//            this.generateBody(contextMethod);
//        } else if (context == IfContext.ifContext || context == IfContext.elseIfContext) {
//            //if(random.nextBoolean()) {
//            //  int choose = random.nextInt(4);
//            //if(choose < 2) {
//            // this.openElseStatement();
//            //} else
//            this.openElseIfStatement(contextMethod);
////            } else {
////                this.openIfStatement(contextMethod);
////            }
//            this.generateBody(contextMethod);
//        }
//    }
//
//    private void openElseStatement() {
//        controlSrc.append("} else {");
//        this.context = IfContext.elseContext;
//        this.openIfContexts.add(IfContext.elseContext);
//        ++deepness;
//    }
//
//    private void openElseIfStatement(MethodLogger contextMethod) {
//        controlSrc.append("} else if(" + getRandomCondition(contextMethod) + ") {");
//        this.context = IfContext.elseIfContext;
//        this.openIfContexts.add(IfContext.elseIfContext);
//        ++deepness;
//    }
//
//    //TODO condition
//    private void openIfStatement(MethodLogger contextMethod) {
//        controlSrc.append("if(" + getRandomCondition(contextMethod) + ") {");
//        this.context = IfContext.ifContext;
//        this.openIfContexts.add(IfContext.ifContext);
//        ++deepness;
//    }
//
//    public void generateRandomWhileStatement(MethodLogger contextMethod) {
//        this.openWhileStatement(contextMethod);
//        this.generateBody(contextMethod);
//    }
//
//    //TODO condition
//    private void openWhileStatement(MethodLogger method) {
//        controlSrc.append("while(false) {");
//        this.context = IfContext.whileContext;
//        this.openIfContexts.add(IfContext.whileContext);
//        ++deepness;
//    }
//
//    private void closeWhileStatement() {
//        controlSrc.append("}");
//        this.openIfContexts.removeLast();
//        deepness--;
//        if (deepness != 0) context = this.openIfContexts.getLast();
//    }
//
//    private void closeIfStatement() {
//        controlSrc.append("}");
//        this.openIfContexts.removeLast();
//        deepness--;
//        if (deepness != 0) context = this.openIfContexts.getLast();
//    }
//
//    private void generateBody(MethodLogger contextMethod) {
//        IfContext currentContext = context;
//        RandomCodeGenerator.Context.controlContext.setContextMethod(contextMethod);
//        randomCodeGenerator.generate(RandomCodeGenerator.Context.controlContext);
//        if (currentContext == IfContext.elseIfContext || context == IfContext.elseContext) {
//            this.openIfContexts.removeLast();
//            deepness--;
//            if (context == IfContext.elseContext) context = IfContext.closeIfContext;
//            if (deepness != 0) context = this.openIfContexts.getLast();
//        } else if (currentContext == IfContext.doWhileContext) {
//            this.closeDoWhileStatement(contextMethod);
//        } else if (currentContext == IfContext.whileContext) {
//            this.closeWhileStatement();
//        } else if (context == IfContext.ifContext) {
//            this.closeIfStatement();
//        }
//        if (this.getDeepness() == 0) this.insertControlSrcIntoMethod(contextMethod);
//
//    }
//
//    public void generateRandomDoWhileStatement(MethodLogger contextMethod) {
//        this.openDoWhileStatement(contextMethod);
//        this.context = IfContext.doWhileContext;
//        this.generateBody(contextMethod);
//    }
//
//    private void openDoWhileStatement(MethodLogger method) {
//        controlSrc.append("do {");
//        this.context = IfContext.doWhileContext;
//        this.openIfContexts.add(IfContext.doWhileContext);
//        ++deepness;
//    }
//
//    //TODO condition
//    private void closeDoWhileStatement(MethodLogger method) {
//        controlSrc.append("} while(false);");
//        this.openIfContexts.removeLast();
//        deepness--;
//        if (deepness != 0) context = this.openIfContexts.getLast();
//    }
//
////    private void generateDoWhileBody(MethodLogger contextMethod) {
////        RandomCodeGenerator.Context.controlContext.setContextMethod(contextMethod);
////        randomCodeGenerator.generate(RandomCodeGenerator.Context.controlContext);
////        this.closeDoWhileStatement(contextMethod);
////        if (this.getDeepness() == 0) {
////            this.insertControlSrcIntoMethod(contextMethod);
////        }
////    }
//
//    //TODO conditions
//    private String getRandomCondition(MethodLogger method) {
//        //this.getClazzLogger();
//        return "true";
//    }
//
//
//    private boolean insertControlSrcIntoMethod(MethodLogger method) {
//        //for (int i = 0; i < deepness; i++) controlSrc.append("}");
//        CtMethod ctMethod = this.getCtMethod(method);
//        try {
//            System.out.println(controlSrc);
//            ctMethod.insertAfter(controlSrc.toString());
//            context = IfContext.noContext;
//            controlSrc = new StringBuilder();
//            return true;
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public void addCodeToControlSrc(String code) {
//        if (deepness > 0) {
//            controlSrc.append(code);
//        } else {
//            System.err.println("Cannot insert code, no open control-flow-block");
//        }
//    }
//
//    public int getDeepness() {
//        return deepness;
//    }
//
//

//}
//
//
//
//
