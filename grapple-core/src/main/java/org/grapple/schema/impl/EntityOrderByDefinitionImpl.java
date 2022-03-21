package org.grapple.schema.impl;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityOrderByFieldWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityOrderByJoinWiring;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.QueryField;
import org.grapple.query.SortDirection;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityFieldDefinition;
import org.grapple.schema.EntityJoinDefinition;
import org.grapple.schema.EntityOrderByDefinition;

final class EntityOrderByDefinitionImpl<X> implements EntityOrderByDefinition<X> {

    private final EntitySchemaImpl schema;

    private final EntityDefinitionImpl<X> parent;

    private String typeName;

    EntityOrderByDefinitionImpl(EntitySchemaImpl schema, EntityDefinitionImpl<X> parent) {
        this.schema = requireNonNull(schema, "schema");
        this.parent = requireNonNull(parent, "parent");
        this.typeName = schema.getEntityDefaultNameGenerator().generateOrderByEntityName(parent);
    }

    @Override
    public EntityDefinition<X> getParent() {
        return parent;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public void setTypeName(String typeName) {
        requireNonNull(typeName, "typeName");
        this.typeName = typeName;
    }

    GraphQLTypeReference getTypeRef() {
        return typeRef(typeName);
    }

    // Called by SchemaBuilderContext to build our type
    GraphQLInputObjectType build(SchemaBuilderContext ctx) {
        final GraphQLInputObjectType.Builder builder = newInputObject().name(typeName);
        for (EntityFieldDefinitionImpl<X, ?> field: parent.getFields().values()) {
            buildFieldDefinition(ctx, builder, field);
        }
        for (EntityJoinDefinitionImpl<X, ?> join: parent.getJoins().values()) {
            buildJoinDefinition(ctx, builder, join);
        }
        return builder.build();
    }

    // Used to build fields in our order-by object that are queryable properties
    // The type of the field will be an instance of our SortDirection class
    private <T> void buildFieldDefinition(SchemaBuilderContext ctx, GraphQLInputObjectType.Builder builder, EntityFieldDefinition<X, T> field) {
        final QueryField<X, T> queryField = field.getQueryableField();
        if (queryField != null) {
            builder.field(newInputObjectField().name(field.getName()).type((GraphQLInputType) unwrapNonNull(ctx.getUnwrappedTypeFor(SortDirection.class))));
            ctx.addEntityOrderByWiring(entityOrderByFieldWiring(parent.getEntityClass(), field.getName(), queryField));
        }
    }

    // Used to build fields in our order-by object that join to another order-by object
    // The type of the field will be the the order-by type of joined entity
    private <Y> void buildJoinDefinition(SchemaBuilderContext ctx, GraphQLInputObjectType.Builder builder, EntityJoinDefinition<X, Y> join) {
        final EntityDefinitionImpl<Y> joinedEntity = (EntityDefinitionImpl<Y>) join.getJoinedEntity();
        if (joinedEntity != null) {
            final EntityOrderByDefinitionImpl<?> joinedOrderBy = ctx.getEntityOrderBy(joinedEntity);
            if (joinedOrderBy != null) {
                builder.field(newInputObjectField().name(join.getName()).type(joinedOrderBy.getTypeRef()));
                ctx.addEntityOrderByWiring(entityOrderByJoinWiring(parent.getEntityClass(), joinedEntity.getEntityClass(), join.getName(), join.getJoin()));
            }
        }
    }
}

