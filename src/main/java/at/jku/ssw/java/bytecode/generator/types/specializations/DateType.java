package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Defines the specialized meta type for {@link java.util.Date}.
 */
public class DateType extends RefType<Date> {

    /**
     * Singleton.
     */
    public static final DateType DATE = new DateType();

    /**
     * Creates the date type.
     */
    private DateType() {
        super(Date.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<Date>> builders() {
        final DateType self = this;

        return Arrays.asList(
                new NullBuilder<>(self),
                // new Date(long)
                new Builder<Date>() {
                    @Override
                    public List<? extends MetaType<?>> requires() {
                        return Collections.singletonList(PrimitiveType.LONG);
                    }

                    @Override
                    public DateType returns() {
                        return self;
                    }

                    @Override
                    public Expression<Date> build(List<Expression<?>> params) {
                        return new ConstructorCall<>(self, params);
                    }
                }
        );
    }
}
