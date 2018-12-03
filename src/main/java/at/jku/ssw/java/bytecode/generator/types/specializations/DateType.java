package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.LibMethod;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.NullBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;
import static java.util.Arrays.asList;

/**
 * Defines the specialized meta type for {@link java.util.Date}.
 */
public enum DateType implements RefType<Date> {

    /**
     * Singleton.
     */
    DATE;

    /**
     * The methods that are available for this type.
     */
    private final List<LibMethod<?>> methods;

    DateType() {
        this.methods = inferMethods();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Date> clazz() {
        return Date.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<Date> variable) {
        assert variable != null;

        String name = variable.access();

        return ternary(
                notNull(name),
                method(name, "getTime"),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return descriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends MethodBuilder<?>> methods() {
        return methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<Date>> builders() {
        return asList(
                new NullBuilder<>(this),
                // new Date(long)
                new Builder<Date>() {
                    @Override
                    public List<? extends MetaType<?>> requires() {
                        return Collections.singletonList(PrimitiveType.LONG);
                    }

                    @Override
                    public DateType returns() {
                        return DateType.this;
                    }

                    @Override
                    public Expression<Date> build(List<? extends Expression<?>> params) {
                        return new ConstructorCall<>(DateType.this, params);
                    }
                }
        );
    }
}
