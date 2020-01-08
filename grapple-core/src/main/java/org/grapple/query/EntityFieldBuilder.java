package org.grapple.query;

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

import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;

public final class EntityFieldBuilder {

    private EntityFieldBuilder() {

    }

    public static <X, T> QueryField<X, T> literal(String name, Class<T> resultType, T value) {
        requireNonNull(name, "name");
        requireNonNull(resultType, "resultType");
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return EntityResultType.of(resultType, (value == null));
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                return null;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder builder) {
                return tuple -> value;
            }

            @Override
            public Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder builder) {
                return (value == null ? builder.nullLiteral(resultType) : builder.literal(value));
            }
        };
    }

    public static <X, T> QueryField<X, T> from(SingularAttribute<X, T> attribute) {
        requireNonNull(attribute, "attribute");
        return attribute(fieldBuilder -> fieldBuilder.attribute(attribute));
    }

    public static <X, T> QueryField<X, T> from(SingularAttribute<X, T> attribute, boolean nullable) {
        requireNonNull(attribute, "attribute");
        return attribute(fieldBuilder -> fieldBuilder.attribute(attribute).nullable(nullable));
    }

    public static <X, T> QueryField<X, T> from(String name, SingularAttribute<X, T> attribute) {
        requireNonNull(name, "name");
        requireNonNull(attribute, "attribute");
        return attribute(fieldBuilder -> fieldBuilder.name(name).attribute(attribute));
    }

    public static <X, T> QueryField<X, T> from(String name, SingularAttribute<X, T> attribute, boolean nullable) {
        requireNonNull(name, "name");
        requireNonNull(attribute, "attribute");
        return attribute(fieldBuilder -> fieldBuilder.name(name).attribute(attribute).nullable(nullable));
    }

    public static <X, T> QueryField<X, T> attribute(Consumer<AttributeFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(fieldBuilder);
        final SingularAttribute<X, T> attribute = requireNonNull(builder.attribute, "attribute required");
        final String name = coalesce(builder.name, attribute.getName());
        final boolean nullable = coalesce(builder.nullable, attribute.isOptional());
        final String description = coalesce(builder.description, QueryUtils.getDefaultDescription(attribute));
        final boolean deprecated = coalesce(builder.deprecated, QueryUtils.isDefaultDeprecated(attribute));
        return new QueryField<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return EntityResultType.of(attribute.getJavaType(), nullable);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.DESCRIPTION) {
                    return description;
                }
                if (metadataKey == EntityMetadataKeys.DEPRECATED) {
                    return deprecated;
                }
                return null;
            }

            @Override
            public Path<T> getExpression(EntityContext<X> ctx, QueryBuilder builder) {
                return ctx.get(attribute);
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder builder) {
                final Path<T> attributePath = getExpression(ctx, builder);
                ctx.addSelection(attributePath);
                return tuple -> tuple.get(attributePath);
            }
        };
    }

    public static <X, T> QueryField<X, T> from(String name, EntityResultType<T> resultType, ExpressionResultSupplier<X, T> supplier) {
        requireNonNull(name, "name");
        requireNonNull(resultType, "resultType");
        requireNonNull(supplier, "supplier");
        return expression(fieldBuilder -> fieldBuilder.name(name).resultType(resultType).supplier(supplier));
    }

    public static <X, T> QueryField<X, T> expression(Consumer<ExpressionFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final ExpressionFieldBuilder<X, T> builder = new ExpressionFieldBuilder<X, T>().apply(fieldBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final EntityResultType<T> resultType = requireNonNull(builder.resultType, "result type required");
        final ExpressionResultSupplier<X, T> supplier  = requireNonNull(builder.supplier, "supplier required");
        final String description = builder.description;
        final boolean deprecated = coalesce(builder.deprecated, false);
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
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.DESCRIPTION) {
                    return description;
                }
                if (metadataKey == EntityMetadataKeys.DEPRECATED) {
                    return deprecated;
                }
                return null;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder builder) {
                final Expression<T> expression = getExpression(ctx, builder);
                ctx.addSelection(expression);
                return tuple -> tuple.get(expression);
            }

            @Override
            public Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder builder) {
                return supplier.get(ctx, builder);
            }
        };
    }


    public static <X, T> EntityField<X, T> selection(String name, EntityResultType<T> resultType, SelectionResultSupplier<X, T> supplier) {
        requireNonNull(name, "name");
        requireNonNull(resultType, "resultType");
        requireNonNull(supplier, "supplier");
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
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                return null;
            }

            @Override
            public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder builder) {
                return supplier.get(ctx, builder);
            }
        };
    }

    public static <X, T> EntityJoin<X, T> join(SingularAttribute<X, T> attribute) {
        requireNonNull(attribute, "attribute");
        return attributeJoin(fieldBuilder -> fieldBuilder.attribute(attribute));
    }

    public static <X, T> EntityJoin<X, T> join(SingularAttribute<X, T> attribute, boolean nullable) {
        requireNonNull(attribute, "attribute");
        return attributeJoin(fieldBuilder -> fieldBuilder.attribute(attribute).nullable(nullable));
    }

    public static <X, T> EntityJoin<X, T> join(String name, SingularAttribute<X, T> attribute) {
        requireNonNull(name, "name");
        requireNonNull(attribute, "attribute");
        return attributeJoin(fieldBuilder -> fieldBuilder.name(name).attribute(attribute));
    }

    public static <X, T> EntityJoin<X, T> join(String name, SingularAttribute<X, T> attribute, boolean nullable) {
        requireNonNull(name, "name");
        requireNonNull(attribute, "attribute");
        return attributeJoin(fieldBuilder -> fieldBuilder.name(name).attribute(attribute).nullable(nullable));
    }

    public static <X, T> EntityJoin<X, T> attributeJoin(Consumer<AttributeFieldBuilder<X, T>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final AttributeFieldBuilder<X, T> builder = new AttributeFieldBuilder<X, T>().apply(fieldBuilder);
        final SingularAttribute<X, T> attribute = requireNonNull(builder.attribute, "attribute required");
        final String name = coalesce(builder.name, attribute.getName());
        final boolean nullable = coalesce(builder.nullable, attribute.isOptional());
        final String description = coalesce(builder.description, QueryUtils.getDefaultDescription(attribute));
        final boolean deprecated = coalesce(builder.deprecated, QueryUtils.isDefaultDeprecated(attribute));
        return new EntityJoin<X, T>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public EntityResultType<T> getResultType() {
                return EntityResultType.of(attribute.getJavaType(), nullable);
            }

            @Override
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                if (metadataKey == EntityMetadataKeys.DESCRIPTION) {
                    return description;
                }
                if (metadataKey == EntityMetadataKeys.DEPRECATED) {
                    return deprecated;
                }
                return null;
            }

            @Override
            public Supplier<Join<?, T>> join(EntityContext<X> ctx, QueryBuilder builder, Supplier<? extends From<?, X>> entity) {
                return LazyValue.of(() -> entity.get().join(attribute, JoinType.LEFT));
            }
        };
    }

    public static <X, Y> EntityJoin<X, Y> join(String name, EntityResultType<Y> resultType, JoinSupplier<X, Y> supplier) {
        requireNonNull(name, "name");
        requireNonNull(resultType, "resultType");
        requireNonNull(supplier, "supplier");
        return expressionJoin(fieldBuilder -> fieldBuilder.name(name).resultType(resultType).supplier(supplier));
    }

    public static <X, Y> EntityJoin<X, Y> expressionJoin(Consumer<ExpressionJoinBuilder<X, Y>> fieldBuilder) {
        requireNonNull(fieldBuilder, "fieldBuilder");
        final ExpressionJoinBuilder<X, Y> builder = new ExpressionJoinBuilder<X, Y>().apply(fieldBuilder);
        final String name = requireNonNull(builder.name, "name required");
        final EntityResultType<Y> resultType = requireNonNull(builder.resultType, "result type required");
        final JoinSupplier<X, Y> supplier  = requireNonNull(builder.supplier, "supplier required");
        final String description = builder.description;
        final boolean deprecated = coalesce(builder.deprecated, false);
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
            public Object getMetadataValue(MetadataKey<?> metadataKey) {
                return null;
            }

            @Override
            public Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder builder, Supplier<? extends From<?, X>> entity) {
                return supplier.join(ctx, builder);
            }
        };
    }

    @FunctionalInterface
    public interface ExpressionResultSupplier<X, T> {

        Expression<T> get(EntityContext<X> ctx, QueryBuilder builder);
    }

    @FunctionalInterface
    public interface SelectionResultSupplier<X, T> {

        Function<Tuple, T> get(EntityContext<X> ctx, QueryBuilder builder);
    }

    @FunctionalInterface
    public interface JoinSupplier<X, Y> {

        Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder builder);
    }

    public static final class AttributeFieldBuilder<X, T> implements Chainable<AttributeFieldBuilder<X, T>> {

        SingularAttribute<X, T> attribute;

        String name;

        Boolean nullable;

        String description;

        Boolean deprecated;

        public AttributeFieldBuilder<X, T> attribute(SingularAttribute<X, T> attribute) {
            this.attribute = attribute;
            return this;
        }

        public AttributeFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public AttributeFieldBuilder<X, T> nullable(Boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public AttributeFieldBuilder<X, T> description(String description) {
            this.description = description;
            return this;
        }

        public AttributeFieldBuilder<X, T> deprecated() {
            this.deprecated = true;
            return this;
        }

        public AttributeFieldBuilder<X, T> deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        @Override
        public AttributeFieldBuilder<X, T> apply(Consumer<AttributeFieldBuilder<X, T>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }
    }

    public static final class ExpressionFieldBuilder<X, T> implements Chainable<ExpressionFieldBuilder<X, T>> {

        String name;

        EntityResultType<T> resultType;

        ExpressionResultSupplier<X, T> supplier;

        String description;

        Boolean deprecated;

        public ExpressionFieldBuilder<X, T> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionFieldBuilder<X, T> resultType(EntityResultType<T> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionFieldBuilder<X, T> supplier(ExpressionResultSupplier<X, T> supplier) {
            this.supplier = supplier;
            return this;
        }

        public ExpressionFieldBuilder<X, T> description(String description) {
            this.description = description;
            return this;
        }

        public ExpressionFieldBuilder<X, T> deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        @Override
        public ExpressionFieldBuilder<X, T> apply(Consumer<ExpressionFieldBuilder<X, T>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }
    }

    public static final class ExpressionJoinBuilder<X, Y> implements Chainable<ExpressionJoinBuilder<X, Y>> {

        String name;

        EntityResultType<Y> resultType;

        JoinSupplier<X, Y> supplier;

        String description;

        Boolean deprecated;

        public ExpressionJoinBuilder<X, Y> name(String name) {
            this.name = name;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> resultType(EntityResultType<Y> resultType) {
            this.resultType = resultType;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> supplier(JoinSupplier<X, Y> supplier) {
            this.supplier = supplier;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> description(String description) {
            this.description = description;
            return this;
        }

        public ExpressionJoinBuilder<X, Y> deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        @Override
        public ExpressionJoinBuilder<X, Y> apply(Consumer<ExpressionJoinBuilder<X, Y>> consumer) {
            if (consumer != null) {
                consumer.accept(this);
            }
            return this;
        }
    }
}
