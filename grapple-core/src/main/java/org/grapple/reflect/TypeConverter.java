package org.grapple.reflect;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.wrapPrimitiveTypeIfNecessary;
import static org.grapple.utils.Utils.captureFunction;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import org.grapple.utils.UnexpectedException;
import org.jooq.lambda.tuple.Tuple2;

public final class TypeConverter {

    private static final Map<Tuple2<Class<?>, Class<?>>, Function<?, ?>> defaultScalarConverters = new HashMap<>();

    private static final Map<Class<?>, Supplier<? extends Collection<Object>>> defaultCollectionSuppliers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T convertObjectToType(TypeLiteral<T> type, Object input) {
        requireNonNull(type, "type");
        if (input == null) {
            return null;
        }
        return (T) convertObject(input, type.getType());
    }

    private Object convertObject(Object input, Type toType) {
        requireNonNull(input, "input");
        requireNonNull(toType, "toType");
        if (toType instanceof Class<?>) {
            final Class<?> toClazz = wrapPrimitiveTypeIfNecessary((Class<?>) toType);
            final Class<?> fromClazz = wrapPrimitiveTypeIfNecessary(input.getClass());
            // Great! Object is of correct type
            if (toClazz.isInstance(input)) {
                return toClazz.cast(input);
            }
            final Function<?, ?> scalarConverter = defaultScalarConverters.get(new Tuple2<Class<?>, Class<?>>(fromClazz, toClazz));
            if (scalarConverter != null) {
                return toClazz.cast(captureFunction(scalarConverter, input));
            }
            if (toClazz.isArray()) {
                return convertArray(input, toClazz.getComponentType());
            }
        }
        if (toType instanceof ParameterizedType) {
            final Class<?> toClazz = ReflectUtils.getRawTypeFor(toType);
            // We can't simply translate types here, as even if we are of the correct type, the contained type may need converting
            final Supplier<? extends Collection<Object>> supplier = defaultCollectionSuppliers.get(toClazz);
            if (supplier != null) {
                return convertCollection(input, toType, supplier.get());
            }
        }
        // Otherwise, we are are unknown parameterised type ...
        throw new TypeConversionException(format("Couldn't convert: %s to %s", input, toType));
    }

    private Object convertArray(Object input, Class<?> toClazz) {
        requireNonNull(input, "input");
        requireNonNull(toClazz, "toClazz");
        final int length = Array.getLength(input);
        final Object target = Array.newInstance(toClazz, length);
        for (int i = 0; i < length; i++) {
            Array.set(target, i, convertObject(Array.get(input, i), toClazz));
        }
        return target;
    }

    private Collection<Object> convertCollection(Object input, Type toType, Collection<Object> collection) {
        requireNonNull(input, "input");
        requireNonNull(toType, "toType");
        requireNonNull(collection, "collection");
        if (!(toType instanceof ParameterizedType)) {
            throw new UnexpectedException(format("Unexpected type: %s", toType));
        }
        final ParameterizedType parameterizedType = (ParameterizedType) toType;
        if (parameterizedType.getActualTypeArguments().length != 1) {
            throw new UnexpectedException(format("Unexpected type: %s", toType));
        }
        if (!(input instanceof Collection<?>)) {
            throw new TypeConversionException(format("Couldn't convert: %s to collection", input));
        }
        for (Object inputItem: ((Collection<?>) input)) {
            collection.add(convertObject(inputItem, parameterizedType.getActualTypeArguments()[0]));
        }
        return collection;
    }

    static {
        addDefaultScalarConverter(Byte.class, Short.class, Byte::shortValue);
        addDefaultScalarConverter(Byte.class, Integer.class, Byte::intValue);
        addDefaultScalarConverter(Byte.class, Long.class, Byte::longValue);
        addDefaultScalarConverter(Byte.class, Float.class, Byte::floatValue);
        addDefaultScalarConverter(Byte.class, Double.class, Byte::doubleValue);

        addDefaultScalarConverter(Short.class, Byte.class, Short::byteValue);
        addDefaultScalarConverter(Short.class, Integer.class, Short::intValue);
        addDefaultScalarConverter(Short.class, Long.class, Short::longValue);
        addDefaultScalarConverter(Short.class, Float.class, Short::floatValue);
        addDefaultScalarConverter(Short.class, Double.class, Short::doubleValue);

        addDefaultScalarConverter(Integer.class, Byte.class, Integer::byteValue);
        addDefaultScalarConverter(Integer.class, Short.class, Integer::shortValue);
        addDefaultScalarConverter(Integer.class, Long.class, Integer::longValue);
        addDefaultScalarConverter(Integer.class, Float.class, Integer::floatValue);
        addDefaultScalarConverter(Integer.class, Double.class, Integer::doubleValue);

        addDefaultScalarConverter(Long.class, Byte.class, Long::byteValue);
        addDefaultScalarConverter(Long.class, Short.class, Long::shortValue);
        addDefaultScalarConverter(Long.class, Integer.class, Long::intValue);
        addDefaultScalarConverter(Long.class, Float.class, Long::floatValue);
        addDefaultScalarConverter(Long.class, Double.class, Long::doubleValue);

        addDefaultScalarConverter(Float.class, Byte.class, Float::byteValue);
        addDefaultScalarConverter(Float.class, Short.class, Float::shortValue);
        addDefaultScalarConverter(Float.class, Integer.class, Float::intValue);
        addDefaultScalarConverter(Float.class, Long.class, Float::longValue);
        addDefaultScalarConverter(Float.class, Double.class, Float::doubleValue);

        addDefaultScalarConverter(Double.class, Byte.class, Double::byteValue);
        addDefaultScalarConverter(Double.class, Short.class, Double::shortValue);
        addDefaultScalarConverter(Double.class, Integer.class, Double::intValue);
        addDefaultScalarConverter(Double.class, Long.class, Double::longValue);
        addDefaultScalarConverter(Double.class, Float.class, Double::floatValue);

        addDefaultCollectionSupplier(Collection.class, ArrayList::new);
        addDefaultCollectionSupplier(List.class, ArrayList::new);
        addDefaultCollectionSupplier(ArrayList.class, ArrayList::new);
        addDefaultCollectionSupplier(Stack.class, Stack::new);

        addDefaultCollectionSupplier(Set.class, HashSet::new);
        addDefaultCollectionSupplier(HashSet.class, HashSet::new);
        addDefaultCollectionSupplier(SortedSet.class, TreeSet::new);
        addDefaultCollectionSupplier(TreeSet.class, TreeSet::new);
    }

    private static <T, U> void addDefaultScalarConverter(Class<T> from, Class<U> to, Function<T, U> converter) {
        requireNonNull(from, "from");
        requireNonNull(to, "to");
        requireNonNull(converter, "converter");
        defaultScalarConverters.put(new Tuple2<>(from, to), converter);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Collection<?>> void addDefaultCollectionSupplier(Class<T> to, Supplier<T> supplier) {
        defaultCollectionSuppliers.put(to, (Supplier<? extends Collection<Object>>) supplier);
    }

}
