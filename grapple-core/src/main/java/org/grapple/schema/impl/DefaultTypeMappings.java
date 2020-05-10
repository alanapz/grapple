package org.grapple.schema.impl;

import static graphql.schema.GraphQLNonNull.nonNull;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import org.grapple.scalars.GrappleScalars;

// Used to store predefined, in-built type mappings
// This class is used as a last resort when we can't find any associated class
final class DefaultTypeMappings {

    private static final Map<Type, GraphQLInputType> inputTypeMappings = new HashMap<>();

    private static final Map<Type, GraphQLOutputType> outputTypeMappings = new HashMap<>();

    public static GraphQLInputType getDefaultInputTypeFor(Type type) {
        requireNonNull(type, "type");
        return inputTypeMappings.get(type);
    }

    public static GraphQLOutputType getDefaultOutputTypeFor(Type type) {
        requireNonNull(type, "type");
        return outputTypeMappings.get(type);
    }

    static {

        // Primitives
        addType(boolean.class, nonNull(Scalars.GraphQLBoolean));
        addType(byte.class, nonNull(Scalars.GraphQLByte));
        addType(short.class, nonNull(Scalars.GraphQLShort));
        addType(int.class, nonNull(Scalars.GraphQLInt));
        addType(long.class, nonNull(Scalars.GraphQLLong));
        addType(char.class, nonNull(Scalars.GraphQLChar));
        addType(float.class, nonNull(Scalars.GraphQLFloat));
        addType(double.class, nonNull(Scalars.GraphQLFloat)); // No "double" type

        // Primitive object wrappers
        addType(Boolean.class, Scalars.GraphQLBoolean);
        addType(Byte.class, Scalars.GraphQLByte);
        addType(Short.class, Scalars.GraphQLShort);
        addType(Integer.class, Scalars.GraphQLInt);
        addType(Long.class, Scalars.GraphQLLong);
        addType(Character.class, Scalars.GraphQLChar);
        addType(Float.class, Scalars.GraphQLFloat);
        addType(Double.class, Scalars.GraphQLFloat); // No "Double" type

        addType(String.class, Scalars.GraphQLString);
        addType(UUID.class, Scalars.GraphQLID);
        addType(BigDecimal.class, Scalars.GraphQLBigDecimal);
        addType(BigInteger.class, Scalars.GraphQLBigInteger);

        addType(LocalDate.class, ExtendedScalars.Date);
        addType(OffsetDateTime.class, ExtendedScalars.DateTime);
        addType(Object.class, ExtendedScalars.Object);
        addType(Locale.class, ExtendedScalars.Locale);
        addType(OffsetTime.class, ExtendedScalars.Time);
        addType(URL.class, ExtendedScalars.Url);

        addType(YearMonth.class, GrappleScalars.YearMonth);

        // INPUT ONLY TYPES

        addInputOnlyType(Instant.class, GrappleScalars.UtcTimestamp);
    }

    private static <V extends GraphQLInputType & GraphQLOutputType> void addType(Type type, V gqlType) {
        requireNonNull(type, "type");
        requireNonNull(gqlType, "gqlType");
        inputTypeMappings.put(type, gqlType);
        outputTypeMappings.put(type, gqlType);
    }

    private static void addInputOnlyType(Type type, GraphQLInputType gqlType) {
        requireNonNull(type, "type");
        requireNonNull(gqlType, "gqlType");
        inputTypeMappings.put(type, gqlType);
    }

    private static void addOutputOnlyType(Type type, GraphQLOutputType gqlType) {
        requireNonNull(type, "klazz");
        requireNonNull(gqlType, "gqlType");
        outputTypeMappings.put(type, gqlType);
    }
}
