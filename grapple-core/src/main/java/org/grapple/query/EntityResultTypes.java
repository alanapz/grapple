package org.grapple.query;

public final class EntityResultTypes {

    public static final EntityResultType<Byte> BYTE = EntityResultType.ofNonNull(byte.class);

    public static final EntityResultType<Byte> NULLABLE_BYTE = EntityResultType.ofNullable(Byte.class);

    public static final EntityResultType<Short> SHORT = EntityResultType.ofNonNull(short.class);

    public static final EntityResultType<Short> NULLABLE_SHORT = EntityResultType.ofNullable(Short.class);

    public static final EntityResultType<Integer> INT = EntityResultType.ofNonNull(int.class);

    public static final EntityResultType<Integer> NULLABLE_INT = EntityResultType.ofNullable(Integer.class);

    public static final EntityResultType<Long> LONG = EntityResultType.ofNonNull(long.class);

    public static final EntityResultType<Long> NULLABLE_LONG = EntityResultType.ofNullable(Long.class);

    public static final EntityResultType<Float> FLOAT = EntityResultType.ofNonNull(float.class);

    public static final EntityResultType<Float> NULLABLE_FLOAT = EntityResultType.ofNullable(Float.class);

    public static final EntityResultType<Double> DOUBLE = EntityResultType.ofNonNull(double.class);

    public static final EntityResultType<Double> NULLABLE_DOUBLE = EntityResultType.ofNullable(Double.class);

    public static final EntityResultType<Character> CHAR = EntityResultType.ofNonNull(char.class);

    public static final EntityResultType<Character> NULLABLE_CHAR = EntityResultType.ofNullable(Character.class);

    public static final EntityResultType<Boolean> BOOLEAN = EntityResultType.ofNonNull(boolean.class);

    public static final EntityResultType<Boolean> NULLABLE_BOOLEAN = EntityResultType.ofNullable(Boolean.class);

    public static final EntityResultType<String> STRING = EntityResultType.ofNonNull(String.class);

    public static final EntityResultType<String> NULLABLE_STRING = EntityResultType.ofNullable(String.class);

    private EntityResultTypes() {

    }
}

