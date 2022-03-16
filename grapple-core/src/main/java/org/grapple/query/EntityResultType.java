package org.grapple.query;

import static java.lang.String.format;
import static org.grapple.reflect.ClassLiteral.classLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.UnexpectedException;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.tuple.Tuple2;

public final class EntityResultType<T> {

    private final TypeLiteral<T> type;

    private final boolean nullAllowed;

    private static final Map<Tuple2<TypeLiteral<?>, Boolean>, EntityResultType<?>> instanceCache = new NoDuplicatesMap<>();

    private EntityResultType(@NotNull TypeLiteral<T> type, boolean nullAllowed) {
        if (type.isPrimitiveType() && nullAllowed)  {
            throw new UnexpectedException(format("Cannot have null primitive: %s", type));
        }
        this.type = type;
        this.nullAllowed = nullAllowed;
    }

    public TypeLiteral<T> getType() {
        return type;
    }

    public boolean isNullAllowed() {
        return nullAllowed;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof EntityResultType<?> && equals((EntityResultType<?>) other));
    }

    public boolean equals(EntityResultType<?> other) {
        return (other != null) && type.equals(other.type) && (nullAllowed == other.nullAllowed);
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ (nullAllowed ? 1 : 0);
    }

    @Override
    public String toString() {
        return format("%s%s", type, (nullAllowed ? "" : "!"));
    }

    public static <T> EntityResultType<T> nullAllowed(@NotNull Class<T> clazz) {
        return entityResultType(clazz, true);
    }

    public static <T> EntityResultType<T> nullAllowed(@NotNull TypeLiteral<T> type) {
        return entityResultType(type, true);
    }

    public static <T> EntityResultType<T> nonNull(@NotNull Class<T> clazz) {
        return entityResultType(clazz, false);
    }

    public static <T> EntityResultType<T> nonNull(@NotNull TypeLiteral<T> type) {
        return entityResultType(type, false);
    }

    public static <T> EntityResultType<T> entityResultType(@NotNull Class<T> clazz, boolean nullAllowed) {
        return entityResultType(classLiteral(clazz), nullAllowed);
    }

    @SuppressWarnings("unchecked")
    static <T> EntityResultType<T> entityResultType(@NotNull TypeLiteral<T> type, boolean nullAllowed) {
        return (EntityResultType<T>) instanceCache.computeIfAbsent(new Tuple2<>(type, nullAllowed), unused -> new EntityResultType<>(type, nullAllowed));
    }

    static {
        addPredefinedType(classLiteral(byte.class));
        addPredefinedType(classLiteral(byte[].class));
        addPredefinedType(classLiteral(short.class));
        addPredefinedType(classLiteral(short[].class));
        addPredefinedType(classLiteral(int.class));
        addPredefinedType(classLiteral(int[].class));
        addPredefinedType(classLiteral(long.class));
        addPredefinedType(classLiteral(long[].class));
        addPredefinedType(classLiteral(float.class));
        addPredefinedType(classLiteral(float[].class));
        addPredefinedType(classLiteral(double.class));
        addPredefinedType(classLiteral(double[].class));
        addPredefinedType(classLiteral(char.class));
        addPredefinedType(classLiteral(char[].class));
        addPredefinedType(classLiteral(boolean.class));
        addPredefinedType(classLiteral(boolean[].class));
        addPredefinedType(classLiteral(Byte.class));
        addPredefinedType(classLiteral(Short.class));
        addPredefinedType(classLiteral(Integer.class));
        addPredefinedType(classLiteral(Long.class));
        addPredefinedType(classLiteral(Float.class));
        addPredefinedType(classLiteral(Double.class));
        addPredefinedType(classLiteral(Character.class));
        addPredefinedType(classLiteral(Boolean.class));
        addPredefinedType(classLiteral(String.class));
        addPredefinedType(classLiteral(String[].class));
        addPredefinedType(classLiteral(BigInteger.class));
        addPredefinedType(classLiteral(BigDecimal.class));
        addPredefinedType(classLiteral(Instant.class));
        addPredefinedType(classLiteral(LocalDate.class));
        addPredefinedType(classLiteral(LocalTime.class));
    }

    static void addPredefinedType(@NotNull TypeLiteral<?> type) {
        instanceCache.put(new Tuple2<>(type, false), new EntityResultType<>(type, false));
        if (!type.isPrimitiveType()) {
            instanceCache.put(new Tuple2<>(type, true), new EntityResultType<>(type, true));
        }
    }
}
