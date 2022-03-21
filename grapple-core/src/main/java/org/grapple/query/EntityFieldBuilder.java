package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.EntityResultType.entityResultType;
import static org.grapple.query.EntityResultType.nonNull;
import static org.grapple.utils.Utils.coalesce;
import static org.grapple.utils.Utils.markAsUsed;
import static org.grapple.utils.Utils.requireNonNullArgument;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

import org.grapple.core.Chainable;
import org.grapple.core.MetadataKey;
import org.grapple.utils.LazyValue;
import org.grapple.utils.MetadataValues;

import org.jetbrains.annotations.NotNull;

public final class EntityFieldBuilder {

    private EntityFieldBuilder() {

    }

    public static <X, T> QueryField<X, T> literalField(@NotNull Consumer<LiteralFieldBuilder<X, T>> fieldBuilder) {
        final LiteralFieldBuilder<X, T> builder = new LiteralFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNullArgument(builder.name, "name required");
        final Class<T> resultType = requireNonNullArgument(builder.resultType, "result type required");
        final T value = builder.value;
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return entityResultType(resultType, (value == null));
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return tuple -> value;
            }

            @Override
            public Expression<T> getExpression(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return (value == null ? queryBuilder.nullLiteral(resultType) : queryBuilder.literal(value));
            }

            @Override
            public Expression<T> getOrderBy(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return getExpression(ctx, queryBuilder);
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return null;
            }
        };
    }

    public static <X, T> QueryField<X, T> attributeField(@NotNull SingularAttribute<? super X, T> attribute) {
        return attributeField(attribute, null);
    }

    public static <X, T> QueryField<X, T> attributeField(@NotNull SingularAttribute<? super X, T> attribute, Consumer<AttributeFieldBuilder<X, T>> fieldBuilder) {
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = coalesce(builder.name, attribute.getName());
        final MetadataValues metadata = requireNonNull(builder.metadata, "metadata");
        metadata.putDefault(EntityMetadataKeys.Description, QueryUtils.getDefaultDescription(attribute));
        metadata.putDefault(EntityMetadataKeys.DeprecationReason, QueryUtils.getDefaultDeprecationReason(attribute));
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            public Class<? super X> getEntityType() {
                return attribute.getDeclaringType().getJavaType();
            }

            @Override
            public EntityResultType<T> getResultType() {
                return parseAttributeResultType(attribute, builder.nullAllowed);
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                final Path<T> attributePath = ctx.addSelection(getExpression(ctx, queryBuilder));
                markAsUsed(attributePath);
                return tuple -> tuple.get(attributePath);
            }

            @Override
            public Path<T> getExpression(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return ctx.get(attribute);
            }

            @Override
            public Path<T> getOrderBy(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return getExpression(ctx, queryBuilder);
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return metadata.get(metadataKey);
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, getResultType());
            }
        };
    }

    private static <T> EntityResultType<T> parseAttributeResultType(@NotNull SingularAttribute<?, T> attribute, Boolean nullAllowed) {
        final Class<T> javaType = attribute.getJavaType();
        // Primitives must be non-null
        if (javaType.isPrimitive()) {
            if (Boolean.TRUE.equals(nullAllowed)) {
                throw new IllegalArgumentException(format("Attribute '%s' cannot be nullable (as has primitive type: '%s')", attribute.getName(), attribute.getJavaType().getName()));
            }
            return nonNull(javaType);
        }
        return entityResultType(javaType, coalesce(nullAllowed, attribute.isOptional()));
    }

    public static <X, T> QueryField<X, T> expressionField(@NotNull Consumer<ExpressionFieldBuilder<X, T>> fieldBuilder) {
        final ExpressionFieldBuilder<X, T> builder = new ExpressionFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNullArgument(builder.name, "name required");
        final EntityResultType<T> resultType = requireNonNullArgument(builder.resultType, "result type required");
        final ExpressionResolver<X, T> expressionResolver = requireNonNullArgument(builder.expression, "expressionResolver required");
        final ExpressionOrderByResolver<X> orderBy = coalesce(builder.orderBy, expressionResolver::get);
        final MetadataValues metadata = requireNonNull(builder.metadata, "metadata");
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return resultType;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                final Expression<T> expression = ctx.addSelection(getExpression(ctx, queryBuilder));
                markAsUsed(expression); // Do not remove
                return tuple -> tuple.get(expression);
            }

            @Override
            public Expression<T> getExpression(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return queryBuilder.wrapPredicateIfNecessary(expressionResolver.get(ctx, queryBuilder));
            }

            @Override
            public Expression<?> getOrderBy(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder) {
                return orderBy.get(ctx, queryBuilder);
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return metadata.get(metadataKey);
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, resultType);
            }
        };
    }

    public static <X, T> NonQueryField<X, T> nonQueryField(@NotNull Consumer<NonQueryFieldBuilder<X, T>> fieldBuilder) {
        final NonQueryFieldBuilder<X, T> builder = new NonQueryFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNullArgument(builder.name, "name required");
        final EntityResultType<T> resultType = requireNonNullArgument(builder.resultType, "result type required");
        final NonQueryFieldResolver<X, T> resolver = requireNonNullArgument(builder.resolver, "resolver required");
        final MetadataValues metadata = requireNonNull(builder.metadata, "metadata");
        return new NonQueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return resultType;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return ctx.addNonQuerySelection(this)::get;
            }

            @Override
            public NonQueryFieldResolver<X, T> getResolver() {
                return resolver;
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return metadata.get(metadataKey);
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, resultType);
            }
        };
    }

    public static <X, T> EntityJoin<X, T> attributeJoin(@NotNull SingularAttribute<? super X, T> attribute) {
        return attributeJoin(attribute, null);
    }

    public static <X, T> EntityJoin<X, T> attributeJoin(@NotNull SingularAttribute<? super X, T> attribute, Consumer<AttributeFieldBuilder<X, T>> joinBuilder) {
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(joinBuilder);
        final String name = coalesce(builder.name, attribute.getName());
        final MetadataValues metadata = requireNonNull(builder.metadata, "metadata");
        metadata.putDefault(EntityMetadataKeys.Description, QueryUtils.getDefaultDescription(attribute));
        metadata.putDefault(EntityMetadataKeys.DeprecationReason, QueryUtils.getDefaultDeprecationReason(attribute));
        return new EntityJoin<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            public Class<? super X> getEntityType() {
                return attribute.getDeclaringType().getJavaType();
            }

            @Override
            public EntityResultType<T> getResultType() {
                return parseAttributeResultType(attribute, builder.nullAllowed);
            }

            @Override
            public Supplier<Join<?, T>> join(EntityContext<X> ctx, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
                return LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT));
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return metadata.get(metadataKey);
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, getResultType());
            }
        };
    }

    public static <X, Y> EntityJoin<X, Y> join(@NotNull String name, @NotNull EntityResultType<Y> resultType, @NotNull JoinSupplier<X, Y> supplier) {
        return expressionJoin(fieldBuilder -> fieldBuilder.name(name).resultType(resultType).expression(supplier));
    }

    public static <X, Y> EntityJoin<X, Y> expressionJoin(@NotNull Consumer<ExpressionJoinBuilder<X, Y>> joinBuilder) {
        final ExpressionJoinBuilder<X, Y> builder = new ExpressionJoinBuilder<X, Y>().apply(joinBuilder);
        final String name = requireNonNullArgument(builder.name, "name required");
        final EntityResultType<Y> resultType = requireNonNullArgument(builder.resultType, "result type required");
        final JoinSupplier<X, Y> supplier = requireNonNullArgument(builder.expression, "expression required");
        final MetadataValues metadata = requireNonNull(builder.metadata, "metadata");
        return new EntityJoin<X, Y>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<Y> getResultType() {
                return resultType;
            }

            @Override
            public Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
                return supplier.join(ctx, queryBuilder);
            }

            @Override
            public <M> M getMetadata(MetadataKey<M> metadataKey) {
                return metadata.get(metadataKey);
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, resultType);
            }
        };
    }

    public static final class LiteralFieldBuilder<X, T> implements Chainable<LiteralFieldBuilder<X, T>> {

        String name;

        Class<T> resultType;

        T value;

        public LiteralFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public LiteralFieldBuilder<X, T> resultType(Class<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public LiteralFieldBuilder<X, T> value(T value) {
            this.value = value;
            return this;
        }
    }

    public static final class AttributeFieldBuilder<X, T> implements Chainable<AttributeFieldBuilder<X, T>> {

        String name;

        Boolean nullAllowed;

        final MetadataValues metadata = new MetadataValues();

        public AttributeFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public AttributeFieldBuilder<X, T> nullAllowed(Boolean nullAllowed) {
            this.nullAllowed = nullAllowed;
            return this;
        }

        public <Z> AttributeFieldBuilder<X, T> metadata(MetadataKey<Z> metadataKey, Z value) {
            metadata.put(metadataKey, value);
            return this;
        }
    }

    @FunctionalInterface
    public interface ExpressionResolver<X, T> {

        Expression<T> get(EntityContext<X> ctx, QueryBuilder queryBuilder);
    }

    @FunctionalInterface
    public interface ExpressionOrderByResolver<X> {

        Expression<?> get(EntityContext<X> ctx, QueryBuilder queryBuilder);
    }

    @FunctionalInterface
    public interface JoinSupplier<X, Y> {

        Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder queryBuilder);
    }

    public static final class ExpressionFieldBuilder<X, T> implements Chainable<ExpressionFieldBuilder<X, T>> {

        String name;

        EntityResultType<T> resultType;

        ExpressionResolver<X, T> expression;

        ExpressionOrderByResolver<X> orderBy;

        final MetadataValues metadata = new MetadataValues();

        public ExpressionFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionFieldBuilder<X, T> resultType(EntityResultType<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionFieldBuilder<X, T> expression(ExpressionResolver<X, T> expression) {
            this.expression = expression;
            return this;
        }

        public ExpressionFieldBuilder<X, T> orderBy(ExpressionOrderByResolver<X> orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public <Z> ExpressionFieldBuilder<X, T> metadata(MetadataKey<Z> key, Z value) {
            metadata.put(key, value);
            return this;
        }
    }

    public static final class NonQueryFieldBuilder<X, T> implements Chainable<NonQueryFieldBuilder<X, T>> {

        String name;

        Class<X> entityType;

        EntityResultType<T> resultType;

        boolean nullAllowed;

        NonQueryFieldResolver<X, T> resolver;

        final MetadataValues metadata = new MetadataValues();

        public NonQueryFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public NonQueryFieldBuilder<X, T> entity(Class<X> entityType) {
            this.entityType = entityType;
            return this;
        }

        public NonQueryFieldBuilder<X, T> resultType(EntityResultType<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public NonQueryFieldBuilder<X, T> nullAllowed(boolean nullAllowed) {
            this.nullAllowed = nullAllowed;
            return this;
        }

        public NonQueryFieldBuilder<X, T> resolver(NonQueryFieldResolver<X, T> resolver) {
            this.resolver = resolver;
            return this;
        }

        public <Z> NonQueryFieldBuilder<X, T> metadata(MetadataKey<Z> key, Z value) {
            metadata.put(key, value);
            return this;
        }
    }

    public static final class ExpressionJoinBuilder<X, Y> implements Chainable<ExpressionJoinBuilder<X, Y>> {

        String name;

        EntityResultType<Y> resultType;

        JoinSupplier<X, Y> expression;

        final MetadataValues metadata = new MetadataValues();

        public ExpressionJoinBuilder<X, Y> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> resultType(EntityResultType<Y> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> expression(JoinSupplier<X, Y> expression) {
            this.expression = expression;
            return this;
        }

        public <Z> ExpressionJoinBuilder<X, Y> metadata(MetadataKey<Z> key, Z value) {
            metadata.put(key, value);
            return this;
        }
    }
}
