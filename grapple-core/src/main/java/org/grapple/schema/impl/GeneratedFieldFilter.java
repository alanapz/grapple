package org.grapple.schema.impl;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.Map;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.Utils;

final class GeneratedFieldFilter<T> {

    private final TypeLiteral<T> filterType;

    private final String entityName;

    private final String description;

    final Map<String, SimpleFieldFilterItem<T>> items;

    GeneratedFieldFilter(FieldFilterDefinitionImpl<T> definition) {
        this.filterType = requireNonNull(definition.getFieldType(), "fieldType");
        this.entityName = requireNonNull(definition.getName(), "entityName");
        this.description = definition.getDescription();
        this.items = readOnlyCopy(requireNonNull(definition.getItems(), "items"));
    }

    GraphQLTypeReference getRef() {
        return typeRef(entityName);
    }

    TypeLiteral<T> getFilterType() {
        return filterType;
    }

    GraphQLInputObjectType build(SchemaBuilderContext ctx) {
        final GraphQLInputObjectType.Builder builder = newInputObject().name(entityName).description(description);
        items.values().forEach(element -> builder.field(newInputObjectField().name(element.name).type(element.gqlType)));
        return builder.build();
    }
}
