package org.grapple.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

import static java.util.Objects.requireNonNull;

public final class EntityOrderBy<X> {

    private final FetchSet<X> fetchSet;

    private final SortDirection direction;

    private final ExpressionResolver<X> resolver;

    EntityOrderBy(FetchSet<X> fetchSet, SortDirection direction, ExpressionResolver<X> resolver) {
        this.fetchSet = requireNonNull(fetchSet, "fetchSet");
        this.direction = requireNonNull(direction, "direction");
        this.resolver = requireNonNull(resolver, "resolver");
    }

    FetchSet<?> getFetchSet() {
        return fetchSet;
    }

    Order build(ExecutionContext executionContext, QueryBuilder builder) {
        final EntityContext<X> entityContext = executionContext.getEntityContext(fetchSet);
        final Expression<?> expression = resolveExpression(entityContext, builder);
        return builder.orderBy(expression, direction);
    }

    @SuppressWarnings("unchecked")
    private Expression<?> resolveExpression(EntityContext<X> entityContext, QueryBuilder builder) {
        final Expression<?> expression = resolver.get(entityContext, builder);
        // Special case ORDER BY bool|boolean to map true = 0, false = 1
        if (expression.getJavaType() == boolean.class || expression.getJavaType() == Boolean.class) {
            return builder.<Integer> selectCase().when(builder.isTrue((Expression<Boolean>) expression), 0).otherwise(1);
        }
        return expression;
    }

    @FunctionalInterface
    interface ExpressionResolver<X> {

        Expression<?> get(EntityContext<X> ctx, QueryBuilder builder);

    }
}
