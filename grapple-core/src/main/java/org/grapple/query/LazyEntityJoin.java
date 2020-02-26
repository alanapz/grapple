package org.grapple.query;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import org.grapple.core.MetadataKey;
import org.grapple.utils.LazyValue;

final class LazyEntityJoin<X, Y> implements EntityJoin<X, Y> {

    private final LazyValue<EntityJoin<X, Y>> source;

    LazyEntityJoin(LazyValue<EntityJoin<X, Y>> source) {
        this.source = requireNonNull(source, "source");
    }

    @Override
    public String getName() {
        return source.get().getName();
    }

    @Override
    public EntityResultType<Y> getResultType() {
        return source.get().getResultType();
    }

    @Override
    public Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
        return source.get().join(ctx, queryBuilder, entity);
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
