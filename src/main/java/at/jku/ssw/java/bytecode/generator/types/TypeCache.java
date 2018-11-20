package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import at.jku.ssw.java.bytecode.generator.types.specializations.DateType;
import at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;

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

    /**
     * Captures all primitive types.
     * Since they are initialized on startup, this map remains constant
     * throughout the execution.
     */
    private final Map<Class<?>, PrimitiveType<?>> primitiveTypes;

    /**
     * Captures all reference types.
     * Since "array types" are created on demand, this map only stores
     * common reference types (including those that are explicitly covered
     * by a meta type). This map may be modified when new types are registered
     * (e.g. a new class is generated).
     */
    private final Map<Class<?>, RefType<?>> refTypes;

    /**
     * Initializes the type maps.
     * It is done in the constructor as to prevent issues when the classes are
     * loaded (e.g. cross-references, null pointers on pre-initialization
     * access).
     */
    TypeCache() {
        primitiveTypes = new HashMap<>();
        refTypes = new HashMap<>();

        // register primitive types
        primitiveTypes.put(Byte.TYPE, BYTE);
        primitiveTypes.put(Short.TYPE, SHORT);
        primitiveTypes.put(Integer.TYPE, INT);
        primitiveTypes.put(Long.TYPE, LONG);
        primitiveTypes.put(Float.TYPE, FLOAT);
        primitiveTypes.put(Double.TYPE, DOUBLE);
        primitiveTypes.put(Boolean.TYPE, BOOLEAN);
        primitiveTypes.put(Character.TYPE, CHAR);

        // register reference types
        refTypes.put(Object.class, ObjectType.OBJECT);
        refTypes.put(Date.class, DateType.DATE);
        refTypes.put(String.class, StringType.STRING);

        // register boxed types
        refTypes.put(Byte.class, BoxedType.BYTE);
        refTypes.put(Short.class, BoxedType.SHORT);
        refTypes.put(Integer.class, BoxedType.INT);
        refTypes.put(Long.class, BoxedType.LONG);
        refTypes.put(Float.class, BoxedType.FLOAT);
        refTypes.put(Double.class, BoxedType.DOUBLE);
        refTypes.put(Boolean.class, BoxedType.BOOLEAN);
        refTypes.put(Character.class, BoxedType.CHAR);
    }

    /**
     * Registers a new reference type.
     *
     * @param newType The reference type that should be made available.
     * @param <T>     The actual Java type
     * @return the reference type that was registered (the same as the input)
     */
    @SuppressWarnings("unchecked")
    public final <T> RefType<T> register(RefType<T> newType) {
        logger.error("Registering type " + newType);
        final T oldType = (T) refTypes.putIfAbsent(newType.clazz(), newType);

        assert oldType == null : "Type '" + oldType + "' already registered";
        return newType;
    }

    /**
     * Looks up the registered meta type for the given Java class.
     *
     * @param clazz The class that is looked up
     * @param <T>   The parameter identifying the type
     * @return the mapped meta type for the given class or nothing if the
     * class is not registered yet
     */
    @SuppressWarnings("unchecked")
    public final <T> Optional<? extends MetaType<T>> find(Class<T> clazz) {
        MetaType<T> type = (MetaType<T>) primitiveTypes.get(clazz);

        return type == null
                ? Optional.ofNullable((MetaType<T>) refTypes.get(clazz))
                : Optional.of(type);
    }

    /**
     * Concatenates all registered primitive and reference types
     * and returns them as a {@link Stream}.
     *
     * @return a stream of all available (registered) meta types
     */
    public final Stream<? extends MetaType<?>> types() {
        return Stream
                .of(primitiveTypes, refTypes)
                .map(Map::values)
                .flatMap(Collection::stream);
    }

    /**
     * Returns all registered reference types
     *
     * @return a stream of reference types
     */
    public final Stream<RefType<?>> refTypes() {
        return refTypes.values().stream();
    }

    /**
     * Returns all registered primitive types.
     *
     * @return a stream of primitive types
     */
    public final Stream<PrimitiveType<?>> primitiveTypes() {
        return primitiveTypes.values().stream();
    }


}
