package org.grapple.schema;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import graphql.schema.GraphQLSchema;
import org.grapple.utils.Chainable;

public interface EntitySchema extends Chainable<EntitySchema> {

    <X> EntitySchema addEntity(Class<X> entityClass, Consumer<EntityDefinition<X>> consumer);

    EntitySchema setContainerName(UnaryOperator<String> containerName);

    EntitySchema setFilterName(UnaryOperator<String> filterName);

    // EntitySchema addCustomType(String typeAlias, GraphQLType type);

    // EntitySchema addDefaultType(Type javaType, GraphQLType type);

    // <X> void addEntityQueryInvoker(String name, Class<X> entityClass, Class<QueryParameters<X>> invokerParams, QueryResolver<X> queryResolver);

    GraphQLSchema generate();
}
