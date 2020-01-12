package org.grapple.query;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

public interface QueryBuilder extends CriteriaBuilder {

    <T> T apply(Function<QueryBuilder, T> function);

    Expression<Integer> zero();

    Expression<Boolean> trueLiteral();

    Expression<Boolean> falseLiteral();

    Expression<Boolean> toExpression(Predicate predicate);

    Predicate isBitmaskSet(Expression<Integer> bitmask, int value);

    Predicate isBitmaskSet(Expression<Integer> bitmask, Enum<?> value);

    Predicate isBitmaskSet(Expression<Integer> bitmask, Expression<Integer> value);

    Order orderBy(Expression<?> x, SortDirection direction);

    <N extends Number> Expression<N> sum(List<Expression<N>> values, N zero);

    @SuppressWarnings("unchecked")
    Expression<Integer> greatest(Expression<Integer>... values);

    <X extends Comparable<? super X>> Expression<X> greatest(Class<X> entityType, Expression<X> v1, Expression<X> v2);

    Predicate and(Collection<Predicate> restrictions);

    Predicate or(Collection<Predicate> restrictions);

    Predicate alwaysTrue();

    Predicate alwaysFalse();

    Predicate likeNonAnchored(Expression<String> x, String value);

    <T> Predicate in(Expression<? extends T> expression, Set<T> values);

}
