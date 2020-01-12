package org.grapple.query;

import javax.persistence.criteria.Expression;

public interface QueryField<X, T> extends EntityField<X, T> {

    Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder builder);

    Expression<?> getOrderBy(EntityContext<X> ctx, QueryBuilder builder);

}
