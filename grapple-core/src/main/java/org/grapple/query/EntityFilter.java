package org.grapple.query;

import javax.persistence.criteria.Predicate;

public interface EntityFilter<X> {

    default boolean isAlwaysTrue() {
        return false;
    }

    default boolean isAlwaysFalse() {
        return false;
    }

    Predicate apply(EntityContext<X> ctx, QueryBuilder builder);

}
