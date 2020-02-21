package org.grapple.query.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import org.grapple.query.EntityContext;
import org.grapple.query.QueryBuilder;
import org.grapple.query.SortDirection;

final class QueryBuilderImpl implements QueryBuilder {

    private final CriteriaBuilder criteriaBuilder;

    QueryBuilderImpl(CriteriaBuilder criteriaBuilder) {
        this.criteriaBuilder = requireNonNull(criteriaBuilder, "criteriaBuilder");
    }

    @Override
    public <T> T apply(Function<QueryBuilder, T> function) {
        return function.apply(this);
    }

    @Override
    public Expression<Integer> zero() {
        return criteriaBuilder.literal(0);
    }

    @Override
    public Expression<Boolean> trueLiteral() {
        return literal(true);
    }

    @Override
    public Expression<Boolean> falseLiteral() {
        return literal(false);
    }

    @Override
    public Expression<Boolean> toExpression(Predicate predicate) {
        requireNonNull(predicate, "predicate");
        return this.<Boolean>selectCase().when(predicate, trueLiteral()).otherwise(falseLiteral());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Expression<T> wrapPredicateIfNecessary(Expression<T> expression) {
        requireNonNull(expression, "expression");
        if (expression instanceof Predicate) {
            return (Expression<T>) toExpression((Predicate) expression);
        }
        return expression;
    }

    @Override
    public Predicate isBitmaskSet(Expression<Integer> bitmask, int value) {
        return isBitmaskSet(bitmask, literal(value));
    }

    @Override
    public Predicate isBitmaskSet(Expression<Integer> bitmask, Enum<?> value) {
        return isBitmaskSet(bitmask, literal(1 << value.ordinal()));
    }

    @Override
    public Predicate isBitmaskSet(Expression<Integer> bitmask, Expression<Integer> value) {
        return criteriaBuilder.notEqual(criteriaBuilder.function("is_bit_set", Integer.class, bitmask, value), zero());
    }

    @Override
    public CriteriaQuery<Object> createQuery() {
        return criteriaBuilder.createQuery();
    }

    @Override
    public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
        return criteriaBuilder.createQuery(resultClass);
    }

    @Override
    public CriteriaQuery<Tuple> createTupleQuery() {
        return criteriaBuilder.createTupleQuery();
    }

    @Override
    public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
        return criteriaBuilder.createCriteriaUpdate(targetEntity);
    }

    @Override
    public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
        return criteriaBuilder.createCriteriaDelete(targetEntity);
    }

    @Override
    public <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections) {
        return criteriaBuilder.construct(resultClass, selections);
    }

    @Override
    public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        return criteriaBuilder.tuple(selections);
    }

    @Override
    public CompoundSelection<Object[]> array(Selection<?>... selections) {
        return criteriaBuilder.array(selections);
    }

    @Override
    public Order asc(Expression<?> x) {
        return criteriaBuilder.asc(x);
    }

    @Override
    public Order desc(Expression<?> x) {
        return criteriaBuilder.desc(x);
    }

    @Override
    public Order orderBy(Expression<?> x, SortDirection direction) {
        return (direction == SortDirection.ASC ? criteriaBuilder.asc(x) : criteriaBuilder.desc(x));
    }

    @Override
    public <N extends Number> Expression<Double> avg(Expression<N> x) {
        return criteriaBuilder.avg(x);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<N> x) {
        return criteriaBuilder.sum(x);
    }

    @Override
    public <N extends Number> Expression<N> sum(List<Expression<N>> values, N zero) {
        if (values.isEmpty()) {
            return literal(zero);
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        if (values.size() == 2) {
            return sum(values.get(0), values.get(1));
        }
        /* mutable */ Expression<N> current = literal(zero);
        for (Expression<N> value : values) {
            current = sum(current, value);
        }
        return current;
    }

    @Override
    public Expression<Long> sumAsLong(Expression<Integer> x) {
        return criteriaBuilder.sumAsLong(x);
    }

    @Override
    public Expression<Double> sumAsDouble(Expression<Float> x) {
        return criteriaBuilder.sumAsDouble(x);
    }

    @Override
    public <N extends Number> Expression<N> max(Expression<N> x) {
        return criteriaBuilder.max(x);
    }

    @Override
    public <N extends Number> Expression<N> min(Expression<N> x) {
        return criteriaBuilder.min(x);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
        return criteriaBuilder.greatest(x);
    }

    @SafeVarargs
    @Override
    public final Expression<Integer> greatest(Expression<Integer>... values) {
        return function("greatest", Integer.class, values);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Class<X> entityType, Expression<X> v1, Expression<X> v2) {
        return function("greatest", entityType, v1, v2);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
        return criteriaBuilder.least(x);
    }

    @Override
    public Expression<Long> count(Expression<?> x) {
        return criteriaBuilder.count(x);
    }

    @Override
    public Expression<Long> countDistinct(Expression<?> x) {
        return criteriaBuilder.countDistinct(x);
    }

    @Override
    public Predicate exists(Subquery<?> subquery) {
        return criteriaBuilder.exists(subquery);
    }

    @Override
    public <Y> Expression<Y> all(Subquery<Y> subquery) {
        return criteriaBuilder.all(subquery);
    }

    @Override
    public <Y> Expression<Y> some(Subquery<Y> subquery) {
        return criteriaBuilder.some(subquery);
    }

    @Override
    public <Y> Expression<Y> any(Subquery<Y> subquery) {
        return criteriaBuilder.any(subquery);
    }

    @Override
    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return criteriaBuilder.and(x, y);
    }

    @Override
    public Predicate and(Predicate... restrictions) {
        return criteriaBuilder.and(restrictions);
    }

    @Override
    public Predicate and(Collection<Predicate> restrictions) {
        if (restrictions == null || restrictions.isEmpty()) {
            return alwaysTrue();
        }
        return and(restrictions.toArray(new Predicate[0]));
    }

    @Override
    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return criteriaBuilder.or(x, y);
    }

    @Override
    public Predicate or(Predicate... restrictions) {
        return criteriaBuilder.or(restrictions);
    }

    @Override
    public Predicate or(Collection<Predicate> restrictions) {
        if (restrictions == null || restrictions.isEmpty()) {
            return alwaysTrue();
        }
        return or(restrictions.toArray(new Predicate[0]));
    }

    @Override
    public Predicate not(Expression<Boolean> restriction) {
        return criteriaBuilder.not(restriction);
    }

    @Override
    public Predicate conjunction() {
        return criteriaBuilder.conjunction();
    }

    @Override
    public Predicate alwaysTrue() {
        return conjunction();
    }

    @Override
    public Predicate disjunction() {
        return criteriaBuilder.disjunction();
    }

    @Override
    public Predicate alwaysFalse() {
        return disjunction();
    }

    @Override
    public Predicate isTrue(Expression<Boolean> x) {
        return criteriaBuilder.isTrue(x);
    }

    @Override
    public Predicate isFalse(Expression<Boolean> x) {
        return criteriaBuilder.isFalse(x);
    }

    @Override
    public Predicate isNull(Expression<?> x) {
        return criteriaBuilder.isNull(x);
    }

    @Override
    public Predicate isNotNull(Expression<?> x) {
        return criteriaBuilder.isNotNull(x);
    }

    @Override
    @SuppressWarnings("squid:S1221") // Methods should not be named "tostring", "hashcode" or "equal"
    public Predicate equal(Expression<?> x, Expression<?> y) {
        requireNonNull(x);
        requireNonNull(y);
        return criteriaBuilder.equal(wrapPredicateIfNecessary(x), wrapPredicateIfNecessary(y));
    }

    @Override
    @SuppressWarnings("squid:S1221") // Methods should not be named "tostring", "hashcode" or "equal"
    public Predicate equal(Expression<?> x, Object y) {
        requireNonNull(x);
        if (y == null) {
            return isNull(x);
        }
        return criteriaBuilder.equal(wrapPredicateIfNecessary(x), y);
    }

    @Override
    @SuppressWarnings("squid:S1221") // Methods should not be named "tostring", "hashcode" or "equal"
    public <X, T> Predicate equal(SingularAttribute<X, T> attribute, EntityContext<X> left, EntityContext<X> right) {
        return equal(left.get(attribute), right.get(attribute));
    }

    @Override
    public Predicate notEqual(Expression<?> x, Expression<?> y) {
        requireNonNull(x);
        requireNonNull(y);
        return criteriaBuilder.notEqual(wrapPredicateIfNecessary(x), wrapPredicateIfNecessary(y));
    }

    @Override
    public Predicate notEqual(Expression<?> x, Object y) {
        requireNonNull(x);
        if (y == null) {
            return isNotNull(x);
        }
        return criteriaBuilder.notEqual(wrapPredicateIfNecessary(x), y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.greaterThan(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y) {
        return criteriaBuilder.greaterThan(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.greaterThanOrEqualTo(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return criteriaBuilder.greaterThanOrEqualTo(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.lessThan(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
        return criteriaBuilder.lessThan(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.lessThanOrEqualTo(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return criteriaBuilder.lessThanOrEqualTo(x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.between(v, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y) {
        return criteriaBuilder.between(v, x, y);
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return criteriaBuilder.gt(x, y);
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Number y) {
        return criteriaBuilder.gt(x, y);
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
        return criteriaBuilder.ge(x, y);
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Number y) {
        return criteriaBuilder.ge(x, y);
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return criteriaBuilder.lt(x, y);
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Number y) {
        return criteriaBuilder.lt(x, y);
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
        return criteriaBuilder.le(x, y);
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Number y) {
        return criteriaBuilder.le(x, y);
    }

    @Override
    public <N extends Number> Expression<N> neg(Expression<N> x) {
        return criteriaBuilder.neg(x);
    }

    @Override
    public <N extends Number> Expression<N> abs(Expression<N> x) {
        return criteriaBuilder.abs(x);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
        return criteriaBuilder.sum(x, y);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
        return criteriaBuilder.sum(x, y);
    }

    @Override
    public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
        return criteriaBuilder.sum(x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
        return criteriaBuilder.prod(x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
        return criteriaBuilder.prod(x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
        return criteriaBuilder.prod(x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y) {
        return criteriaBuilder.diff(x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
        return criteriaBuilder.diff(x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
        return criteriaBuilder.diff(x, y);
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
        return criteriaBuilder.quot(x, y);
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> x, Number y) {
        return criteriaBuilder.quot(x, y);
    }

    @Override
    public Expression<Number> quot(Number x, Expression<? extends Number> y) {
        return criteriaBuilder.quot(x, y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
        return criteriaBuilder.mod(x, y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Integer y) {
        return criteriaBuilder.mod(x, y);
    }

    @Override
    public Expression<Integer> mod(Integer x, Expression<Integer> y) {
        return criteriaBuilder.mod(x, y);
    }

    @Override
    public Expression<Double> sqrt(Expression<? extends Number> x) {
        return criteriaBuilder.sqrt(x);
    }

    @Override
    public Expression<Long> toLong(Expression<? extends Number> number) {
        return criteriaBuilder.toLong(number);
    }

    @Override
    public Expression<Integer> toInteger(Expression<? extends Number> number) {
        return criteriaBuilder.toInteger(number);
    }

    @Override
    public Expression<Float> toFloat(Expression<? extends Number> number) {
        return criteriaBuilder.toFloat(number);
    }

    @Override
    public Expression<Double> toDouble(Expression<? extends Number> number) {
        return criteriaBuilder.toDouble(number);
    }

    @Override
    public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
        return criteriaBuilder.toBigDecimal(number);
    }

    @Override
    public Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
        return criteriaBuilder.toBigInteger(number);
    }

    @Override
    public Expression<String> toString(Expression<Character> character) {
        return criteriaBuilder.toString(character);
    }

    @Override
    public <T> Expression<T> literal(T value) {
        return criteriaBuilder.literal(value);
    }

    @Override
    public <T> Expression<T> nullLiteral(Class<T> resultClass) {
        return criteriaBuilder.nullLiteral(resultClass);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
        return criteriaBuilder.parameter(paramClass);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
        return criteriaBuilder.parameter(paramClass, name);
    }

    @Override
    public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
        return criteriaBuilder.isEmpty(collection);
    }

    @Override
    public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
        return criteriaBuilder.isNotEmpty(collection);
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
        return criteriaBuilder.size(collection);
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(C collection) {
        return criteriaBuilder.size(collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(Expression<E> elem, Expression<C> collection) {
        return criteriaBuilder.isMember(elem, collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(E elem, Expression<C> collection) {
        return criteriaBuilder.isMember(elem, collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> elem, Expression<C> collection) {
        return criteriaBuilder.isNotMember(elem, collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(E elem, Expression<C> collection) {
        return criteriaBuilder.isNotMember(elem, collection);
    }

    @Override
    public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
        return criteriaBuilder.values(map);
    }

    @Override
    public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
        return criteriaBuilder.keys(map);
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern) {
        return criteriaBuilder.like(x, pattern);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern) {
        return criteriaBuilder.like(x, pattern);
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return criteriaBuilder.like(x, pattern, escapeChar);
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return criteriaBuilder.like(x, pattern, escapeChar);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return criteriaBuilder.like(x, pattern, escapeChar);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, char escapeChar) {
        return criteriaBuilder.like(x, pattern, escapeChar);
    }

    // Equivalent to like("%" + value + "%")
    @Override
    public Predicate likeNonAnchored(Expression<String> x, String value) {
        return criteriaBuilder.like(x, format("%%%s%%", value));
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern) {
        return criteriaBuilder.notLike(x, pattern);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern) {
        return criteriaBuilder.notLike(x, pattern);
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return criteriaBuilder.notLike(x, pattern, escapeChar);
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return criteriaBuilder.notLike(x, pattern, escapeChar);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return criteriaBuilder.notLike(x, pattern, escapeChar);
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
        return criteriaBuilder.notLike(x, pattern, escapeChar);
    }

    @Override
    public Expression<String> concat(Expression<String> x, Expression<String> y) {
        return criteriaBuilder.concat(x, y);
    }

    @Override
    public Expression<String> concat(Expression<String> x, String y) {
        return criteriaBuilder.concat(x, y);
    }

    @Override
    public Expression<String> concat(String x, Expression<String> y) {
        return criteriaBuilder.concat(x, y);
    }

    @Override
    public Expression<String> concat(List<Expression<String>> values) {
        requireNonNull(values);
        if (values.isEmpty()) {
            return literal("");
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        if (values.size() == 2) {
            return concat(values.get(0), values.get(1));
        }
        /* mutable */ Expression<String> current = literal("");
        for (Expression<String> value : values) {
            current = concat(current, value);
        }
        return current;
    }

    @Override
    public Expression<String> substring(Expression<String> x, Expression<Integer> from) {
        return criteriaBuilder.substring(x, from);
    }

    @Override
    public Expression<String> substring(Expression<String> x, int from) {
        return criteriaBuilder.substring(x, from);
    }

    @Override
    public Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len) {
        return criteriaBuilder.substring(x, from, len);
    }

    @Override
    public Expression<String> substring(Expression<String> x, int from, int len) {
        return criteriaBuilder.substring(x, from, len);
    }

    @Override
    public Expression<String> trim(Expression<String> x) {
        return criteriaBuilder.trim(x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<String> x) {
        return criteriaBuilder.trim(ts, x);
    }

    @Override
    public Expression<String> trim(Expression<Character> t, Expression<String> x) {
        return criteriaBuilder.trim(t, x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
        return criteriaBuilder.trim(ts, t, x);
    }

    @Override
    public Expression<String> trim(char t, Expression<String> x) {
        return criteriaBuilder.trim(t, x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
        return criteriaBuilder.trim(ts, t, x);
    }

    @Override
    public Expression<String> lower(Expression<String> x) {
        return criteriaBuilder.lower(x);
    }

    @Override
    public Expression<String> upper(Expression<String> x) {
        return criteriaBuilder.upper(x);
    }

    @Override
    public Expression<Integer> length(Expression<String> x) {
        return criteriaBuilder.length(x);
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
        return criteriaBuilder.locate(x, pattern);
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, String pattern) {
        return criteriaBuilder.locate(x, pattern);
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
        return criteriaBuilder.locate(x, pattern, from);
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, String pattern, int from) {
        return criteriaBuilder.locate(x, pattern, from);
    }

    @Override
    public Expression<Date> currentDate() {
        return criteriaBuilder.currentDate();
    }

    @Override
    public Expression<Timestamp> currentTimestamp() {
        return criteriaBuilder.currentTimestamp();
    }

    @Override
    public Expression<Time> currentTime() {
        return criteriaBuilder.currentTime();
    }

    @Override
    public <T> In<T> in(Expression<? extends T> expression) {
        return criteriaBuilder.in(expression);
    }

    @Override
    public <T> Predicate in(Expression<? extends T> expression, Set<T> values) {
        if (values == null) {
            return alwaysTrue();
        }
        if (values.isEmpty()) {
            return alwaysFalse();
        }
        final In<T> predicate = criteriaBuilder.in(expression);
        for (final T value : values) {
            predicate.value(value);
        }
        return predicate;
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y) {
        return criteriaBuilder.coalesce(x, y);
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
        return criteriaBuilder.coalesce(x, y);
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
        return criteriaBuilder.nullif(x, y);
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
        return criteriaBuilder.nullif(x, y);
    }

    @Override
    public <T> Coalesce<T> coalesce() {
        return criteriaBuilder.coalesce();
    }

    @Override
    public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        return criteriaBuilder.selectCase(expression);
    }

    @Override
    public <R> Case<R> selectCase() {
        return criteriaBuilder.selectCase();
    }

    @Override
    public <T> Expression<T> function(String name, Class<T> type, Expression<?>... args) {
        return criteriaBuilder.function(name, type, args);
    }

    @Override
    public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
        return criteriaBuilder.treat(join, type);
    }

    @Override
    public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
        return criteriaBuilder.treat(join, type);
    }

    @Override
    public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
        return criteriaBuilder.treat(join, type);
    }

    @Override
    public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
        return criteriaBuilder.treat(join, type);
    }

    @Override
    public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
        return criteriaBuilder.treat(join, type);
    }

    @Override
    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
        return criteriaBuilder.treat(path, type);
    }

    @Override
    public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
        return criteriaBuilder.treat(root, type);
    }
}
