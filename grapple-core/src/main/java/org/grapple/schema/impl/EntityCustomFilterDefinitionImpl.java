package org.grapple.schema.impl;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterCustomWiring;
import static org.grapple.utils.Utils.coalesce;

import java.util.function.Consumer;
import java.util.function.Function;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityCustomFilterDefinition;
import org.grapple.schema.EntityCustomFilterResolver;
import org.grapple.schema.EntityDefinition;
import org.grapple.utils.Utils;

final class EntityCustomFilterDefinitionImpl<X, T> implements EntityCustomFilterDefinition<X, T>, Validatable {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final TypeLiteral<T> fieldType;

    private String fieldName;

    private String description;

    private GraphQLInputType forcedGraphQLType;

    private EntityCustomFilterResolver<X, T> filterResolver;

    EntityCustomFilterDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, TypeLiteral<T> fieldType) {
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
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
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
    public GraphQLInputType getForcedGraphQLType() {
        return forcedGraphQLType;
    }

    @Override
    public void setForcedGraphQLType(GraphQLInputType forcedGraphQLType) {
        this.forcedGraphQLType = forcedGraphQLType;
    }

    @Override
    public EntityCustomFilterResolver<X, T> getFilterResolver() {
        return filterResolver;
    }

    @Override
    public void setFilterResolver(EntityCustomFilterResolver<X, T> filterResolver) {
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

    void build(SchemaBuilderContext ctx, GraphQLInputObjectType.Builder inputObjectBuilder) {
        validate();
        final GraphQLInputType graphQLType = coalesce(forcedGraphQLType, (GraphQLInputType) schema.getUnwrappedTypeFor(ctx,fieldType.getType()));
        if (graphQLType != null) {
            inputObjectBuilder.field(newInputObjectField().name(fieldName).description(description).type(graphQLType));
            ctx.addEntityFilterWiring(entityFilterCustomWiring(entity.getEntityClass(), fieldName, fieldType, filterResolver));
        }
    }

    @Override
    public EntityCustomFilterDefinitionImpl<X, T> apply(Consumer<EntityCustomFilterDefinition<X, T>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityCustomFilterDefinition<X, T>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}

