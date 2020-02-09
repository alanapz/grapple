package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.EntityResultType.entityResultType;
import static org.grapple.utils.Utils.coalesce;

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
import org.grapple.core.MetadataKey;
import org.grapple.utils.Chainable;
import org.grapple.utils.LazyValue;
import org.grapple.utils.Utils;

public final class EntityFieldBuilder {

    private EntityFieldBuilder() {

    }

    public static <X, T> QueryField<X, T> literalField(Consumer<LiteralFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final LiteralFieldBuilder<X, T> builder = new LiteralFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final Class<T> resultType = requireNonNull(builder.resultType, "result type required");
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
            public Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return (value == null ? queryBuilder.nullLiteral(resultType) : queryBuilder.literal(value));
            }

            @Override
            public Expression<T> getOrderBy(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return getExpression(ctx, queryBuilder);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                return null;
            }
        };
    }

    public static <X, T> QueryField<X, T> attributeField(SingularAttribute<X, T> attribute) {
        return attributeField(attribute, null);
    }

    public static <X, T> QueryField<X, T> attributeField(SingularAttribute<X, T> attribute, Consumer<AttributeFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(attribute, "attribute");
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = coalesce(builder.name, attribute.getName());
        final EntityResultType<T> resultType = entityResultType(attribute.getJavaType(), coalesce(builder.nullAllowed, attribute.isOptional()));
        final String description = coalesce(builder.description, QueryUtils.getDefaultDescription(attribute));
        final String deprecationReason = coalesce(builder.deprecationReason, QueryUtils.getDefaultDeprecationReason(attribute));
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            public Class<X> getEntityType() {
                return attribute.getDeclaringType().getJavaType();
            }

            @Override
            public EntityResultType<T> getResultType() {
                return resultType;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                final Path<T> attributePath = ctx.addSelection(getExpression(ctx, queryBuilder));
                return tuple -> tuple.get(attributePath);
            }

            @Override
            public Path<T> getExpression(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return ctx.get(attribute);
            }

            @Override
            public Path<T> getOrderBy(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return getExpression(ctx, queryBuilder);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.Visibility) {
                    return builder.visibility;
                }
                if (metadataKey == EntityMetadataKeys.Description) {
                    return description;
                }
                if (metadataKey == EntityMetadataKeys.DeprecationReason) {
                    return deprecationReason;
                }
                return null;
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, getResultType());
            }
        };
    }

    public static <X, T> QueryField<X, T> expressionField(Consumer<ExpressionFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final ExpressionFieldBuilder<X, T> builder = new ExpressionFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final EntityResultType<T> resultType = requireNonNull(builder.resultType, "result type required");
        final ExpressionResolver<X, T> expression = requireNonNull(builder.expression, "expression required");
        final ExpressionOrderByResolver<X> orderBy = coalesce(builder.orderBy, expression::get);
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
                final Expression<T> expression = getExpression(ctx, queryBuilder);
                ctx.addSelection(expression);
                return tuple -> tuple.get(expression);
            }

            @Override
            public Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return expression.get(ctx, queryBuilder);
            }

            @Override
            public Expression<?> getOrderBy(EntityContext<X> ctx, QueryBuilder queryBuilder) {
                return orderBy.get(ctx, queryBuilder);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.Visibility) {
                    return builder.visibility;
                }
                if (metadataKey == EntityMetadataKeys.Description) {
                    return builder.description;
                }
                if (metadataKey == EntityMetadataKeys.DeprecationReason) {
                    return builder.deprecationReason;
                }
                return null;
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, resultType);
            }
        };
    }

    public static <X, T> EntityField<X, T> selectionField(String name, EntityResultType<T> resultType, SelectionResultSupplier<X, T> supplier) {
        return selectionField(fieldBuilder -> fieldBuilder
                .name(name)
                .resultType(resultType)
                .resolver(supplier));
    }

    public static <X, T> EntityField<X, T> selectionField(Consumer<SelectionFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final SelectionFieldBuilder<X, T> builder = new SelectionFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final EntityResultType<T> resultType = requireNonNull(builder.resultType, "result type required");
        final SelectionResultSupplier<X, T> resolver = requireNonNull(builder.resolver, "resolver required");
        return new EntityField<X, T>() {

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
                return resolver.get(ctx, queryBuilder);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.Visibility) {
                    return builder.visibility;
                }
                if (metadataKey == EntityMetadataKeys.Description) {
                    return builder.description;
                }
                if (metadataKey == EntityMetadataKeys.DeprecationReason) {
                    return builder.deprecationReason;
                }
                return null;
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, resultType);
            }
        };
    }

    public static <X, T> EntityJoin<X, T> attributeJoin(SingularAttribute<X, T> attribute) {
        return attributeJoin(attribute, null);
    }

    public static <X, T> EntityJoin<X, T> attributeJoin(SingularAttribute<X, T> attribute, Consumer<AttributeFieldBuilder<X, T>> joinBuilder) {
        requireNonNull(attribute, "attribute");
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(joinBuilder);
        final String name = coalesce(builder.name, attribute.getName());
        final boolean nullAllowed = coalesce(builder.nullAllowed, attribute.isOptional());
        final String description = coalesce(builder.description, QueryUtils.getDefaultDescription(attribute));
        final String deprecationReason = coalesce(builder.deprecationReason, QueryUtils.getDefaultDeprecationReason(attribute));
        return new EntityJoin<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            public Class<X> getEntityType() {
                return attribute.getDeclaringType().getJavaType();
            }

            @Override
            public EntityResultType<T> getResultType() {
                return entityResultType(attribute.getJavaType(), nullAllowed);
            }

            @Override
            public Supplier<Join<?, T>> join(EntityContext<X> ctx, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
                return LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT));
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.Visibility) {
                    return builder.visibility;
                }
                if (metadataKey == EntityMetadataKeys.Description) {
                    return description;
                }
                if (metadataKey == EntityMetadataKeys.DeprecationReason) {
                    return deprecationReason;
                }
                return null;
            }

            @Override
            public String toString() {
                return format("%s[%s]", name, getResultType());
            }
        };
    }

    public static <X, Y> EntityJoin<X, Y> join(String name, EntityResultType<Y> resultType, JoinSupplier<X, Y> supplier) {
        requireNonNull(name, "name");
        requireNonNull(resultType, "resultType");
        requireNonNull(supplier, "supplier");
        return expressionJoin(fieldBuilder -> fieldBuilder.name(name).resultType(resultType).expression(supplier));
    }

    public static <X, Y> EntityJoin<X, Y> expressionJoin(Consumer<ExpressionJoinBuilder<X, Y>> joinBuilder) {
        requireNonNull(joinBuilder, "joinBuilder");
        final ExpressionJoinBuilder<X, Y> builder = new ExpressionJoinBuilder<X, Y>().apply(joinBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final EntityResultType<Y> resultType = requireNonNull(builder.resultType, "result type required");
        final JoinSupplier<X, Y> supplier  = requireNonNull(builder.expression, "expression required");
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
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.Visibility) {
                    return builder.visibility;
                }
                if (metadataKey == EntityMetadataKeys.Description) {
                    return builder.description;
                }
                if (metadataKey == EntityMetadataKeys.DeprecationReason) {
                    return builder.deprecationReason;
                }
                return null;
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

        @Override
        public LiteralFieldBuilder<X, T> apply(Consumer<LiteralFieldBuilder<X, T>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }

        @Override
        public <Z> Z invoke(Function<LiteralFieldBuilder<X, T>, Z> function) {
            return requireNonNull(function, "function").apply(this);
        }
    }

    public static final class AttributeFieldBuilder<X, T> implements Chainable<AttributeFieldBuilder<X, T>> {

        String name;

        Boolean nullAllowed;

        EntitySchemaVisibility visibility;

        String description;

        String deprecationReason;

        public AttributeFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public AttributeFieldBuilder<X, T> nullAllowed(Boolean nullAllowed) {
            this.nullAllowed = nullAllowed;
            return this;
        }

        public AttributeFieldBuilder<X, T> visibility(EntitySchemaVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public AttributeFieldBuilder<X, T> description(String description) {
            this.description = description;
            return this;
        }

        public AttributeFieldBuilder<X, T> deprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        @Override
        public AttributeFieldBuilder<X, T> apply(Consumer<AttributeFieldBuilder<X, T>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }

        @Override
        public <Z> Z invoke(Function<AttributeFieldBuilder<X, T>, Z> function) {
            return requireNonNull(function, "function").apply(this);
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
    public interface SelectionResultSupplier<X, T> {

        Function<Tuple, T> get(EntityContext<X> ctx, QueryBuilder queryBuilder);
    }

    @FunctionalInterface
    public interface JoinSupplier<X, Y> {

        Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder queryBuilder);
    }

    public static final class ExpressionFieldBuilder<X, T> implements Chainable<ExpressionFieldBuilder<X, T>> {

        String name;

        EntityResultType<T> resultType;

        EntitySchemaVisibility visibility;

        ExpressionResolver<X, T> expression;

        ExpressionOrderByResolver<X> orderBy;

        String description;

        String deprecationReason;

        public ExpressionFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionFieldBuilder<X, T> resultType(EntityResultType<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionFieldBuilder<X, T> visibility(EntitySchemaVisibility visibility) {
            this.visibility = visibility;
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

        public ExpressionFieldBuilder<X, T> description(String description) {
            this.description = description;
            return this;
        }

        public ExpressionFieldBuilder<X, T> deprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        @Override
        public ExpressionFieldBuilder<X, T> apply(Consumer<ExpressionFieldBuilder<X, T>> consumer) {
            return Utils.apply(this, consumer);
        }

        @Override
        public <Z> Z invoke(Function<ExpressionFieldBuilder<X, T>, Z> function) {
            return requireNonNull(function, "function").apply(this);
        }
    }

    public static final class SelectionFieldBuilder<X, T> implements Chainable<SelectionFieldBuilder<X, T>> {

        String name;

        Class<X> entityType;

        EntityResultType<T> resultType;

        boolean nullAllowed;

        EntitySchemaVisibility visibility;

        SelectionResultSupplier<X, T> resolver;

        String description;

        String deprecationReason;

        public SelectionFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public SelectionFieldBuilder<X, T> entity(Class<X> entityType) {
            this.entityType = entityType;
            return this;
        }

        public SelectionFieldBuilder<X, T> resultType(EntityResultType<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public SelectionFieldBuilder<X, T> nullAllowed(boolean nullAllowed) {
            this.nullAllowed = nullAllowed;
            return this;
        }

        public SelectionFieldBuilder<X, T> visibility(EntitySchemaVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public SelectionFieldBuilder<X, T> resolver(SelectionResultSupplier<X, T> resolver) {
            this.resolver = resolver;
            return this;
        }

        public SelectionFieldBuilder<X, T> description(String description) {
            this.description = description;
            return this;
        }

        public SelectionFieldBuilder<X, T> deprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        @Override
        public SelectionFieldBuilder<X, T> apply(Consumer<SelectionFieldBuilder<X, T>> consumer) {
            return Utils.apply(this, consumer);
        }

        @Override
        public <Z> Z invoke(Function<SelectionFieldBuilder<X, T>, Z> function) {
            return requireNonNull(function, "function").apply(this);
        }
    }

    public static final class ExpressionJoinBuilder<X, Y> implements Chainable<ExpressionJoinBuilder<X, Y>> {

        String name;

        EntityResultType<Y> resultType;

        EntitySchemaVisibility visibility;

        JoinSupplier<X, Y> expression;

        String description;

        String deprecationReason;

        public ExpressionJoinBuilder<X, Y> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> resultType(EntityResultType<Y> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> visibility(EntitySchemaVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> expression(JoinSupplier<X, Y> expression) {
            this.expression = expression;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> description(String description) {
            this.description = description;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> deprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        @Override
        public ExpressionJoinBuilder<X, Y> apply(Consumer<ExpressionJoinBuilder<X, Y>> consumer) {
            return Utils.apply(this, consumer);
        }

        @Override
        public <Z> Z invoke(Function<ExpressionJoinBuilder<X, Y>, Z> function) {
            return requireNonNull(function, "function").apply(this);
        }
    }
}
