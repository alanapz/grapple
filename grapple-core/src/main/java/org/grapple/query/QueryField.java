package org.grapple.query;

import javax.persistence.criteria.Expression;

import org.jetbrains.annotations.NotNull;

public interface QueryField<X, T> extends EntityField<X, T> {

    Expression<T> getExpression(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder);

    Expression<?> getOrderBy(@NotNull EntityContext<X> ctx, @NotNull QueryBuilder queryBuilder);

}
