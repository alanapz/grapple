package org.grapple.schema.impl;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.QueryResultListUtils.queryResultList;
import static org.grapple.reflect.ReflectUtils.wrapPrimitiveTypeIfNecessary;
import static org.grapple.schema.impl.RuntimeWiring.entityQueryParameterWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityQueryWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultListUtils;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.reflect.EntityQueryMetadata;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryMethodMetadata;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeConversionException;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.QueryExecutionException;
import org.grapple.utils.UnexpectedException;
import org.grapple.utils.UnreachableException;

final class EntityQueryUtils {

    private EntityQueryUtils() {

    }

    static <X> void buildAndRegisterEntityQuery(SchemaBuilderContext ctx, EntityQueryDefinitionImpl<X> queryDefinition) {
        requireNonNull(ctx, "ctx");
        requireNonNull(queryDefinition, "queryDefinition");

        final EntityDefinitionImpl<X> entity = requireNonNull(queryDefinition.getEntity(), "entity");
        final String queryName = requireNonNull(queryDefinition.getName(), "queryName");
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

        ctx.addRootQueryField(fieldBuilder.build());
        ctx.addEntityQueryWiring(entityQueryWiring(entity.getEntityClass(), queryName, queryType, queryDefinition.getQueryResolver()));

        if (queryDefinition.getVisibility() != null) {
            ctx.getSchemaBuilderElementVisibility().setQueryVisibility(queryName, queryDefinition.getVisibility());
        }
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

    static <X> EntityQueryResolver<X> buildMethodEntityQueryResolver(EntityQueryMethodMetadata<X> methodMetadata, Object instance) {
        requireNonNull(methodMetadata, "methodMetadata");
        requireNonNull(instance, "instance");
        final Method sourceMethod = methodMetadata.method;
        // Attempt to "rebind" method to instance (as instance class may not be the same as QueryMethodResult class
        // This is due to (for example) Spring Proxies (we need to always go via proxy even though we have the unwrapped type as parameter)
        // Another advantage, we can setAccessible systematically
        final Method boundMethod = ReflectUtils.lookupDeclaredMethod(instance.getClass(), sourceMethod.getName(), sourceMethod.getParameterTypes()).orElseThrow(() -> new UnexpectedException(format("Couldn't lookup method: %s", sourceMethod)));
        boundMethod.setAccessible(true);
        return new MethodEntityQueryResolver<>(boundMethod, methodMetadata, instance);
    }

    private static final class MethodEntityQueryResolver<X> implements EntityQueryResolver<X> {

        private final Method method;

        private final EntityQueryMethodMetadata<X> methodMetadata;

        private final EntityQueryMetadata.EntityQueryMethodType methodType;

        private final Object instance;

        private MethodEntityQueryResolver(Method method, EntityQueryMethodMetadata<X> methodMetadata, Object instance) {
            this.method = requireNonNull(method, "method");
            this.methodMetadata = requireNonNull(methodMetadata, "methodMetadata");
            this.methodType = requireNonNull(methodMetadata.methodType, "methodType");
            this.instance = requireNonNull(instance, "instance");
        }

        @Override
        public QueryResultList<X> execute(DataFetchingEnvironment environment, RootFetchSet<X> fetchSet, Map<String, Object> queryParameters) {
            requireNonNull(fetchSet, "fetchSet");
            requireNonNull(queryParameters, "queryParameters");

            final Object[] invokerParams = new Object[method.getParameterCount()];
            invokerParams[methodMetadata.fetchSetParameterIndex] = fetchSet;

            queryParameters.forEach((paramName, paramValue) -> {
                final EntityQueryMetadata.EntityQueryParameterMetadata parameterMetadata = methodMetadata.parameters.get(paramName);
                if (parameterMetadata == null) {
                    throw new UnexpectedException(format("Unknown parameter: %s for method: %s", paramName, method.getName()));
                }
                if (paramValue == null) {
                    return;
                }
                final Class<?> parameterType = method.getParameterTypes()[parameterMetadata.index];
                if (!wrapPrimitiveTypeIfNecessary(parameterType).isInstance(paramValue)) { // wrapPrimitiveType to handle Integer -> int conversions
                    throw new TypeConversionException(format("Couldn't convert %s (of type %s) to %s for method: %s", paramValue, paramValue.getClass().getName(), parameterType.getName(), method.getName()));
                }
                invokerParams[parameterMetadata.index] = paramValue;
            });

            try {
                final Object response = method.invoke(instance, invokerParams);
                if (response == null) {
                    return null;
                }
                // We need to convert response based on method type
                if (methodType == EntityQueryMetadata.EntityQueryMethodType.LIST) {
                    final @SuppressWarnings("unchecked") QueryResultList<X> queryResultList = (QueryResultList<X>) response;
                    return queryResultList;
                }
                // QueryResultRow -> QueryResultList
                // We return a fake queryResultList of just 1 element
                if (methodType == EntityQueryMetadata.EntityQueryMethodType.ROW) {
                    final @SuppressWarnings("unchecked") QueryResultRow<X> queryResultRow = (QueryResultRow<X>) response;
                    return queryResultList(1, singletonList(queryResultRow));
                }
                // Optional<QueryResultRow> -> QueryResultList
                // If we have a result: Return fake queryResultList of element
                // Otherwise return empty list
                if (methodType == EntityQueryMetadata.EntityQueryMethodType.OPTIONAL_ROW) {
                    final @SuppressWarnings("unchecked") QueryResultRow<X> queryResultRow = ((Optional<QueryResultRow<X>>) response).orElse(null);
                    if (queryResultRow == null) {
                        return QueryResultListUtils.emptyResultList();
                    }
                    return queryResultList(1, singletonList(queryResultRow));
                }
                throw new UnreachableException();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (InvocationTargetException e) {
                throw (e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause() : new QueryExecutionException(method, e.getCause()));
            }
            catch (Exception e) {
                throw new QueryExecutionException(method, e);
            }
        }

        @Override
        public String toString() {
            return format("%s.%s", method.getDeclaringClass().getName(), method.getName());
        }
    }
}
