package org.grapple.schema.impl;

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.isNonNull;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import graphql.language.Field;
import graphql.language.SelectionSet;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import org.grapple.query.EntityResultType;
import org.grapple.query.RootFetchSet;
import org.grapple.query.impl.QueryProvider;
import org.grapple.schema.GraphQLQueryParameters;

public final class SchemaUtils {

    private SchemaUtils() {

    }

    static GraphQLType wrapList(GraphQLType wrappedType) {
        requireNonNull(wrappedType, "wrappedType");
        return (isList(wrappedType) ? wrappedType : GraphQLList.list(wrappedType));
    }

    static GraphQLType wrapNonNull(GraphQLType wrappedType) {
        requireNonNull(wrappedType, "wrappedType");
        return (isNonNull(wrappedType) ? wrappedType : nonNull(wrappedType));
    }

    static GraphQLOutputType wrapNonNullIfNecessary(EntityResultType<?> resultType, GraphQLOutputType unwrappedType) {
        requireNonNull(resultType, "resultType");
        requireNonNull(unwrappedType, "unwrappedType");
        return (resultType.isNullAllowed() || (unwrappedType instanceof GraphQLNonNull) ? unwrappedType : nonNull(unwrappedType));
    }

    static SelectionSet walkFieldHierachy(Field field, String... paths) {
        requireNonNull(field, "field");
        requireNonNull(paths, "paths");
        for (String path : paths) {
            final SelectionSet selectionSet = field.getSelectionSet();
            if (selectionSet == null) {
                return null;
            }
            final Field nextField = seq(selectionSet.getChildren()).filter(Field.class::isInstance).cast(Field.class).findFirst(child -> path.equals(child.getName())).orElse(null);
            if (nextField == null) {
                return null;
            }
            field = nextField;
        }
        return field.getSelectionSet();
    }

    static <X> RootFetchSet<X> buildFetchSet(DataFetchingEnvironment environment, String queryName, Class<X> entityClass) {
        requireNonNull(environment, "environment");
        requireNonNull(queryName, "queryName");
        requireNonNull(entityClass, "entityClass");
        return QueryProvider.newQuery(entityClass)
                .setQueryParameter(GraphQLQueryParameters.QueryName, queryName)
                .setQueryParameter(GraphQLQueryParameters.Environment, environment)
                .setQueryParameter(GraphQLQueryParameters.QuerySource, environment.getSource());
    }




}
