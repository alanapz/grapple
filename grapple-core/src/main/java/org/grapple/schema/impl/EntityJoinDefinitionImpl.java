package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entitySelectionJoinWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNullIfNecessary;

import graphql.schema.GraphQLFieldDefinition;
import org.grapple.core.ElementVisibility;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.schema.EntityJoinDefinition;

final class EntityJoinDefinitionImpl<X, Y> implements EntityJoinDefinition<X, Y> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> entity;

    private final EntityJoin<X, Y> join;

    private String fieldName;

    private String description;

    private String deprecationReason;

    private ElementVisibility visibility;

    EntityJoinDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> entity, EntityJoin<X, Y> join) {
        this.schema = requireNonNull(schema, "schema");
        this.entity = requireNonNull(entity, "entity");
        this.join = requireNonNull(join, "join");
        // Initialise default values
        this.fieldName = join.getName();
        this.description = join.getMetadata(EntityMetadataKeys.Description);
        this.deprecationReason = join.getMetadata(EntityMetadataKeys.DeprecationReason);
        this.visibility = join.getMetadata(EntityMetadataKeys.Visibility);
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

    GraphQLFieldDefinition build(SchemaBuilderContext ctx) {
        final EntityDefinitionImpl<?> targetEntity = schema.getEntityFor(join.getResultType());
        if (targetEntity == null) {
            // Ignore joins to entites not found
            return null;
        }
        ctx.addEntitySelectionWiring(entitySelectionJoinWiring(entity.getEntityClass(), fieldName, getJoinedEntity().getEntityClass(), join));
        if (visibility != null) {
            ctx.getSchemaBuilderElementVisibility().setFieldVisibility(entity.getName(), fieldName, visibility);
        }
        return newFieldDefinition()
                .name(fieldName)
                .type(wrapNonNullIfNecessary(join.getResultType(), targetEntity.getEntityTypeRef()))
                .description(description)
                .deprecate(deprecationReason)
                .build();
    }
}
