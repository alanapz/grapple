package org.grapple.schema.impl;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityQueryParameterWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityQueryWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNull;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLTypeReference;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryType;

final class EntityQueryUtils {

    private EntityQueryUtils() {

    }

    static <X> void buildAndRegisterEntityQuery(SchemaBuilderContext ctx, EntityQueryDefinitionImpl<X> queryDefinition) {
        requireNonNull(ctx, "ctx");
        requireNonNull(queryDefinition, "queryDefinition");

        final EntityDefinitionImpl<X> entity = requireNonNull(queryDefinition.getEntity(), "entity");
        final String queryName = requireNonNull(queryDefinition.getQueryName(), "queryName");
        final EntityQueryType queryType = requireNonNull(queryDefinition.getQueryType(), "queryType");

        final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition();
        fieldBuilder.name(queryName);
        fieldBuilder.description(queryDefinition.getDescription());
        fieldBuilder.deprecate(queryDefinition.getDeprecationReason());
        if (queryType == EntityQueryType.LIST) {
            fieldBuilder.type(nonNull(entity.getContainerTypeRef()));
        }
        if (queryType == EntityQueryType.SCALAR_NON_NULL) {
            fieldBuilder.type(nonNull(entity.getEntityTypeRef()));
        }
        if (queryType == EntityQueryType.SCALAR_NULL_ALLOWED) {
            fieldBuilder.type(entity.getEntityTypeRef());
        }

        if (queryType == EntityQueryType.LIST) {
            final GraphQLTypeReference filterTypeRef = entity.getFilterTypeRef(ctx);
            if (filterTypeRef != null) {
                fieldBuilder.argument(newArgument().name("filter").type(filterTypeRef).build());
            }

            final GraphQLTypeReference orderByTypeRef = entity.getOrderByTypeRef(ctx);
            if (orderByTypeRef != null) {
                fieldBuilder.argument(newArgument().name("orderBy").type(GraphQLList.list(nonNull(orderByTypeRef))).build());
            }

            fieldBuilder.argument(newArgument().name("offset").type(Scalars.GraphQLInt).build());
            fieldBuilder.argument(newArgument().name("count").type(Scalars.GraphQLInt).build());
        }

        for (EntityQueryDefinitionParameter<?> parameter: queryDefinition.getParameters()) {
            final GraphQLArgument parameterArgument = buildParameter(ctx, parameter);
            if (parameterArgument != null) {
                fieldBuilder.argument(parameterArgument);
                ctx.addEntityQueryParameterWiring(entityQueryParameterWiring(queryName, parameter.getName(), parameter.getType()));
            }
        }

        ctx.addEntityQueryField(fieldBuilder.build());
        ctx.addEntityQueryWiring(entityQueryWiring(entity.getEntityClass(), queryName, queryType, queryDefinition.getQueryResolver()));
    }

    private static GraphQLArgument buildParameter(SchemaBuilderContext ctx, EntityQueryDefinitionParameter<?> parameter) {
        final GraphQLInputType graphQLType = resolveGraphQLType(ctx, parameter.getType(), parameter.isRequired());
        if (graphQLType == null) {
            // Skip parameters with no matching types
            return null;
        }
        final GraphQLArgument.Builder argumentBuilder = newArgument();
        argumentBuilder.name(parameter.getName());
        argumentBuilder.description(parameter.getDescription());
        argumentBuilder.type(graphQLType);
        return argumentBuilder.build();
    }

    /// XXX: Maybe this should be in SchemaBuilderContext ?
    private static GraphQLInputType resolveGraphQLType(SchemaBuilderContext ctx, TypeLiteral<?> type, boolean required) {
        // XXX TODO FIXME
//        if (forcedGraphQLType != null) {
//            return forcedGraphQLType;
//        }
        final GraphQLInputType inputType = (GraphQLInputType) ctx.getSchema().getUnwrappedTypeFor(ctx, type.getType());
        if (inputType == null) {
            return null;
        }
        return (required ? (GraphQLInputType) wrapNonNull(inputType) : inputType);
    }
}
