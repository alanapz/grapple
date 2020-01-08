package org.grapple.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.query.QueryField;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

final class EntityDefinitionImpl<X> implements EntityDefinition<X> {

    private final EntitySchemaImpl schema;

    private final Class<X> entityClass;

    private String name;

    private final Set<EntityField<X, ?>> fields = new HashSet<>();

    private final List<EntityJoin<X, ?>> joins = new ArrayList<>();

    private final List<EntityJoin<X, ?>> filters = new ArrayList<>();

    EntityDefinitionImpl(EntitySchemaImpl schema, Class<X> entityClass) {
        this.schema = requireNonNull(schema, "schema");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        this.name = entityClass.getSimpleName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EntityDefinition<X> setName(String name) {
        this.name = requireNonNull(name, "name");
        return this;
    }

    @Override
    public EntityDefinition<X> addField(EntityField<X, ?> field) {
        fields.add(requireNonNull(field, "field"));
        return this;
    }

    @Override
    public EntityDefinition<X> addJoin(EntityJoin<X, ?> join) {
        joins.add(requireNonNull(join, "join"));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityDefinition<X> importFrom(Class<?> definitionClass) {
        requireNonNull(definitionClass, "definitionClass");
        for (Field field: definitionClass.getFields()) {
            final EntityField<X, ?> entityField = (EntityField<X, ?>) getFieldOfType(EntityField.class, field, null);
            if (entityField != null) {
                addField(entityField);
            }
            final EntityJoin<X, ?> entityJoin = (EntityJoin<X, ?>) getFieldOfType(EntityJoin.class, field, null);
            if (entityJoin != null) {
                addJoin(entityJoin);
            }
        }
        return this;
    }

    private static <T> T getFieldOfType(Class<T> fieldType, Field field, Object object) {
        if (!fieldType.isAssignableFrom(field.getType())) {
            return null;
        }
        try {
            final Object value = field.get(object);
            return (value != null ? fieldType.cast(value) : null);
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public EntityDefinition<X> apply(Consumer<EntityDefinition<X>> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }

    String getContainerTypeName() {
        return format("%sResults", getName());
    }

    // We are only filterable if we have at least 1 queryable scalar field
    // We can only filter on queryable selections
    // We can currently quick filter on scalar types (primitives)
    boolean isFilterSupported() {
        return !fields.isEmpty() && fields.stream().anyMatch(field -> (field instanceof QueryField<?, ?>) && (schema.getRawTypeFor(field.getResultType().getJavaType()) instanceof GraphQLScalarType));
    }

    String getFilterTypeName() {
        return format("%sFilter", getName());
    }

    @Override
    public String toString() {
        return format("%s=%s", entityClass.getName(), name);
    }

    /* package */ void build(SchemaBuilderContext ctx) {

        final GraphQLObjectType.Builder entityBuilder = new GraphQLObjectType.Builder().name(name);

        for (EntityField<X, ?> field : fields) {
            entityBuilder.field(newFieldDefinition()
                    .name(field.getName())
                    .description(field.getMetadata(EntityMetadataKeys.DESCRIPTION))
                    .deprecate(field.getMetadata(EntityMetadataKeys.DEPRECATED) ? "DEPRECATED" : null)
                    .type(SchemaUtils.wrapOutputType(field.getResultType(), schema.getRawTypeFor(field.getResultType().getJavaType()))));
        }

        for (EntityJoin<X, ?> join : joins) {
            final EntityDefinitionImpl<?> targetEntity = schema.getEntity(join.getResultType());
            if (targetEntity != null) {
                final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                        .name(join.getName())
                        .type(SchemaUtils.wrapOutputType(join.getResultType(), GraphQLTypeReference.typeRef(targetEntity.getName())));
                if (targetEntity.isFilterSupported()) {
                    fieldBuilder.argument(newArgument().name("filter").type(GraphQLTypeReference.typeRef(targetEntity.getFilterTypeName())).build());
                }
                entityBuilder.field(fieldBuilder);
            }
        }

        ctx.addEntityType(this, entityBuilder);

        // Responsible for building the XXXResults pseudo-type (results T[], total_results int)
        ctx.addContainerType(this, new GraphQLObjectType.Builder()
                .name(getContainerTypeName())
                .field(newFieldDefinition()
                        .name("offset")
                        .type(Scalars.GraphQLInt))
                .field(newFieldDefinition()
                        .name("count")
                        .type(Scalars.GraphQLInt))
                .field(newFieldDefinition()
                        .name("results")
                        .type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(getName()))))))
                .field(newFieldDefinition()
                        .name("total")
                        .type(Scalars.GraphQLInt)));

        // If we are filterable, add our custom filter object
        if (isFilterSupported()) {
            final GraphQLTypeReference filterType = GraphQLTypeReference.typeRef(getFilterTypeName());
            ctx.addFilterType(this, new GraphQLInputObjectType.Builder()
                    .name(getFilterTypeName())
//                    .field(newInputObjectField()
//                            .name("AND")
//                            .type(GraphQLList.list(GraphQLNonNull.nonNull(filterType))))
//                    .field(newInputObjectField()
//                            .name("OR")
//                            .type(GraphQLList.list(GraphQLNonNull.nonNull(filterType))))
//                    .field(newInputObjectField()
//                            .name("NOT")
//                            .type(filterType))
                    .fields(buildAdditionalFilterFields()));
        }
    }

    private List<GraphQLInputObjectField> buildAdditionalFilterFields() {
        final List<GraphQLInputObjectField> filter = new ArrayList<>();
        for (EntityField<X, ?> field : fields) {
            // We can only filter on queryable selections
            if (!(field instanceof QueryField<?, ?>)) {
                continue;
            }
            final GraphQLType queryType = schema.getRawTypeFor(field.getResultType().getJavaType());
            // We can currently filter on scalar types (primitives)
            if (!(queryType instanceof GraphQLScalarType)) {
                continue;
            }
            final GraphQLScalarType scalarType = (GraphQLScalarType) queryType;
            filter.add(newInputObjectField()
                    .name(field.getName())
                    .type(scalarType)
                    .build());
            final Class<?> javaType = field.getResultType().getJavaType();
            if (javaType != boolean.class && javaType != Boolean.class) {
                filter.add(newInputObjectField()
                        .name(format("%s_in", field.getName()))
                        .type(GraphQLList.list(queryType))
                        .build());
            }
            if (String.class.isAssignableFrom(javaType)) {
                filter.add(newInputObjectField()
                        .name(format("%s_like", field.getName()))
                        .type(scalarType)
                        .build());
            }
            if (Number.class.isAssignableFrom(javaType)) {
                filter.add(newInputObjectField()
                        .name(format("%s_lt", field.getName()))
                        .type(scalarType)
                        .build());
                filter.add(newInputObjectField()
                        .name(format("%s_lte", field.getName()))
                        .type(scalarType)
                        .build());
                filter.add(newInputObjectField()
                        .name(format("%s_gt", field.getName()))
                        .type(scalarType)
                        .build());
                filter.add(newInputObjectField()
                        .name(format("%s_gte", field.getName()))
                        .type(scalarType)
                        .build());
            }
        }
        return filter;
    }
}

