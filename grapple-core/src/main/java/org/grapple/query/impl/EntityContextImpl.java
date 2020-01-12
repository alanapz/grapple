package org.grapple.query.impl;

import java.util.function.Supplier;
import javax.persistence.criteria.From;
import org.grapple.query.EntityContext;
import org.grapple.query.QueryBuilder;

final class EntityContextImpl<X> extends AbstractEntityContextImpl<X> implements EntityContext<X> {

    EntityContextImpl(RootFetchSetImpl<?> rootFetchSet, QueryWrapper queryWrapper, QueryBuilder queryBuilder, Supplier<? extends From<?, X>> entity) {
        super(rootFetchSet, queryWrapper, queryBuilder, entity);
    }
}
