package org.grapple.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.Filters;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;

final class EntityDefinitionImpl<X> implements EntityDefinition<X> {

    private final EntitySchemaImpl schema;

    private final Class<X> entityClass;

    private String name;

    private String description;

    private String containerName;

    private String filterName;

    private final Map<EntityField<X, ?>, EntityFieldDefinitionImpl<X, ?>> fields = new LinkedHashMap<>();

    private final Map<EntityJoin<X, ?>, EntityJoinDefinitionImpl> joins = new LinkedHashMap<>();

    private final Map<String, EntityFilterDefinition> filters = new LinkedHashMap<>();

    EntityDefinitionImpl(EntitySchemaImpl schema, Class<X> entityClass) {
        this.schema = requireNonNull(schema, "schema");
        this.entityClass = requireNonNull(entityClass, "entityClass");
    }

    @Override
    public EntityDefinition<X> setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public EntityDefinition<X> setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public EntityDefinition<X> addField(EntityField<X, ?> field) {
        return addField(field, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> EntityDefinition<X> addField(EntityField<X, T> field, Consumer<EntityFieldDefinition> consumer) {
        requireNonNull(field, "field");
        final EntityFieldDefinitionImpl<X, T> fieldDefinition = (EntityFieldDefinitionImpl<X, T>) fields.computeIfAbsent(field, unused -> new EntityFieldDefinitionImpl<>(field, schema));
        if (consumer != null) {
            consumer.accept(fieldDefinition);
        }
        return this;
    }

    @Override
    public Map<EntityField<X, ?>, EntityFieldDefinition> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public EntityDefinition<X> addJoin(EntityJoin<X, ?> join) {
        return addJoin(join, null);
    }

    @Override
    public EntityDefinition<X> addJoin(EntityJoin<X, ?> join, Consumer<EntityJoinDefinition> consumer) {
        requireNonNull(join, "join");
        final EntityJoinDefinitionImpl joinDefinition = joins.computeIfAbsent(join, unused -> new EntityJoinDefinitionImpl(join, schema));
        if (consumer != null) {
            consumer.accept(joinDefinition);
        }
        return this;
    }

    @Override
    public Map<EntityJoin<X, ?>, EntityJoinDefinition> getJoins() {
        return Collections.unmodifiableMap(joins);
    }

    @Override
    public EntityDefinition<X> importFrom(Class<?> definitionClass) {
        requireNonNull(definitionClass, "definitionClass");
        for (Field field : definitionClass.getFields()) {
            try {
                importFrom(field.get(null));
            }
            catch (IllegalAccessException e) {
                // Skip field..
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private void importFrom(Object object) {
        if (object instanceof EntityField<?, ?>) {
            addField((EntityField<X, ?>) object);
            return;
        }
        if (object instanceof EntityJoin<?, ?>) {
            addJoin((EntityJoin<X, ?>) object);
            return;
        }
        if (object instanceof Object[]) {
            Arrays.asList((Object[]) object).forEach(this::importFrom);
            return;
        }
        if (object instanceof Collection<?>) {
            ((Collection<?>) object).forEach(this::importFrom);
            return;
        }
        if (object instanceof Map<?, ?>) {
            ((Map<?, ?>) object).values().forEach(this::importFrom);
            return;
        }
    }

    // We are only filterable if we have at least 1 queryable scalar field
    // We can only filter on queryable selections
    // We can currently quick filter on scalar scalars (primitives)
    boolean isFilterSupported(SchemaBuilderContext ctx) {
        return !fields.isEmpty() && fields.values().stream().anyMatch(field -> field.isFilterable(ctx));
    }

    @Override
    public String toString() {
        return format("%s=%s", entityClass.getName(), name);
    }

    String resolveName() {
        return coalesce(name, entityClass.getSimpleName());
    }

    String resolveDescription() {
        return coalesce(description);
    }

    String resolveContainerName() {
        return coalesce(containerName, schema.resolveContainerName(resolveName()));
    }

    String resolveFilterName() {
        return coalesce(filterName, schema.resolveFilterName(resolveName()));
    }

    /* package */ void build(SchemaBuilderContext ctx) {

        final GraphQLObjectType.Builder entityBuilder = new GraphQLObjectType.Builder().name(resolveName());

        for (EntityFieldDefinitionImpl fieldDefinition: fields.values()) {
            fieldDefinition.build(ctx, entityBuilder);
        }
        for (EntityJoinDefinitionImpl joinDefinition: joins.values()) {
            joinDefinition.build(ctx, entityBuilder);
        }

        ctx.addEntityType(this, entityBuilder);

        buildContainerType(ctx);
        buildFilterType(ctx);
    }

    private void buildContainerType(SchemaBuilderContext ctx) {
        // Responsible for building the XXXResults pseudo-type (results T[], total_results int)
        ctx.addContainerType(this, new GraphQLObjectType.Builder()
                .name(resolveContainerName())
                .field(newFieldDefinition()
                        .name("offset")
                        .type(Scalars.GraphQLInt))
                .field(newFieldDefinition()
                        .name("count")
                        .type(Scalars.GraphQLInt))
                .field(newFieldDefinition()
                        .name("results")
                        .type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(resolveName()))))))
                .field(newFieldDefinition()
                        .name("total")
                        .type(Scalars.GraphQLInt)));

    }

    @SuppressWarnings("unchecked")
    private void buildFilterType(SchemaBuilderContext ctx) {
        // If we are filterable, add our custom filter object
        if (!isFilterSupported(ctx)) {
            return;
        }

        final List<EntityFilterDefinition<X>> filterDefinitions = new ArrayList<>();

        for (EntityFieldDefinitionImpl<X, ?> fieldDefinition: fields.values()) {
            fieldDefinition.buildAdditionalQuickFilter(ctx, filterDefinitions);
        }

        final GraphQLTypeReference filterType = GraphQLTypeReference.typeRef(resolveFilterName());

        if (!filterDefinitions.isEmpty()) {

            filterDefinitions.add(EntityFilterDefinition.of(
                    "OR",
                    GraphQLList.list(GraphQLNonNull.nonNull(filterType)),
                    args -> {
                        final List<EntityFilter<X>> allEntityFilters = new ArrayList<>();
                        for (Map<String, Object> filterItem : (List<Map<String, Object>>) args) {
                            allEntityFilters.add(ctx.applyEntityFilter(this, filterItem));
                        }
                        return Filters.or(allEntityFilters);
                    }));

            filterDefinitions.add(EntityFilterDefinition.of(
                    "AND",
                    GraphQLList.list(GraphQLNonNull.nonNull(filterType)),
                    args -> {
                        final List<EntityFilter<X>> allEntityFilters = new ArrayList<>();
                        for (Map<String, Object> filterItem : (List<Map<String, Object>>) args) {
                            allEntityFilters.add(ctx.applyEntityFilter(this, filterItem));
                        }
                        return Filters.and(allEntityFilters);
                    }));

            filterDefinitions.add(EntityFilterDefinition.of(
                    "NOT",
                    GraphQLList.list(GraphQLNonNull.nonNull(filterType)),
                    args -> {
                        final List<EntityFilter<X>> allEntityFilters = new ArrayList<>();
                        for (Map<String, Object> filterItem : (List<Map<String, Object>>) args) {
                            allEntityFilters.add(Filters.not(ctx.applyEntityFilter(this, filterItem)));
                        }
                        return Filters.and(allEntityFilters);
                    }));
        }

        final List<GraphQLInputObjectField> filterFields = new ArrayList<>();

        for (EntityFilterDefinition<X> filterDefinition: filterDefinitions) {
            filterFields.add(newInputObjectField().name(filterDefinition.getName()).type(filterDefinition.getType()).build());
            ctx.addFieldFilter(this, filterDefinition.getName(), filterDefinition);
        }

        ctx.addFilterType(this, new GraphQLInputObjectType.Builder()
                .name(filterType.getName())
                .fields(filterFields));
    }

    @Override
    public EntityDefinition<X> apply(Consumer<EntityDefinition<X>> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }
}
