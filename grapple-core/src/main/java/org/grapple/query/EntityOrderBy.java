package org.grapple.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import org.grapple.utils.EntitySortKey;

import static java.util.Objects.requireNonNull;

public final class EntityOrderBy<X> {

    private final FetchSet<X> fetchSet;

    private final EntitySortKey<X> sortKey;

    private final SortDirection direction;

    private EntityOrderBy(FetchSet<X> fetchSet, EntitySortKey<X> sortKey, SortDirection direction) {
        this.fetchSet = requireNonNull(fetchSet, "fetchSet");
        this.sortKey = requireNonNull(sortKey, "sortKey");
        this.direction = requireNonNull(direction, "direction");
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
        final Expression<?> expression = sortKey.getPath(entityContext);
        // Special case ORDER BY bool|boolean to map true = 0, false = 1
        if (expression.getJavaType() == boolean.class || expression.getJavaType() == Boolean.class) {
            return builder.<Integer> selectCase().when(builder.isTrue((Expression<Boolean>) expression), 0).otherwise(1);
        }
        return expression;
    }

    static <X> EntityOrderBy<X> of(FetchSet<X> fetchSet, EntitySortKey<X> sortKey, SortDirection direction) {
        return new EntityOrderBy<>(fetchSet, sortKey, direction);
    }

    static <X> EntityOrderBy<X> of(FetchSet<X> fetchSet, QueryField<X, ?> field, SortDirection direction) {
        return new EntityOrderBy<>(fetchSet, ctx -> ctx.get(field), direction);
    }
}
