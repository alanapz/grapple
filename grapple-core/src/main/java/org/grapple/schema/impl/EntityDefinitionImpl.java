package org.grapple.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterAndWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterFieldWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterJoinWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterNotWiring;
import static org.grapple.schema.impl.RuntimeWiring.entityFilterOrWiring;
import static org.grapple.schema.impl.SchemaUtils.wrapList;
import static org.grapple.schema.impl.SchemaUtils.wrapNonNull;
import static org.grapple.utils.Utils.coalesce;
import static org.jooq.lambda.Seq.seq;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import org.grapple.core.Validatable;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.query.QueryField;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityCustomFilterDefinition;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityFieldDefinition;
import org.grapple.schema.EntityJoinDefinition;
import org.grapple.schema.EntityQueryDefinition;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.schema.impl.EntityQueryScanner.QueryMethodResult;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.NoDuplicatesSet;
import org.grapple.utils.Utils;

final class EntityDefinitionImpl<X> implements EntityDefinition<X>, Validatable {

    private final EntitySchemaImpl schema;

    private final Class<X> entityClass;

    private String entityName;

    private String description;

    private String containerName;

    private String filterName;

    private final Map<EntityField<X, ?>, EntityFieldDefinitionImpl<X, ?>> fields = new NoDuplicatesMap<>();

    private final Map<EntityJoin<X, ?>, EntityJoinDefinitionImpl<X, ?>> joins = new NoDuplicatesMap<>();

    private final Set<EntityCustomFilterDefinitionImpl<X, ?>> customFilters = new NoDuplicatesSet<>();

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
    @SuppressWarnings("unchecked")
    public <T> EntityFieldDefinition<X, T> addField(EntityField<X, T> field) {
        requireNonNull(field, "field");
        final EntityFieldDefinition<X, T> existing = (EntityFieldDefinitionImpl<X, T>) fields.get(field);
        if (existing != null) {
            return existing;
        }
        final EntityFieldDefinitionImpl<X, T> fieldDefinition = new EntityFieldDefinitionImpl<>(schema, this, field);
        fields.put(field, fieldDefinition);
        return fieldDefinition;
    }

    @Override
    public Map<EntityField<X, ?>, EntityFieldDefinitionImpl<X, ?>> getFields() {
        return unmodifiableMap(fields);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> EntityJoinDefinition<X, Y> addJoin(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        final EntityJoinDefinitionImpl<X, Y> existing = (EntityJoinDefinitionImpl<X, Y>) joins.get(join);
        if (existing != null) {
            return existing;
        }
        final EntityJoinDefinitionImpl<X, Y> joinDefinition = new EntityJoinDefinitionImpl<>(schema, this, join);
        joins.put(join, joinDefinition);
        return joinDefinition;
    }

    @Override
    public Map<EntityJoin<X, ?>, EntityJoinDefinitionImpl<X, ?>> getJoins() {
        return unmodifiableMap(joins);
    }

    @Override
    public <T> EntityCustomFilterDefinitionImpl<X, T> addCustomFilter(TypeLiteral<T> filterType, Consumer<EntityCustomFilterDefinition<X, T>> consumer) {
        requireNonNull(filterType, "filterType");
        requireNonNull(consumer, "consumer");
        final EntityCustomFilterDefinitionImpl<X, T> filterDefinition = Utils.applyAndValidate(new EntityCustomFilterDefinitionImpl<>(schema, this, filterType), consumer);
        customFilters.add(filterDefinition);
        return filterDefinition;
    }

    @Override
    public Set<? extends EntityCustomFilterDefinition<X, ?>> getCustomFilters() {
        return unmodifiableSet(customFilters);
    }

    @Override
    public EntityQueryDefinitionImpl<X> addQuery(Consumer<EntityQueryDefinition<X>> consumer) {
        requireNonNull(consumer, "consumer");
        final UserEntityQueryDefinitionImpl<X> queryDefinition = Utils.applyAndValidate(new UserEntityQueryDefinitionImpl<>(schema, this), consumer);
        queries.add(queryDefinition);
        return queryDefinition;
    }

    @Override
    public Set<? extends EntityQueryDefinition<X>> getQueries() {
        return unmodifiableSet(queries);
    }

    GeneratedEntityQueryDefinitionImpl<X> addGeneratedQuery(QueryMethodResult<X> methodResult, EntityQueryResolver<X> queryResolver) {
        requireNonNull(methodResult, "methodResult");
        requireNonNull(queryResolver, "queryResolver");
        final GeneratedEntityQueryDefinitionImpl<X> queryDefinition = new GeneratedEntityQueryDefinitionImpl<>(schema, this, methodResult, queryResolver);
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

    String resolveDescription() {
        return coalesce(description);
    }

    String resolveContainerName() {
        return coalesce(containerName, schema.getEntityDefaultNameGenerator().generateContainerEntityName(this));
    }

    String resolveFilterName() {
        return coalesce(filterName, schema.getEntityDefaultNameGenerator().generateFilterEntityName(this));
    }

    /* package */ void build(SchemaBuilderContext ctx) {

        validate();

        final GraphQLObjectType.Builder entityBuilder = new GraphQLObjectType.Builder().name(resolveName());

        for (EntityFieldDefinitionImpl<X, ?> fieldDefinition: fields.values()) {
            fieldDefinition.build(ctx, entityBuilder);
        }
        for (EntityJoinDefinitionImpl<X, ?> joinDefinition: joins.values()) {
            joinDefinition.build(ctx, entityBuilder);
        }

        ctx.addEntityType(this, entityBuilder);

        buildContainerType(ctx);
        buildOrderByType(ctx);

        final GeneratedEntityFilter<X> entityFilter = generateFilterType(ctx);
        if (entityFilter != null) {
            ctx.addEntityFilter(entityClass, entityFilter);
        } else
        {
//            System.out.println("NULL "  + entityClass);
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

        // XXX System.out.println("BUOILT +" + entityClass);

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
            filterItems.put(fieldDefinition.getFieldName(), new GeneratedEntityFilterItem<X>() {

                @Override
                public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                    return fieldFilter.getRef();
                }

            });

            ctx.addEntityFilterWiring(entityFilterFieldWiring(entityClass, fieldDefinition.getFieldName(), fieldFilter.getFilterType(), queryField));
        }
    }

    private <Y> void appendJoinFilter(SchemaBuilderContext ctx, EntityJoinDefinitionImpl<X, Y> fieldDefinition, Map<String, GeneratedEntityFilterItem<X>> filterItems) {
        final EntityJoin<X, Y> entityJoin = fieldDefinition.getJoin();
        final EntityDefinitionImpl<Y> targetEntity = schema.getEntityFor(entityJoin.getResultType());
        if (targetEntity != null) {
            filterItems.put(fieldDefinition.getFieldName(), new GeneratedEntityFilterItem<X>() {

                @Override
                public GraphQLInputType getInputType(GeneratedEntityFilter<X> source) {
                    return typeRef(targetEntity.resolveFilterName());
                }
            });

            ctx.addEntityFilterWiring(entityFilterJoinWiring(entityClass, fieldDefinition.getFieldName(), targetEntity.getEntityClass(), entityJoin));
        }
    }

    @Override
    public void validate() {
        if (entityName == null || entityName.trim().isEmpty()) {
            throw new IllegalArgumentException("entityName not configured");
        }
        customFilters.forEach(EntityCustomFilterDefinitionImpl::validate);
        queries.forEach(EntityQueryDefinitionImpl::validate);
    }

    @Override
    public EntityDefinition<X> apply(Consumer<EntityDefinition<X>> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<EntityDefinition<X>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }

    @Override
    public String toString() {
        return format("%s[%s]", entityName, entityClass.getName());
    }

}
