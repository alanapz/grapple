package org.grapple.utils;

import javax.persistence.criteria.Expression;
import org.grapple.query.EntityContext;

public interface QuickFilterBy<X> {

    Expression<String> getPath(EntityContext<X> entityContext);

}
