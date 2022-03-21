package org.grapple.scalars;

import static graphql.scalars.util.Kit.typeName;
import static graphql.schema.GraphQLScalarType.newScalar;
import static java.lang.String.format;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;

public final class GrappleScalars {

    public static final GraphQLScalarType YearMonth = newScalar()
            .name("YearMonth")
            .description("Year-month (in the format YYYY-MM - eg: 2020-01)")
            .coercing(new Coercing<YearMonth, String>() {

                @Override
                public String serialize(@NotNull Object input) {
                    if (input instanceof YearMonth) {
                        return ((YearMonth) input).toString();
                    }
                    if (input instanceof String) {
                        try {
                            return java.time.YearMonth.parse((String) input).toString();
                        } catch (Exception e) {
                            throw new CoercingSerializeException(format("Expected a YearMonth string but received: %s", typeName(input)));
                        }
                    }
                    throw new CoercingSerializeException(format("Expected a 'java.time.YearMonth' object but received: %s", typeName(input)));
                }

                @Override
                public java.time.YearMonth parseValue(@NotNull Object input) throws CoercingParseValueException {
                    if (input instanceof java.time.YearMonth) {
                        return ((java.time.YearMonth) input);
                    }
                    if (input instanceof String) {
                        try {
                            return java.time.YearMonth.parse((String) input);
                        } catch (Exception e) {
                            throw new CoercingParseValueException(format("Unable to parse value to 'java.time.YearMonth' because of: %s", e.getMessage()));
                        }
                    }
                    throw new CoercingParseValueException(format("Expected a 'java.lang.String' object but was %s", typeName(input)));
                }

                @Override
                public YearMonth parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue) {
                        return java.time.YearMonth.parse(((StringValue) input).getValue());
                    }
                    throw new CoercingParseLiteralException(format("Expected a 'java.lang.String' object but was %s", typeName(input)));
                }

            }).build();

    public static final GraphQLScalarType UtcTimestamp = newScalar()
            .name("UtcTimestamp")
            .description("UTC timestamp, either in format ISO (2011-12-03T10:15:30Z) or epoch milliseconds")
            .coercing(new Coercing<Instant, String>() {

                private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

                private final Pattern epochMillisPattern = Pattern.compile("^(\\d+)$");

                @Override
                public String serialize(Object input) {
                    try {
                        if (input instanceof Instant) {
                            return formatter.format((Instant) input);
                        }
                        if (input instanceof String) {
                            return formatter.format(Instant.from(formatter.parse((String) input)));
                        }
                        throw new CoercingSerializeException(format("Unexpected value: %s(%s) for UtcTimestamp", input, input.getClass().getName()));
                    }
                    catch (Exception e) {
                        throw new CoercingSerializeException(format("Couldn't coerce: %s(%s) to UtcTimestamp", input, input.getClass().getName()), e);
                    }
                }

                @Override
                public Instant parseValue(@NotNull Object input) {
                    try {
                        if (input instanceof Instant) {
                            return (Instant) input;
                        }
                        if (input instanceof Number) {
                            return Instant.ofEpochMilli(((Number) input).longValue());
                        }
                        if (input instanceof String) {
                            return Instant.from(formatter.parse((String) input));
                        }
                        throw new CoercingParseValueException(format("Unexpected value: %s(%s) for UtcTimestamp", input, input.getClass().getName()));
                    }
                    catch (Exception e) {
                        throw new CoercingParseValueException(format("Couldn't coerce: %s(%s) to UtcTimestamp", input, input.getClass().getName()), e);
                    }
                }

                @Override
                public Instant parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                    try {
                        if (input instanceof IntValue) {
                            return Instant.ofEpochMilli(((IntValue) input).getValue().longValue());
                        }
                        if (input instanceof StringValue) {
                            return Instant.from(formatter.parse(((StringValue) input).getValue()));
                        }
                        throw new CoercingParseLiteralException(format("Unexpected value: %s(%s) for UtcTimestamp", input, input.getClass().getName()));
                    }
                    catch (Exception e) {
                        throw new CoercingParseLiteralException(format("Couldn't coerce: %s(%s) to UtcTimestamp", input, input.getClass().getName()), e);
                    }
                }

            }).build();

    private GrappleScalars() {

    }
}
