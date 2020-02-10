package org.grapple.query;

import javax.persistence.criteria.Expression;

public interface EntitySortKey<X> {

    Expression<?> getPath(EntityContext<X> entityContext);

}
