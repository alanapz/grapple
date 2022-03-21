package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterAndWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterFieldWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterJoinWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterNotWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterOrWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapList;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNull;
import static org.grapple.utils.Utils.applyAndValidate;
import static org.grapple.utils.Utils.coalesce;
import static org.grapple.utils.Utils.readOnlyCopy;
import static org.jooq.lambda.Seq.seq;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.core.ElementVisibility;
import org.grapple.core.Validatable;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.QueryField;
import org.grapple.reflect.EntityQueryMetadata.EntityQueryMethodMetadata;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityFieldDefinition;
import org.grapple.schema.EntityFilterItemDefinition;
import org.grapple.schema.EntityJoinDefinition;
import org.grapple.schema.EntityQueryDefinition;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.NoDuplicatesSet;
import org.grapple.utils.Utils;
import org.jetbrains.annotations.NotNull;

final class EntityDefinitionImpl<X> implements EntityDefinition<X>, Validatable {

    private final EntitySchemaImpl schema;

    private final Class<X> entityClass;

    private String entityName;

    private String description;

    private String containerName;

    private String filterName;

    private ElementVisibility visibility;

    private final Map<EntityField<X, ?>, EntityFieldDefinitionImpl<X, ?>> fields = new NoDuplicatesMap<>();

    private final Map<EntityJoin<X, ?>, EntityJoinDefinitionImpl<X, ?>> joins = new NoDuplicatesMap<>();

    private final Set<EntityFilterItemDefinitionImpl<X, ?>> customFilters = new NoDuplicatesSet<>();

    private final Set<EntityQueryDefinitionImpl<X>> queries = new NoDuplicatesSet<>();

    EntityDefinitionImpl(EntitySchemaImpl schema, Class<X> entityClass) {
        this.schema = requireNonNull(schema, "schema");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        // Initialise default values
        // XXX: TODO: FIXME: This needs to be customisable via schema
        this.entityName = entityClass.getSimpleName();
    }

    @Override
    public Class<X> getEntityClass() {
        return entityClass;
    }

    @Override
    public String getName() {
        return entityName;
    }

    @Override
    public void setName(String entityName) {
        requireNonNull(entityName, "entityName");
        this.entityName = entityName;
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
        return null;
    }

    @Override
    public void setDeprecationReason(String deprecationReason) {
        throw new UnsupportedOperationException("Cannot define deprecationReason for object types");
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
    public <T> EntityFieldDefinition<X, T> addField(EntityField<X, T> field) {
        requireNonNull(field, "field");
        final @SuppressWarnings("unchecked") EntityFieldDefinition<X, T> existing = (EntityFieldDefinitionImpl<X, T>) fields.get(field);
        if (existing != null) {
            return existing;
        }
        final EntityFieldDefinitionImpl<X, T> fieldDefinition = new EntityFieldDefinitionImpl<>(schema, this, field);
        fields.put(field, fieldDefinition);
        return fieldDefinition;
    }

    @Override
    public Map<EntityField<X, ?>, EntityFieldDefinitionImpl<X, ?>> getFields() {
        return readOnlyCopy(fields);
    }

    @Override
    public <Y> EntityJoinDefinition<X, Y> addJoin(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        final @SuppressWarnings("unchecked") EntityJoinDefinitionImpl<X, Y> existing = (EntityJoinDefinitionImpl<X, Y>) joins.get(join);
        if (existing != null) {
            return existing;
        }
        final EntityJoinDefinitionImpl<X, Y> joinDefinition = new EntityJoinDefinitionImpl<>(schema, this, join);
        joins.put(join, joinDefinition);
        return joinDefinition;
    }

    @Override
    public Map<EntityJoin<X, ?>, EntityJoinDefinitionImpl<X, ?>> getJoins() {
        return readOnlyCopy(joins);
    }

    @Override
    public <T> EntityFilterItemDefinitionImpl<X, T> addFilterItem(TypeLiteral<T> fieldType, Consumer<EntityFilterItemDefinition<X, T>> consumer) {
        requireNonNull(fieldType, "fieldType");
        requireNonNull(consumer, "consumer");
        final EntityFilterItemDefinitionImpl<X, T> filterItemDefinition = applyAndValidate(new EntityFilterItemDefinitionImpl<>(schema, this, fieldType), consumer);
        customFilters.add(filterItemDefinition);
        return filterItemDefinition;
    }

    @Override
    public Set<EntityFilterItemDefinitionImpl<X, ?>> getFilterItems() {
        return readOnlyCopy(customFilters);
    }

    @Override
    public EntityQueryDefinitionImpl<X> addQuery(Consumer<EntityQueryDefinition<X>> consumer) {
        requireNonNull(consumer, "consumer");
        final UserEntityQueryDefinitionImpl<X> queryDefinition = applyAndValidate(new UserEntityQueryDefinitionImpl<>(schema, this), consumer);
        queries.add(queryDefinition);
        return queryDefinition;
    }

    @Override
    public Set<? extends EntityQueryDefinition<X>> getQueries() {
        return unmodifiableSet(queries);
    }

    GeneratedEntityQueryDefinitionImpl<X> addGeneratedQuery(EntityQueryMethodMetadata<X> methodMetadata, EntityQueryResolver<X> queryResolver) {
        requireNonNull(methodMetadata, "methodMetadata");
        requireNonNull(queryResolver, "queryResolver");
        final GeneratedEntityQueryDefinitionImpl<X> queryDefinition = new GeneratedEntityQueryDefinitionImpl<>(schema, this, methodMetadata, queryResolver);
        queries.add(queryDefinition);
        return queryDefinition;
    }

    // We are only filterable if we have at least 1 queryable scalar field
    // We can only filter on queryable selections
    // We can currently quick filter on scalar scalars (primitives)
    boolean isFilterSupported(SchemaBuilderContext ctx) {
        return !customFilters.isEmpty() || (!fields.isEmpty() && seq(fields.values()).anyMatch(field -> field.isFilterable(ctx)));
    }

    String resolveName() {
        return coalesce(entityName, entityClass.getSimpleName());
    }

    GraphQLTypeReference getEntityTypeRef() {
        return typeRef(entityName);
    }

    GraphQLTypeReference getFilterTypeRef(SchemaBuilderContext ctx) {
        return (isFilterSupported(ctx) ? typeRef(resolveFilterName()) : null);
    }

    GraphQLTypeReference getContainerTypeRef() {
        return typeRef(resolveContainerName());
    }

    GraphQLTypeReference getOrderByTypeRef(SchemaBuilderContext ctx) {
        return ctx.getEntityOrderBy(this).getTypeRef();
    }

    String resolveContainerName() {
        return coalesce(containerName, schema.getEntityDefaultNameGenerator().generateContainerEntityName(this));
    }

    String resolveFilterName() {
        return coalesce(filterName, schema.getEntityDefaultNameGenerator().generateFilterEntityName(this));
    }

    /* package */ void build(SchemaBuilderContext ctx) {

        validate();

        if (!ctx.isSchemaElementVisible(visibility)) {
            return;
        }

        final GraphQLObjectType.Builder entityBuilder = new GraphQLObjectType.Builder()
                .name(resolveName())
                .description(description);

        for (EntityFieldDefinitionImpl<X, ?> field: fields.values()) {
            final GraphQLFieldDefinition graphQLFieldDefinition = field.build(ctx);
            if (graphQLFieldDefinition != null) {
                entityBuilder.field(graphQLFieldDefinition);
            }
        }

        for (EntityJoinDefinitionImpl<X, ?> join: joins.values()) {
            final GraphQLFieldDefinition graphQLFieldDefinition = join.build(ctx);
            if (graphQLFieldDefinition != null) {
                entityBuilder.field(graphQLFieldDefinition);
            }
        }

        ctx.addEntityType(this, entityBuilder);

        buildContainerType(ctx);
        buildOrderByType(ctx);

        final GeneratedEntityFilter<X> entityFilter = generateFilterType(ctx);
        if (entityFilter != null) {
            ctx.addEntityFilter(entityClass, entityFilter);
        } else {
            /// XXXX:
            System.out.println("Filter not available for: "  + entityClass);
        }

        for (EntityQueryDefinitionImpl<?> queryDefinition: queries) {
            queryDefinition.build(ctx);
        }
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
                        .type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLNonNull.nonNull(typeRef(resolveName()))))))
                .field(newFieldDefinition()
                        .name("total")
                        .type(Scalars.GraphQLInt)));

    }

    private EntityOrderByDefinitionImpl<X> buildOrderByType(SchemaBuilderContext ctx) {
        final EntityOrderByDefinitionImpl<X> existingType = ctx.getEntityOrderBy(this);
        if (existingType != null) {
            // Already built, nothing to do here
            return existingType;
        }

        // Make sure all listeners agree to build this type
        for (EntitySchemaListener schemaListener: schema.getSchemaListeners()) {
            if (!schemaListener.acceptEntityOrderBy(this)) {
                return null;
            }
        }

        final EntityOrderByDefinitionImpl<X> orderByDefinition = new EntityOrderByDefinitionImpl<>(schema, this);
        ctx.putEntityOrderBy(this, orderByDefinition);

        schema.getSchemaListeners().forEach(schemaListener -> schemaListener.configureEntityOrderBy(orderByDefinition));

        return orderByDefinition;
    }

    GeneratedEntityFilter<X> generateFilterType(SchemaBuilderContext ctx) {

        // If we are filterable, add our custom filter object
        if (!isFilterSupported(ctx)) {
//            return null;
        }

        final Map<String, GeneratedEntityFilterItem<X>> filterItems = new NoDuplicatesMap<>();

        fields.values().forEach(field -> appendFieldFilter(ctx, field, filterItems));
        joins.values().forEach(join -> appendJoinFilter(ctx, join, filterItems));

        if (customFilters.isEmpty() && filterItems.isEmpty()) {
//            return null;
        }

        ctx.addEntityFilterWiring(entityFilterNotWiring(entityClass, "not"));
        ctx.addEntityFilterWiring(entityFilterAndWiring(entityClass, "and"));
        ctx.addEntityFilterWiring(entityFilterOrWiring(entityClass, "or"));

        filterItems.put("not", new GeneratedEntityFilterItem<X>() {

            @Override
            public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                return (GraphQLInputType) wrapList(wrapNonNull(source.getRef()));
            }

        });

        filterItems.put("and", new GeneratedEntityFilterItem<X>() {

            @Override
            public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                return (GraphQLInputType) wrapList(wrapNonNull(source.getRef()));
            }

        });

        filterItems.put("or", new GeneratedEntityFilterItem<X>() {

            @Override
            public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                return (GraphQLInputType) wrapList(wrapNonNull(source.getRef()));
            }

        });

        return new GeneratedEntityFilter<>(resolveFilterName(), null, filterItems, customFilters);
    }

    private <T> void appendFieldFilter(SchemaBuilderContext ctx, EntityFieldDefinitionImpl<X, T> fieldDefinition, Map<String, GeneratedEntityFilterItem<X>> filterItems) {
        // Ignore non-queryable fields
        if (fieldDefinition.getQueryableField() == null) {
            return;
        }
        final QueryField<X, T> queryField = (QueryField<X, T>) fieldDefinition.getField();
        final GeneratedFieldFilter<T> fieldFilter = ctx.buildFieldFilter(queryField.getResultType());
        if (fieldFilter != null) {
            filterItems.put(fieldDefinition.getName(), new GeneratedEntityFilterItem<X>() {

                @Override
                public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                    return fieldFilter.getRef();
                }

            });

            ctx.addEntityFilterWiring(entityFilterFieldWiring(entityClass, fieldDefinition.getName(), fieldFilter.getFilterType(), queryField));
        }
    }

    private <Y> void appendJoinFilter(SchemaBuilderContext ctx, EntityJoinDefinitionImpl<X, Y> fieldDefinition, Map<String, GeneratedEntityFilterItem<X>> filterItems) {
        final EntityJoin<X, Y> entityJoin = fieldDefinition.getJoin();
        final EntityDefinitionImpl<Y> targetEntity = schema.getEntityFor(entityJoin.getResultType());
        if (targetEntity != null) {
            filterItems.put(fieldDefinition.getName(), new GeneratedEntityFilterItem<X>() {

                @Override
                public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                    return typeRef(targetEntity.resolveFilterName());
                }
            });

            ctx.addEntityFilterWiring(entityFilterJoinWiring(entityClass, fieldDefinition.getName(), targetEntity.getEntityClass(), entityJoin));
        }
    }

    @Override
    public void validate() {
        if (entityName == null || entityName.trim().isEmpty()) {
            throw new IllegalArgumentException("entityName not configured");
        }
        customFilters.forEach(EntityFilterItemDefinitionImpl::validate);
        queries.forEach(EntityQueryDefinitionImpl::validate);
    }

    @Override
    public String toString() {
        return format("%s[%s]", entityName, entityClass.getName());
    }
}
