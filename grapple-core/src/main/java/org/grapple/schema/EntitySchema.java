package org.grapple.schema;

import java.util.function.Consumer;
import graphql.schema.GraphQLSchema;
import org.grapple.resolver.QueryResolver;
import org.grapple.utils.Chainable;

public interface EntitySchema extends Chainable<EntitySchema> {

    <X> EntitySchema addEntity(Class<X> entityClass, Consumer<EntityDefinition<X>> consumer);

    // EntitySchema addCustomType(String typeAlias, GraphQLType type);

    // EntitySchema addDefaultType(Type javaType, GraphQLType type);

    <X> void addEntityQueryInvoker(String name, Class<X> entityClass, Class<QueryParameters<X>> invokerParams, QueryResolver<X> queryResolver);

    GraphQLSchema generate();
}
