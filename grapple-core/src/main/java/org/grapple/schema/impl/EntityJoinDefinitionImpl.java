package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entitySelectionJoinWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNullIfNecessary;

import java.util.function.Consumer;
import java.util.function.Function;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.schema.EntityJoinDefinition;
import org.grapple.utils.Utils;

final class EntityJoinDefinitionImpl<X, Y> implements EntityJoinDefinition<X, Y> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final EntityJoin<X, Y> join;

    private String fieldName;

    private String description;

    private String deprecationReason;

    EntityJoinDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, EntityJoin<X, Y> join) {
        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.join = requireNonNull(join, "join");
        // Initialise default values
        this.fieldName = join.getName();
        this.description = join.getMetadata(EntityMetadataKeys.Description);
        this.deprecationReason = join.getMetadata(EntityMetadataKeys.DeprecationReason);
    }

    @Override
    public EntityDefinitionImpl<X> getEntity() {
        return entity;
    }

    @Override
    public EntityJoin<X, Y> getJoin() {
        return join;
    }

    @Override
    public EntityDefinitionImpl<Y> getJoinedEntity() {
        return schema.getEntityFor(join.getResultType());
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
        final EntityDefinitionImpl<?> targetEntity = schema.getEntityFor(join.getResultType());

        if (targetEntity == null) {
            return;
        }

        final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type(wrapNonNullIfNecessary(join.getResultType(), targetEntity.getEntityTypeRef()))
                .description(description)
                .deprecate(deprecationReason);

        entityBuilder.field(fieldBuilder);

        ctx.addEntitySelectionWiring(entitySelectionJoinWiring(entity.getEntityClass(), fieldName, getJoinedEntity().getEntityClass(), join));
    }

    @Override
    public EntityJoinDefinition<X, Y> apply(Consumer<EntityJoinDefinition<X, Y>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityJoinDefinition<X, Y>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}

