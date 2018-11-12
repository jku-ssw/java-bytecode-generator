package at.jku.ssw.java.bytecode.generator.types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.PrimitiveType.*;

/**
 * Container that keeps track of all registered and available types in the
 * current run.
 */
public enum TypeCache {

    /**
     * Singleton instance.
     */
    INSTANCE;

    public static final Logger logger = LogManager.getLogger();

    private Map<Class<?>, PrimitiveType<?>> primitiveTypes = new HashMap<>();
    private Map<Class<?>, RefType<?>> refTypes = new HashMap<>();

    TypeCache() {
        primitiveTypes.put(byte.class, BYTE);
        primitiveTypes.put(short.class, SHORT);
        primitiveTypes.put(int.class, INT);
        primitiveTypes.put(long.class, LONG);
        primitiveTypes.put(float.class, FLOAT);
        primitiveTypes.put(double.class, DOUBLE);
        primitiveTypes.put(boolean.class, BOOLEAN);
        primitiveTypes.put(char.class, CHAR);

        refTypes.put(Object.class, ObjectType.OBJECT);
        refTypes.put(Date.class, RefType.DATE);
        refTypes.put(String.class, RefType.STRING);
    }

    @SuppressWarnings("unchecked")
    public final <T> RefType<T> register(RefType<T> newType) {
        logger.error("Registering type " + newType);
        final T oldType = (T) refTypes.putIfAbsent(newType.getClazz(), newType);

        assert oldType == null : "Type '" + oldType + "' already registered";
        return newType;
    }

    @SuppressWarnings("unchecked")
    public final <T> Optional<? extends FieldVarType<T>> find(Class<T> clazz) {
        FieldVarType<T> type =
                (FieldVarType<T>) primitiveTypes.get(clazz);

        return type == null
                ? Optional.ofNullable((FieldVarType<T>) refTypes.get(clazz))
                : Optional.of(type);
    }

    public final Stream<? extends FieldVarType<?>> types() {
        return Stream
                .of(primitiveTypes, refTypes)
                .map(Map::values)
                .flatMap(Collection::stream);
    }

    public final Stream<RefType<?>> refTypes() {
        return refTypes.values().stream();
    }

    public final Stream<PrimitiveType<?>> primitiveTypes() {
        return primitiveTypes.values().stream();
    }


}
