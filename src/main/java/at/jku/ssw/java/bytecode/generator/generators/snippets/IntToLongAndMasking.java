package at.jku.ssw.java.bytecode.generator.generators.snippets;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Assignment;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.constants.LongConstant;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.BinaryOp;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.TypeCast;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.Operator;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;

import static at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression.NOP.NOP;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;

/**
 * Generates a code snippet that takes an integer,
 * casts it to long and then masks it with 32bit.
 */
public class IntToLongAndMasking implements Snippet {

    /**
     * The bitmask to apply in order to get the "int" value.
     */
    private static final long MASK = 0xFF_FF_FF_FF;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPossible(MethodLogger method) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String generate(RandomSupplier supplier, ClazzFileContainer container, MethodLogger context) {
        Expression<Integer> intExpr =
                container.getClazzLogger().valueOf(PrimitiveType.INT, context);

        Expression<Long> longExpr = new TypeCast<>(PrimitiveType.LONG, intExpr);

        Expression<Long> maskedExpr = new BinaryOp<>(
                PrimitiveType.LONG,
                Operator.BIT_AND,
                longExpr,
                new LongConstant(MASK)
        );

        return Statement(
                container.resolver().resolve(
                        container.getClazzLogger()
                                .getNonFinalVarsUsableInMethod(context)
                                .filter(v -> v.getType() == PrimitiveType.LONG)
                                .findFirst()
                                .<Expression<?>>map(d ->
                                        new Assignment<>(
                                                (FieldVarLogger<Long>) d,
                                                maskedExpr))
                                .orElse(NOP)
                )
        );
    }
}
