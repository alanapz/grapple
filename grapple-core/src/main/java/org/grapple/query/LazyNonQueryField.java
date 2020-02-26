package org.grapple.query;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import javax.persistence.Tuple;
import org.grapple.core.MetadataKey;
import org.grapple.utils.LazyValue;

final class LazyNonQueryField<X, T> implements NonQueryField<X, T> {

    private final LazyValue<NonQueryField<X, T>> source;

    LazyNonQueryField(LazyValue<NonQueryField<X, T>> source) {
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
    public NonQueryFieldResolver<X, T> getResolver() {
        return source.get().getResolver();
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
