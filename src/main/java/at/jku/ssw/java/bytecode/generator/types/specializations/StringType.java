package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.DefaultConstructorBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.LibMethod;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.NullBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.Collections;
import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;
import static java.util.Arrays.asList;

/**
 * Specialized {@link String} meta type.
 */
public enum StringType implements RefType<String> {

    /**
     * {@link String} type constant.
     */
    STRING;

    /**
     * The methods that are available for this type.
     */
    private final List<LibMethod<?>> methods;

    StringType() {
        methods = inferMethods();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<String> clazz() {
        return String.class;
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
    public String getHashCode(FieldVarLogger<String> variable) {
        assert variable != null;

        String name = variable.access();

        return ternary(
                notNull(name),
                method(name, "hashCode"),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        // string is only assignable from other strings
        return this == other;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return Collections.singletonList(this);
    }

    @Override
    public List<LibMethod<?>> methods() {
        return methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<String>> builders() {
        return asList(
                new NullBuilder<>(this),
                new DefaultConstructorBuilder<>(this),
                // direct initialization (i.e. String str = "...")
                new Builder<String>() {
                    @Override
                    public List<? extends MetaType<?>> requires() {
                        return Collections.singletonList(STRING);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Expression<String> build(List<Expression<?>> params) {
                        assert params.size() == 1;
                        return (Expression<String>) params.get(0);
                    }

                    @Override
                    public StringType returns() {
                        return StringType.this;
                    }
                }
        );
    }
}
