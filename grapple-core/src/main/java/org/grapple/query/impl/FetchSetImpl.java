package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;

import org.grapple.query.EntityJoin;
import org.grapple.query.FetchSet;
import org.grapple.query.RootFetchSet;

final class FetchSetImpl<X> extends AbstractFetchSetImpl<X> {

    private final RootFetchSetImpl<?> fetchRoot;

    private final FetchSet<?> parent;

    private final EntityJoin<?, X> joinedBy;

    FetchSetImpl(RootFetchSetImpl<?> fetchRoot, FetchSet<?> parent, EntityJoin<?, X> joinedBy) {
        this.fetchRoot = requireNonNull(fetchRoot, "fetchRoot");
        this.parent = requireNonNull(parent, "parent");
        this.joinedBy = requireNonNull(joinedBy, "joinedBy");
    }

    @Override
    public RootFetchSet<?> getFetchRoot() {
        return fetchRoot;
    }

    @Override
    public FetchSet<?> getFetchParent() {
        return parent;
    }

    @Override
    public EntityJoin<?, X> getJoinedBy() {
        return joinedBy;
    }

    @Override
    protected RootFetchSetImpl<?> fetchRoot() {
        return fetchRoot;
    }
}