package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.List;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Specialized {@link Object} meta type.
 */
public enum ObjectType implements RefType<Object> {

    /**
     * Singleton.
     */
    OBJECT;

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Object> clazz() {
        return Object.class;
    }

    @Override
    public String toString() {
        return descriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        return other instanceof RefType<?>;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<Object> variable) {
        String name = variable.access();

        return ternary(
                notNull(name),
                method(method(method(name, "getClass"), "getSimpleName"), "hashCode"),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes().collect(Collectors.toList());
    }
}
