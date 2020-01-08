package org.grapple.utils;

import javax.persistence.criteria.Expression;
import org.grapple.query.EntityContext;

public interface EntitySortKey<X> {

    Expression<?> getPath(EntityContext<X> entityContext);

}
