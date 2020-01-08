package org.grapple.schema;

import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import org.grapple.query.EntityResultType;

public final class SchemaUtils {

    private SchemaUtils() {

    }

    static GraphQLOutputType wrapOutputType(EntityResultType<?> resultType, GraphQLOutputType type) {
        return (resultType.isNullable() || type instanceof GraphQLNonNull ? type : GraphQLNonNull.nonNull(type));
    }

}
