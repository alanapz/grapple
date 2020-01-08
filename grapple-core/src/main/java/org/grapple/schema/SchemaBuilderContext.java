package org.grapple.schema;

import java.util.HashMap;
import java.util.Map;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import static java.util.Objects.requireNonNull;

final class SchemaBuilderContext {

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> entityTypes = new HashMap<>();

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> containerTypes = new HashMap<>();

    private final Map<EntityDefinition<?>, GraphQLInputObjectType.Builder> filterTypes = new HashMap<>();

    SchemaBuilderContext addEntityType(EntityDefinition<?> entity, GraphQLObjectType.Builder entityType) {
        requireNonNull(entity, "entity");
        requireNonNull(entityType, "entityType");
        entityTypes.put(entity, entityType);
        return this;
   }

    SchemaBuilderContext addContainerType(EntityDefinition<?> entity, GraphQLObjectType.Builder containerType) {
        requireNonNull(entity, "entity");
        requireNonNull(containerType, "containerType");
        containerTypes.put(entity, containerType);
        return this;
    }

    SchemaBuilderContext addFilterType(EntityDefinition<?> entity, GraphQLInputObjectType.Builder filterType) {
        requireNonNull(entity, "entity");
        requireNonNull(filterType, "filterType");
        filterTypes.put(entity, filterType);
        return this;
    }

    public void generate(GraphQLSchema.Builder schemaBuilder) {
        for (Map.Entry<?, GraphQLInputObjectType.Builder> entry: filterTypes.entrySet()) {
            schemaBuilder.additionalType(entry.getValue().build());
        }
        for (Map.Entry<?, GraphQLObjectType.Builder> entry: entityTypes.entrySet()) {
            schemaBuilder.additionalType(entry.getValue().build());
        }
        for (Map.Entry<?, GraphQLObjectType.Builder> entry: containerTypes.entrySet()) {
            schemaBuilder.additionalType(entry.getValue().build());
        }
    }
}
