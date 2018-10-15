package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import javassist.CannotCompileException;
import javassist.CtMethod;

import java.util.Stack;
import java.util.logging.Logger;

import static at.jku.ssw.java.bytecode.generator.utils.Operator.OpStatKind.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Assignments.assign;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Blocks.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statements.Break;

class ControlFlowGenerator extends Generator {

    public static final Logger logger = Logger.getLogger(ControlFlowGenerator.class.getName());

    private static class Context {
        int branches = 0;
        boolean hasElse = false;
        final boolean isLoop;

        private Context(boolean isLoop) {
            this.isLoop = isLoop;
        }

        private static Context Loop() {
            return new Context(true);
        }

        private static Context If() {
            return new Context(false);
        }
    }

    private final Stack<Context> contexts = new Stack<>();
    private final StringBuilder controlSrc = new StringBuilder();
    private final int ifBranchingFactor;
    private final int maxLoopIterations;
    private final RandomCodeGenerator randomCodeGenerator;
    private final MathGenerator mathGenerator;

    public ControlFlowGenerator(RandomCodeGenerator randomCodeGenerator, MathGenerator mathGenerator) {
        super(randomCodeGenerator.getClazzFileContainer());
        this.randomCodeGenerator = randomCodeGenerator;
        this.ifBranchingFactor = randomCodeGenerator.getController().getIfBranchingFactor();
        this.maxLoopIterations = randomCodeGenerator.getController().getMaxLoopIterations();
        this.mathGenerator = mathGenerator;
    }

    private void generateIfClause(MethodLogger method) {
        controlSrc.append(If(getIfCondition(method)));
        contexts.add(Context.If());
        generateBody(method);
        controlSrc.append(BlockEnd);
        contexts.pop();
        if (contexts.empty())
            insertControlSrcIntoMethod(method);
    }

    private void generateElseClause(MethodLogger method) {
        Context context = contexts.peek();

        if (!context.isLoop && !context.hasElse) {
            context.hasElse = true;
            controlSrc.append(Else);
            generateBody(method);
        }
    }

    private void generateElseIfClause(MethodLogger method) {
        Context context = contexts.peek();

        if (!context.isLoop && !context.hasElse && context.branches < ifBranchingFactor) {
            context.branches++;
            controlSrc.append(ElseIf(getIfCondition(method)));
            generateBody(method);
        }
    }

    //==========================================IF ELSEIF ELSE==========================================================

    public void generateIfElseStatement(MethodLogger method) {
        if (contexts.empty()) {
            generateIfClause(method);
        } else {
            Randomizer.doOneOfOptions(5,
                    () -> generateElseClause(method),
                    () -> generateIfClause(method),
                    () -> generateElseIfClause(method)
            );
        }
    }

    private String getIfCondition(MethodLogger method) {
        return Randomizer.oneOf(
                LOGICAL,
                ARITHMETIC_LOGICAL,
                BITWISE_LOGICAL,
                ARITHMETIC_LOGICAL_BITWISE
        ).map(kind -> {
            boolean useNoVars = kind == ARITHMETIC_LOGICAL || kind == ARITHMETIC_LOGICAL_BITWISE;
            return mathGenerator.srcGenerateOperatorStatement(
                    method,
                    randomCodeGenerator.getController().getMaxOperators(),
                    kind,
                    useNoVars
            );
        }).map(src -> src.substring(0, src.length() - 1)).orElse("");
    }

    //=================================================DO WHILE=========================================================

    public void generateDoWhileStatement(MethodLogger method) {
        String varName = getClazzContainer().getRandomSupplier().getVarName();

        Randomizer.oneOf(
                () -> {
                    controlSrc
                            .append(Statement(assign(0).toLocalVar(int.class, varName)))
                            .append(Do)
                            .append(Statement(incr(varName)));
                    return lt(varName, randomLoopIterations());
                },
                () -> {
                    controlSrc
                            .append(Statement(assign(randomLoopIterations()).toLocalVar(int.class, varName)))
                            .append(Do)
                            .append(Statement(decr(varName)));
                    return gt(varName, 0);
                }
        ).ifPresent(condition -> {
            contexts.push(Context.Loop());
            generateBody(method);
            controlSrc.append(DoWhile(condition));
            contexts.pop();
            if (contexts.empty())
                insertControlSrcIntoMethod(method);
        });
    }

    //==================================================FOR/WHILE=======================================================

    public void generateWhileStatement(MethodLogger method) {
        String varName = getClazzContainer().getRandomSupplier().getVarName();
        Randomizer.oneOf(
                () -> controlSrc
                        .append(Statement(assign(0).toLocalVar(int.class, varName)))
                        .append(While(lt(varName, randomLoopIterations())))
                        .append(Statement(incr(varName))),
                () -> controlSrc
                        .append(Statement(assign(randomLoopIterations()).toLocalVar(int.class, varName)))
                        .append(While(gt(varName, 0)))
                        .append(Statement(decr(varName)))
        );
        contexts.push(Context.Loop());
        generateBody(method);
        controlSrc.append(BlockEnd);
        contexts.pop();
        if (contexts.empty())
            insertControlSrcIntoMethod(method);
    }

    public void generateForStatement(MethodLogger method) {
        RandomSupplier supplier = this.getClazzContainer().getRandomSupplier();
        String varName = supplier.getVarName();
        int it = randomLoopIterations();

        controlSrc.append(
                For(
                        assign(0).toLocalVar(int.class, varName),
                        lt(varName, it),
                        incr(varName)
                )
        );

        contexts.push(Context.Loop());
        generateBody(method);
        controlSrc.append(BlockEnd);
        contexts.pop();
        if (contexts.empty())
            insertControlSrcIntoMethod(method);
    }

    public void insertBreak() {
        // only generate break statements when inside some loop
        if (contexts.stream().anyMatch(c -> c.isLoop))
            controlSrc.append(Break);
    }

    //==================================================COMMON==========================================================

    private void generateBody(MethodLogger method) {
        RandomCodeGenerator.Context.CONTROL_CONTEXT.setContextMethod(method);
        randomCodeGenerator.generate(RandomCodeGenerator.Context.CONTROL_CONTEXT);
    }

    private void insertControlSrcIntoMethod(MethodLogger method) {
        CtMethod ctMethod = getCtMethod(method);
        try {
            ctMethod.insertAfter(controlSrc.toString());
            controlSrc.setLength(0);
        } catch (CannotCompileException e) {
            logger.severe(controlSrc.toString());
            throw new AssertionError(e);
        }
    }

    public void addCodeToControlSrc(String code) {
        if (contexts.empty())
            logger.severe("Cannot insert code, no open control-flow-block");
        else
            controlSrc.append(code);
    }

    private int randomLoopIterations() {
        return maxLoopIterations == 0 ? 0 : RANDOM.nextInt(maxLoopIterations);
    }

    public int getDepth() {
        return contexts.size();
    }
}
