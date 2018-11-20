package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specialized {@link String} meta type.
 */
public class StringType extends RefType<String> {

    /**
     * {@link String} type constant.
     */
    public static final RefType<String> STRING = new StringType();

    /**
     * Creates a new string type.
     * Is only invoked from within the class to initialize the singleton.
     */
    private StringType() {
        super(String.class);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<String>> builders() {
        List<Builder<String>> builders = new ArrayList<>(super.builders());

        StringType self = this;

        // direct initialization (i.e. String str = "...")
        builders.add(
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
                        return self;
                    }
                }
        );

        return builders;
    }
}
