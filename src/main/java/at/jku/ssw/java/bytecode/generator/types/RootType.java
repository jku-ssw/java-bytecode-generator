package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class RootType extends RefType<Object> {

    public static final RootType OBJECT = new RootType();

    private RootType() {
        super(Object.class, JavassistUtils.toCtClass(Object.class), Kind.INSTANCE, null, 0, null);
    }

    @Override
    public boolean isAssignableFrom(FieldVarType<?> other) {
        return other instanceof RefType<?>;
    }

    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes().collect(Collectors.toList());
    }
}
