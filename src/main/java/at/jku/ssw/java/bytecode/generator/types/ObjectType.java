package at.jku.ssw.java.bytecode.generator.types;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized {@link Object} meta type.
 */
public final class ObjectType extends RefType<Object> {

    /**
     * {@link Object} type constant.
     */
    public static final ObjectType OBJECT = new ObjectType();

    /**
     * Creates a new object type.
     * Must not be invoked outside of this class.
     */
    private ObjectType() {
        super(Object.class);
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
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes().collect(Collectors.toList());
    }
}
