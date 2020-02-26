package org.grapple.query;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import org.grapple.core.MetadataKey;
import org.grapple.utils.LazyValue;

final class LazyQueryField<X, T> implements QueryField<X, T> {

    private final LazyValue<QueryField<X, T>> source;

    LazyQueryField(LazyValue<QueryField<X, T>> source) {
        this.source = requireNonNull(source, "source");
    }

    @Override
    public String getName() {
        return source.get().getName();
    }

    @Override
    public EntityResultType<T> getResultType() {
        return source.get().getResultType();
    }

    @Override
    public Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder) {
        return source.get().prepare(ctx, queryBuilder);
    }

    @Override
    public Expression<T> getExpression(EntityContext<X> ctx, QueryBuilder queryBuilder) {
        return source.get().getExpression(ctx, queryBuilder);
    }

    @Override
    public Expression<?> getOrderBy(EntityContext<X> ctx, QueryBuilder queryBuilder) {
        return source.get().getOrderBy(ctx, queryBuilder);
    }

    @Override
    public <M> M getMetadata(MetadataKey<M> metadataKey) {
        return source.get().getMetadata(metadataKey);
    }

    @Override
    public String toString() {
        return source.get().toString();
    }
}
