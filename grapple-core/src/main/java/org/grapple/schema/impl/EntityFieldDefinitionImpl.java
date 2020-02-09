package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLTypeUtil.isScalar;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entitySelectionFieldWiring;

import java.util.function.Consumer;
import java.util.function.Function;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import org.grapple.query.EntityField;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.query.QueryField;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityFieldDefinition;
import org.grapple.utils.Utils;

final class EntityFieldDefinitionImpl<X, T> implements EntityFieldDefinition<X, T> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final EntityField<X, T> field;

    private String fieldName;

    private String description;

    private String deprecationReason;

    EntityFieldDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, EntityField<X, T> field) {
        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.field = requireNonNull(field, "field");
        // Initialise default values
        this.fieldName = field.getName();
        this.description = field.getMetadata(EntityMetadataKeys.Description);
        this.deprecationReason = field.getMetadata(EntityMetadataKeys.DeprecationReason);
    }

    @Override
    public EntityDefinition<X> getEntity() {
        return entity;
    }

    @Override
    public EntityField<X, T> getField() {
        return field;
    }

    @Override
    public QueryField<X, T> getQueryableField() {
        return ((field instanceof QueryField<?, ?>) ? (QueryField<X, T>) field : null);
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
    public String getDeprecationReason() {
        return deprecationReason;
    }

    @Override
    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }

    void build(SchemaBuilderContext ctx, GraphQLObjectType.Builder entityBuilder) {

        final GraphQLOutputType resultType = schema.getResultTypeFor(ctx, field.getResultType());

        // Ignore unmapped types
        if (resultType == null) {
            return;
        }

        final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type(resultType)
                .description(description)
                .deprecate(deprecationReason);

        entityBuilder.field(fieldBuilder);

        ctx.addEntitySelectionWiring(entitySelectionFieldWiring(entity.getEntityClass(), fieldName, field));

    }

    boolean isFilterable(SchemaBuilderContext ctx) {
        return (field instanceof QueryField<?, ?>) && isScalar(unwrapNonNull(schema.getResultTypeFor(ctx, field.getResultType())));
    }

    @Override
    public EntityFieldDefinition<X, T> apply(Consumer<EntityFieldDefinition<X, T>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityFieldDefinition<X, T>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}

