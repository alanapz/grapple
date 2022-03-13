package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
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
import org.jooq.lambda.tuple.Tuple2;

public final class EntityResultType<T> {

    private final TypeLiteral<T> type;

    private final boolean nullAllowed;

    private static final Map<Tuple2<TypeLiteral<?>, Boolean>, EntityResultType<?>> instanceCache = new NoDuplicatesMap<>();

    private EntityResultType(TypeLiteral<T> type, boolean nullAllowed) {
        requireNonNull(type, "type");
        if (type.isPrimitiveType() && nullAllowed)  {
            throw new UnexpectedException(String.format("Cannot have null primitive: %s", type));
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
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        return type.equals(((EntityResultType<?>) other).type) && (nullAllowed == ((EntityResultType<?>) other).nullAllowed);
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ (nullAllowed ? 1 : 0);
    }

    @Override
    public String toString() {
        return format("%s%s", type, (nullAllowed ? "" : "!"));
    }

    public static <T> EntityResultType<T> nullAllowed(Class<T> clazz) {
        requireNonNull(clazz, "clazz");
        return fieldResultType(clazz, true);
    }

    public static <T> EntityResultType<T> nullAllowed(TypeLiteral<T> type) {
        requireNonNull(type, "type");
        return fieldResultType(type, true);
    }

    public static <T> EntityResultType<T> nonNull(Class<T> clazz) {
        requireNonNull(clazz, "clazz");
        return fieldResultType(clazz, false);
    }

    public static <T> EntityResultType<T> nonNull(TypeLiteral<T> type) {
        requireNonNull(type, "type");
        return fieldResultType(type, false);
    }

    public static <T> EntityResultType<T> fieldResultType(Class<T> clazz, boolean nullAllowed) {
        requireNonNull(clazz, "clazz");
        return fieldResultType(classLiteral(clazz), nullAllowed);
    }

    @SuppressWarnings("unchecked")
    static <T> EntityResultType<T> fieldResultType(TypeLiteral<T> type, boolean nullAllowed) {
        requireNonNull(type, "type");
        final EntityResultType<T> predefinedType = (EntityResultType<T>) instanceCache.get(new Tuple2<TypeLiteral<?>, Boolean>(type, nullAllowed));
        if (predefinedType != null) {
            return predefinedType;
        }
        return new EntityResultType<>(type, nullAllowed);
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

    static void addPredefinedType(TypeLiteral<?> type) {
        instanceCache.put(new Tuple2<>(type, false), new EntityResultType<>(type, false));
        if (!type.isPrimitiveType()) {
            instanceCache.put(new Tuple2<>(type, true), new EntityResultType<>(type, true));
        }
    }
}
