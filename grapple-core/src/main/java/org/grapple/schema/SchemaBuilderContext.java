package org.grapple.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.EntityFilter;
import org.grapple.query.Filters;
import org.grapple.utils.UnexpectedException;
import org.jooq.lambda.tuple.Tuple2;

final class SchemaBuilderContext {

    private final EntitySchemaImpl schema;

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> entityTypes = new HashMap<>();

    private final Map<EntityDefinition<?>, GraphQLObjectType.Builder> containerTypes = new HashMap<>();

    private final Map<EntityDefinition<?>, GraphQLInputObjectType.Builder> filterTypes = new HashMap<>();

    private final Map<Class<?>, GraphQLEnumType> enumTypeCache = new HashMap<>();

    private final Map<Tuple2<EntityDefinitionImpl<?>, String>, Function<Object, ? extends EntityFilter<?>>> fieldFilterDefinitions = new HashMap<>();

    SchemaBuilderContext(EntitySchemaImpl schema) {
        this.schema = requireNonNull(schema, "schema");
    }

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

    <X> void addFieldFilter(EntityDefinitionImpl<X> entity, String filterName, Function<Object, EntityFilter<X>> filter) {
        requireNonNull(entity, "entity");
        requireNonNull(filterName, "name");
        requireNonNull(filter, "filter");
        fieldFilterDefinitions.put(new Tuple2<>(entity, filterName), filter);
    }

    @SuppressWarnings("unchecked")
    GraphQLOutputType getUnwrappedTypeFor(Type type) {
        requireNonNull(type, "type");
        if (type instanceof Class<?>) {
            final Class<?> classType = (Class<?>) type;

            // If type is an enum, look to see whether we have already been created (we need to create a cache to make sure we don't create multiple enum classes)
            if (Enum.class.isAssignableFrom(classType)) {
                // If we already generated a corresponding enum class, return typeref directly
                // Otherwise, generate and add to our list of cache (we will add to the schema via schemaBuilder.additionalType) later on
                final GraphQLEnumType enumType = enumTypeCache.computeIfAbsent(classType, unused -> schema.buildEnumTypeForClass((Class<Enum<?>>) classType));
                return GraphQLTypeReference.typeRef(enumType.getName());
            }
        }

        System.out.println(type);
        return null;
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

        for (GraphQLEnumType enumType: enumTypeCache.values()) {
            schemaBuilder.additionalType(enumType);
        }
    }

    @SuppressWarnings("unchecked")
    <X> EntityFilter<X> applyEntityFilter(EntityDefinitionImpl<X> entityDefinition, Map<String, Object> filterItems) {

        requireNonNull(entityDefinition, "entityDefinition");
        requireNonNull(filterItems, "filterItems");

        final List<EntityFilter<X>> entityFilters = new ArrayList<>();

        for (Map.Entry<String, Object> filterItem: filterItems.entrySet()) {
            final Tuple2<EntityDefinitionImpl<?>, String> filterKey = new Tuple2<>(entityDefinition, filterItem.getKey());
            final Function<Object, EntityFilter<X>> filterDefinition = (EntityFilterDefinition<X>) fieldFilterDefinitions.get(filterKey);
            if (filterDefinition == null) {
                throw new UnexpectedException(format("Entity filter not found: %s", filterKey));
            }
            entityFilters.add(filterDefinition.apply(filterItem.getValue()));
        }

        return Filters.and(entityFilters);
    }
}
