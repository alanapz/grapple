package org.grapple.query.impl;

import java.util.function.Supplier;
import javax.persistence.criteria.Join;
import org.grapple.query.JoinedEntityContext;
import org.grapple.query.QueryBuilder;

final class JoinedEntityContextImpl<X> extends AbstractEntityContextImpl<X> implements JoinedEntityContext<X> {

    private final Supplier<? extends Join<?, X>> entity;

    JoinedEntityContextImpl(RootFetchSetImpl<?> rootFetchSet, QueryWrapper queryWrapper, QueryBuilder queryBuilder, Supplier<? extends Join<?, X>> entity) {
        super(rootFetchSet, queryWrapper, queryBuilder, entity);
        this.entity = entity;
    }

    @Override
    public Join<?, X> get() {
        return entity.get();
    }
}
