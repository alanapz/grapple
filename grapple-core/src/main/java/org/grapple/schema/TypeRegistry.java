package org.grapple.schema;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;

import static java.lang.String.format;

public class TypeRegistry {

    private static Map<Class<?>, GraphQLType> defaultTypes = new HashMap<>();

    public static int int_p;

    public static Integer int_v;

    public static int[] int_pa;

    public static Integer[] int_pac;

    public static List<Integer> listOfIntegers;

    public static Set<Integer> setOfIntegers;

    public static Set<List<String>> setOfListOfString;

    public static Set<List<int[]>> setOfListOfInt;

    public static RetentionPolicy retentionPolicy;

    public static Set<RetentionPolicy> retentionPolicies;

    public static Set<List<String>>[] setListStringArray;

    public static Set<Object[]>[] oa;

    public static Object o;

    // *

    private final Map<Type, GraphQLTypeReference> schemaTypes = new HashMap<>();

    private final Map<Type, GraphQLType> constructedTypes = new HashMap<>();

    public static void main(String[] args) throws Exception {
        for (Field f: TypeRegistry.class.getFields()) {
            System.out.println(f.getName() + " - " + GraphQLTypeUtil.simplePrint(getFieldType(f.getName())));
        }
        GraphQLSchema.Builder x;
        //x.
    }

    public static GraphQLType getFieldType(String name) throws Exception {
        // return getType(name, TypeRegistry.class.getField(name).getGenericType());
        return null;
    }

    public GraphQLType getType(String name, Type type) {
        if (type instanceof Class<?>) {
            final Class<?> rawType = (Class<?>) type;
            if (schemaTypes.containsKey(rawType)) {
                return schemaTypes.get(rawType);
            }
            if (defaultTypes.containsKey(rawType)) {
                return defaultTypes.get(rawType);
            }
            if (rawType.isArray()) {
                return GraphQLList.list(getType(name, rawType.getComponentType()));
            }
            if (Enum.class.isAssignableFrom(rawType)) {
                return GraphQLEnumType.newEnum().name(rawType.getSimpleName() + "Enum").build();
            }
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType)) {
                return GraphQLList.list(getType(name, parameterizedType.getActualTypeArguments()[0]));
            }
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType arrayType = (GenericArrayType) type;
            return GraphQLList.list(getType(name, arrayType.getGenericComponentType()));
        }
        throw new IllegalArgumentException(format("Unexpected type: %s for: %s", type, name));
    }

    private void buildEnumType() {
//        return GraphQLEnumType.newEnum().name(rawType.getSimpleName() + "Enum").build();
    }

    static {

        // Primitives
        defaultTypes.put(boolean.class, GraphQLNonNull.nonNull(Scalars.GraphQLBoolean));
        defaultTypes.put(byte.class, GraphQLNonNull.nonNull(Scalars.GraphQLByte));
        defaultTypes.put(short.class, GraphQLNonNull.nonNull(Scalars.GraphQLShort));
        defaultTypes.put(int.class, GraphQLNonNull.nonNull(Scalars.GraphQLInt));
        defaultTypes.put(long.class, GraphQLNonNull.nonNull(Scalars.GraphQLLong));
        defaultTypes.put(char.class, GraphQLNonNull.nonNull(Scalars.GraphQLChar));
        defaultTypes.put(float.class, GraphQLNonNull.nonNull(Scalars.GraphQLFloat));
        defaultTypes.put(double.class, GraphQLNonNull.nonNull(Scalars.GraphQLFloat)); // No "double" type

        // Primitive object wrappers
        defaultTypes.put(Boolean.class, Scalars.GraphQLBoolean);
        defaultTypes.put(Byte.class, Scalars.GraphQLByte);
        defaultTypes.put(Short.class, Scalars.GraphQLShort);
        defaultTypes.put(Integer.class, Scalars.GraphQLInt);
        defaultTypes.put(Long.class, Scalars.GraphQLLong);
        defaultTypes.put(Character.class, Scalars.GraphQLChar);
        defaultTypes.put(Float.class, Scalars.GraphQLFloat);
        defaultTypes.put(Double.class, Scalars.GraphQLFloat); // No "Double" type

        defaultTypes.put(String.class, Scalars.GraphQLString);

        defaultTypes.put(UUID.class, Scalars.GraphQLID);

        defaultTypes.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        defaultTypes.put(BigInteger.class, Scalars.GraphQLBigInteger);

        defaultTypes.put(LocalDate.class, ExtendedScalars.Date);
        defaultTypes.put(OffsetDateTime.class, ExtendedScalars.DateTime);
        defaultTypes.put(Object.class, ExtendedScalars.Object);
        defaultTypes.put(Locale.class, ExtendedScalars.Locale);
        defaultTypes.put(OffsetTime.class, ExtendedScalars.Time);
        defaultTypes.put(URL.class, ExtendedScalars.Url);
    }
}
