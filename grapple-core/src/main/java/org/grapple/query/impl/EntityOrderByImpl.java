package org.grapple.query.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityOrderBy;
import org.grapple.query.EntitySortKey;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryField;
import org.grapple.query.SortDirection;

final class EntityOrderByImpl<X> implements EntityOrderBy<X> {

    private final FetchSet<X> fetchSet;

    private final SortDirection direction;

    private final ExpressionResolver<X> resolver;

    private EntityOrderByImpl(FetchSet<X> fetchSet, SortDirection direction, ExpressionResolver<X> resolver) {
        this.fetchSet = requireNonNull(fetchSet, "fetchSet");
        this.direction = requireNonNull(direction, "direction");
        this.resolver = requireNonNull(resolver, "resolver");
    }

    @Override
    public FetchSet<X> getFetchSet() {
        return fetchSet;
    }

    Order build(ExecutionContext executionContext, QueryBuilder queryBuilder) {
        final EntityContext<X> entityContext = executionContext.getEntityContext(fetchSet);
        final Expression<?> expression = resolveExpression(entityContext, queryBuilder);
        return queryBuilder.orderBy(expression, direction);
    }

    @SuppressWarnings("unchecked")
    private Expression<?> resolveExpression(EntityContext<X> entityContext, QueryBuilder queryBuilder) {
        final Expression<?> expression = resolver.get(entityContext, queryBuilder);
        // Special case ORDER BY bool|boolean to map true = 0, false = 1
        if (expression.getJavaType() == boolean.class || expression.getJavaType() == Boolean.class) {
            return queryBuilder.<Integer> selectCase().when(queryBuilder.isTrue((Expression<Boolean>) expression), 0).otherwise(1);
        }
        return expression;
    }

    @Override
    public String toString() {
        return format("%s %s", QueryImplUtils.resolveFullName(fetchSet, resolver.toString()), direction);
    }

    @FunctionalInterface
    private interface ExpressionResolver<X> {

        Expression<?> get(EntityContext<X> ctx, QueryBuilder queryBuilder);

    }

    static <X> EntityOrderByImpl<X> entityOrderBy(FetchSet<X> fetchSet, SortDirection direction, QueryField<X, ?> field) {
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(direction, "direction");
        requireNonNull(field, "field");
        return new EntityOrderByImpl<>(fetchSet, direction, field::getOrderBy);
    }

    static <X> EntityOrderByImpl<X> entityOrderBy(FetchSet<X> fetchSet, SortDirection direction, EntitySortKey<X> sortKey) {
        requireNonNull(fetchSet, "fetchSet");
        requireNonNull(direction, "direction");
        requireNonNull(sortKey, "sortKey");
        return new EntityOrderByImpl<>(fetchSet, direction, (ctx, queryBuilder) -> sortKey.getPath(ctx));
    }
}
