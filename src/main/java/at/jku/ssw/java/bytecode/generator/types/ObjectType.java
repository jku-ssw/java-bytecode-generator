package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class ObjectType extends RefType<Object> {

    public static final ObjectType OBJECT = new ObjectType();

    private ObjectType() {
        super(Object.class, JavassistUtils.toCtClass(Object.class), Kind.INSTANCE, null, 0, null);
    }

    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        return other instanceof RefType<?>;
    }

    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes().collect(Collectors.toList());
    }
}
