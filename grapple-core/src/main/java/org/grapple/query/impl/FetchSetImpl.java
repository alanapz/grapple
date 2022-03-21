package org.grapple.query.impl;

import org.grapple.query.EntityJoin;
import org.grapple.query.FetchSet;
import org.grapple.query.RootFetchSet;
import org.jetbrains.annotations.NotNull;

final class FetchSetImpl<X> extends AbstractFetchSetImpl<X> {

    private final RootFetchSetImpl<?> fetchRoot;

    private final FetchSet<?> parent;

    private final EntityJoin<?, X> joinedBy;

    FetchSetImpl(@NotNull RootFetchSetImpl<?> fetchRoot, @NotNull FetchSet<?> parent, @NotNull EntityJoin<?, X> joinedBy) {
        this.fetchRoot = fetchRoot;
        this.parent = parent;
        this.joinedBy = joinedBy;
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