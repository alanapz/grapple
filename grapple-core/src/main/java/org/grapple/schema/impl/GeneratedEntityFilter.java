package org.grapple.schema.impl;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLTypeReference;

final class GeneratedEntityFilter<X> {

    private final String entityName;

    private final Map<String, GeneratedEntityFilterItem<X>> items;

    private final Set<EntityCustomFilterDefinitionImpl<X, ?>> customFilters;

    GeneratedEntityFilter(String entityName, String description, Map<String, GeneratedEntityFilterItem<X>> items, Set<EntityCustomFilterDefinitionImpl<X, ?>> customFilters) {
        this.entityName = requireNonNull(entityName, "entityName");
        this.items = unmodifiableMap(requireNonNull(items, "items"));
        this.customFilters = unmodifiableSet(requireNonNull(customFilters, "customFilters"));
    }

    GraphQLTypeReference getRef() {
        return typeRef(entityName);
    }

    GraphQLInputObjectType build(SchemaBuilderContext ctx) {
        final GraphQLInputObjectType.Builder builder = newInputObject().name(entityName);
        items.forEach((key, value) -> builder.field(newInputObjectField().name(key).type(value.getInputType(this))));
        customFilters.forEach(customFilter -> customFilter.build(ctx, builder));
        return builder.build();
    }
}
