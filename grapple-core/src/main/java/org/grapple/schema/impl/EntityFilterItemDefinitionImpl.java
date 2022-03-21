package org.grapple.schema.impl;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterCustomWiring;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputType;
import org.grapple.core.ElementVisibility;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityFilterItemDefinition;
import org.grapple.schema.EntityFilterItemResolver;

final class EntityFilterItemDefinitionImpl<X, T> implements EntityFilterItemDefinition<X, T> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final TypeLiteral<T> fieldType;

    private String fieldName;

    private String description;

    private String deprecationReason;

    private ElementVisibility visibility;

    private EntityFilterItemResolver<X, T> filterResolver;

    EntityFilterItemDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, TypeLiteral<T> fieldType) {
        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.fieldType = requireNonNull(fieldType, "fieldType");
    }

    @Override
    public EntityDefinition<X> getEntity() {
        return entity;
    }

    @Override
    public TypeLiteral<T> getFieldType() {
        return fieldType;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public void setName(String fieldName) {
        requireNonNull(fieldName, "fieldName");
        this.fieldName = fieldName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDeprecationReason() {
        return deprecationReason;
    }

    @Override
    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }

    @Override
    public ElementVisibility getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(ElementVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public EntityFilterItemResolver<X, T> getFilterResolver() {
        return filterResolver;
    }

    @Override
    public void setFilterResolver(EntityFilterItemResolver<X, T> filterResolver) {
        requireNonNull(filterResolver, "filterResolver");
        this.filterResolver = filterResolver;
    }

    @Override
    public void validate() {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("fieldName not configured");
        }
        if (filterResolver == null) {
            throw new IllegalArgumentException("filterResolver not configured");
        }
    }

    GraphQLInputObjectField build(SchemaBuilderContext ctx) {
        this.validate();
        if (!ctx.isSchemaElementVisible(visibility)) {
            return null;
        }
        final GraphQLInputType graphQLType = (GraphQLInputType) schema.getUnwrappedTypeFor(ctx, fieldType.getType());
        if (graphQLType == null) {
            return null;
        }
        ctx.addEntityFilterWiring(entityFilterCustomWiring(entity.getEntityClass(), fieldName, fieldType, filterResolver));
        return newInputObjectField()
                .name(fieldName)
                .description(description)
                .type((GraphQLInputType) unwrapNonNull(graphQLType))
                .build();
    }
}
