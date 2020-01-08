package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class EntityResultType<T> {

    private final Class<T> javaType;

    private final boolean nullable;

    private EntityResultType(Class<T> javaType, boolean nullable) {
        this.javaType = javaType;
        this.nullable = nullable;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String toString() {
        return format("%s%s", javaType.getSimpleName(), (nullable ? "" : "!"));
    }

    public static <T> EntityResultType<T> ofNullable(Class<T> javaType) {
        return of(javaType, true);
    }

    public static <T> EntityResultType<T> ofNonNull(Class<T> javaType) {
        return of(javaType, false);
    }

    public static <T> EntityResultType<T> of(Class<T> javaType, boolean nullable) {
        requireNonNull(javaType, "javaType");
        return new EntityResultType<>(javaType, nullable);
    }
}
