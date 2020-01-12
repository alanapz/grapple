package org.grapple.schema;

import java.util.function.Consumer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;

final class EntityJoinDefinitionImpl implements EntityJoinDefinition {

    private final EntityJoin<?, ?> join;

    private final EntitySchemaImpl schema;

    private String name;

    private String description;

    private boolean deprecated;

    private String deprecationReason;

    EntityJoinDefinitionImpl(EntityJoin<?, ?> join, EntitySchemaImpl schema) {
        this.join = requireNonNull(join, "join");
        this.schema = requireNonNull(schema, "schema");
        // Initialise default values
        this.name = join.getName();
        this.description = join.getMetadata(EntityMetadataKeys.DESCRIPTION);
        this.deprecated = coalesce(join.getMetadata(EntityMetadataKeys.IS_DEPRECATED), false);
        this.deprecationReason = coalesce(join.getMetadata(EntityMetadataKeys.DEPRECATION_REASON), "Deprecated");
    }

    @Override
    public EntityJoinDefinition setName(String name) {
        requireNonNull(name, "name");
        this.name = name;
        return this;
    }

    @Override
    public EntityJoinDefinition setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public EntityJoinDefinition setIsDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    @Override
    public EntityJoinDefinition setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
        return this;
    }

    void build(SchemaBuilderContext ctx, GraphQLObjectType.Builder entityBuilder) {
        final EntityDefinitionImpl<?> targetEntity = schema.getEntityFor(join.getResultType());
        if (targetEntity != null) {
            final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                    .name(name)
                    .type(SchemaUtils.wrapOutputType(join.getResultType(), GraphQLTypeReference.typeRef(targetEntity.resolveName())))
                    .description(description)
                    .deprecate(deprecated ? deprecationReason : null);

            if (targetEntity.isFilterSupported(ctx)) {
                fieldBuilder.argument(newArgument().name("filter").type(GraphQLTypeReference.typeRef(targetEntity.resolveFilterName())).build());
            }
            entityBuilder.field(fieldBuilder);
        }
    }

    @Override
    public EntityJoinDefinition apply(Consumer<EntityJoinDefinition> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }
}

