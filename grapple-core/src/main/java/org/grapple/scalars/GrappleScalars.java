package org.grapple.scalars;

import static graphql.scalars.util.Kit.typeName;
import static graphql.schema.GraphQLScalarType.newScalar;
import static java.lang.String.format;

import java.time.YearMonth;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public final class GrappleScalars {

    public static final GraphQLScalarType YearMonthScalar = newScalar()
            .name("YearMonth")
            .description("Year-month (in the format YYYY-MM - eg: 2020-01)")
            .coercing(new Coercing<YearMonth, String>() {

                @Override
                public String serialize(Object input) throws CoercingSerializeException {
                    if (input instanceof YearMonth) {
                        return ((YearMonth) input).toString();
                    }
                    if (input instanceof String) {
                        try {
                            return YearMonth.parse((String) input).toString();
                        } catch (Exception e) {
                            throw new CoercingSerializeException(format("Expected a valid year month string but was but was %s", typeName(input)));
                        }
                    }
                    throw new CoercingSerializeException(format("Expected a 'java.time.YearMonth' object but was %s", typeName(input)));
                }

                @Override
                public YearMonth parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof YearMonth) {
                        return ((YearMonth) input);
                    }
                    if (input instanceof String) {
                        try {
                            return YearMonth.parse((String) input);
                        } catch (Exception e) {
                            throw new CoercingParseValueException(format("Unable to parse value to 'java.time.YearMonth' because of: %s", e.getMessage()));
                        }
                    }
                    throw new CoercingParseValueException(format("Expected a 'java.lang.String' object but was %s", typeName(input)));
                }

                @Override
                public YearMonth parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue) {
                        return YearMonth.parse(((StringValue) input).getValue());
                    }
                    throw new CoercingParseLiteralException(format("Expected a 'java.lang.String' object but was %s", typeName(input)));
                }

            }).build();

    private GrappleScalars() {

    }
}
