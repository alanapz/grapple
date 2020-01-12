package org.grapple.schema;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityMetadataKeys;
import org.grapple.query.Filters;
import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryField;

final class EntityFieldDefinitionImpl<X, T> implements EntityFieldDefinition {

    private final EntityField<X, T> field;

    private final EntitySchemaImpl schema;

    private String name;

    private String description;

    private boolean deprecated;

    private String deprecationReason;

    EntityFieldDefinitionImpl(EntityField<X, T> field, EntitySchemaImpl schema) {
        this.field = requireNonNull(field, "field");
        this.schema = requireNonNull(schema, "schema");
        // Initialise default values
        this.name = field.getName();
        this.description = field.getMetadata(EntityMetadataKeys.DESCRIPTION);
        this.deprecated = coalesce(field.getMetadata(EntityMetadataKeys.IS_DEPRECATED), false);
        this.deprecationReason = coalesce(field.getMetadata(EntityMetadataKeys.DEPRECATION_REASON), "Deprecated");
    }

    @Override
    public EntityFieldDefinition setName(String name) {
        requireNonNull(name, "name");
        this.name = name;
        return this;
    }

    @Override
    public EntityFieldDefinition setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public EntityFieldDefinition setIsDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    @Override
    public EntityFieldDefinition setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
        return this;
    }

    void build(SchemaBuilderContext ctx, GraphQLObjectType.Builder entityBuilder) {
        final GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(name)
                .type(SchemaUtils.wrapOutputType(field.getResultType(), schema.getUnwrappedTypeFor(ctx, field.getResultType().getJavaType())))
                .description(description)
                .deprecate(deprecated ? deprecationReason : null);

        entityBuilder.field(fieldBuilder);
    }

    boolean isFilterable(SchemaBuilderContext ctx) {
        return (field instanceof QueryField<?, ?>) && schema.getUnwrappedTypeFor(ctx, field.getResultType().getJavaType()) instanceof GraphQLScalarType;
    }

    @SuppressWarnings("unchecked")
    void buildAdditionalQuickFilter(SchemaBuilderContext ctx, List<EntityFilterDefinition<X>> filters) {

        // We can only filter on queryable selections
        if (!(field instanceof QueryField<?, ?>)) {
            return;
        }
        final GraphQLType queryType = schema.getUnwrappedTypeFor(ctx, field.getResultType().getJavaType());

        // We can currently filter on scalar scalars (primitives)
        if (!(queryType instanceof GraphQLScalarType)) {
            return;
        }
        final Class<?> javaType = field.getResultType().getJavaType();
        final GraphQLScalarType scalarType = (GraphQLScalarType) queryType;
        final QueryField<X, T> queryField = (QueryField<X, T>) field;

        filters.add(EntityFilterDefinition.of(
                name,
                scalarType,
                args -> Filters.isEqual(queryField, (T) args)));

        if (javaType != boolean.class && javaType != Boolean.class) {

            filters.add(EntityFilterDefinition.of(
                    format("%s_in", name),
                    GraphQLList.list(scalarType),
                    args -> Filters.contains(queryField, new HashSet<>((Collection<T>) args))));
        }

        if (String.class.isAssignableFrom(javaType)) {

            filters.add(EntityFilterDefinition.of(
                    format("%s_like", name),
                    scalarType,
                    args -> new EntityFilter<X>() {

                        @Override
                        @SuppressWarnings("unchecked")
                        public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                            return builder.like((Expression<String>) ctx.get(queryField), (String) args);
                        }

                        @Override
                        public String toString() {
                            return format("%s LIKE %s", name, args);
                        }
                    }));
        }
        if (Number.class.isAssignableFrom(javaType) || javaType == int.class || javaType == long.class || javaType == float.class || javaType == double.class) {

            filters.add(EntityFilterDefinition.of(
                    format("%s_lt", name),
                    scalarType,
                    args -> new EntityFilter<X>() {

                        @Override
                        public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                            return applyUnsafe(ctx, builder);
                        }

                        @SuppressWarnings("unchecked")
                        private <Y extends Comparable<Y>> Predicate applyUnsafe(EntityContext<X> ctx, QueryBuilder builder) {
                            return builder.lessThan((Expression<Y>) ctx.get(queryField), (Y) args);
                        }

                        @Override
                        public String toString() {
                            return format("%s < %s", name, args);
                        }
                    }));

            filters.add(EntityFilterDefinition.of(
                    format("%s_lte", name),
                    scalarType,
                    args -> new EntityFilter<X>() {

                        @Override
                        public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                            return applyUnsafe(ctx, builder);
                        }

                        @SuppressWarnings("unchecked")
                        private <Y extends Comparable<Y>> Predicate applyUnsafe(EntityContext<X> ctx, QueryBuilder builder) {
                            return builder.lessThanOrEqualTo((Expression<Y>) ctx.get(queryField), (Y) args);
                        }

                        @Override
                        public String toString() {
                            return format("%s <= %s", name, args);
                        }
                    }));

            filters.add(EntityFilterDefinition.of(
                    format("%s_gt", name),
                    scalarType,
                    args -> new EntityFilter<X>() {

                        @Override
                        public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                            return applyUnsafe(ctx, builder);
                        }

                        @SuppressWarnings("unchecked")
                        private <Y extends Comparable<Y>> Predicate applyUnsafe(EntityContext<X> ctx, QueryBuilder builder) {
                            return builder.greaterThan((Expression<Y>) ctx.get(queryField), (Y) args);
                        }

                        @Override
                        public String toString() {
                            return format("%s > %s", name, args);
                        }
                    }));

            filters.add(EntityFilterDefinition.of(
                    format("%s_gte", name),
                    scalarType,
                    args -> new EntityFilter<X>() {

                        @Override
                        public Predicate apply(EntityContext<X> ctx, QueryBuilder builder) {
                            return applyUnsafe(ctx, builder);
                        }

                        @SuppressWarnings("unchecked")
                        private <Y extends Comparable<Y>> Predicate applyUnsafe(EntityContext<X> ctx, QueryBuilder builder) {
                            return builder.greaterThanOrEqualTo((Expression<Y>) ctx.get(queryField), (Y) args);
                        }

                        @Override
                        public String toString() {
                            return format("%s >= %s", name, args);
                        }
                    }));
        }
    }

    @Override
    public EntityFieldDefinition apply(Consumer<EntityFieldDefinition> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }
        return this;
    }
}

