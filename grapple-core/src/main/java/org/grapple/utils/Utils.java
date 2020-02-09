package org.grapple.utils;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.grapple.core.Validatable;
import org.jooq.lambda.tuple.Tuple3;

public final class Utils {

    private Utils() {

    }

    public static void markAsUsed(Object... values) {
        // Nothing to do here..
    }

    public static <T> T requireNonNullArgument(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> toSet(T... values) {
        if (values == null) {
            return null;
        }
        if (values.length == 0) {
            return Collections.emptySet();
        }
        if (values.length == 1) {
            return Collections.singleton(values[0]);
        }
        return new HashSet<>(Arrays.asList(values));
    }

    @SafeVarargs
    public static <T> T coalesce(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (T value: values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static String stringCoalesce(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value: values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object o) {
        return (T) o;
    }

    public static <T> T apply(T object, Consumer<? super T> consumer) {
        requireNonNull(object, "object");
        if (consumer != null) {
            consumer.accept(object);
        }
        return object;
    }

    public static <T extends Validatable> T applyAndValidate(T object, Consumer<? super T> consumer) {
        requireNonNull(object, "object");
        if (consumer != null) {
            consumer.accept(object);
        }
        object.validate();
        return object;
    }

    public static Function<Map<?, ?>, Map<String, Object>> reifyMap() {
        return input -> reifyMap(input, String.class, Object.class);
    }

    public static <K, V> Function<Map<?, ?>, Map<K, V>> reifyMap(Class<K> keyClass, Class<V> valueClass) {
        requireNonNull(keyClass, "keyClass");
        requireNonNull(valueClass, "valueClass");
        return input -> reifyMap(input, keyClass, valueClass);
    }

    public static <K, V> Map<String, Object> reifyMap(Map<?, ?> source) {
        return reifyMap(source, String.class, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> reifyMap(Map<?, ?> source, Class<K> keyClass, Class<V> valueClass) {
        requireNonNull(keyClass, "keyClass");
        requireNonNull(valueClass, "valueClass");
        if (source == null) {
            return null;
        }
        source.forEach((key, value) -> {
            keyClass.cast(key);
            valueClass.cast(value);
        });
        return (Map<K, V>) source; // Obviously safe now
    }

    public static <T, U> Function<T, U> castIfInstance(Class<U> clazz) {
        requireNonNull(clazz, "clazz");
        return value -> (clazz.isInstance(value) ? clazz.cast(value) : null);
    }

    @SuppressWarnings("unchecked")
    public static <T, U> Object captureFunction(Function<T, U> function, Object value) {
        return function.apply((T) value);
    }

    @FunctionalInterface
    public interface TupleConsumer3<X, Y, Z> {

        void accept(X x, Y y, Z z);
    }

    public static <X, Y, Z> Consumer<Tuple3<X, Y, Z>> consumeTuple(TupleConsumer3<X, Y, Z> consumer) {
        return tuple -> consumer.accept(tuple.v1, tuple.v2, tuple.v3);
    }
}
