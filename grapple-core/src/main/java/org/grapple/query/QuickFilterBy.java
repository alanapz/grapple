package org.grapple.query;

import javax.persistence.criteria.Expression;

public interface QuickFilterBy<X> {

    Expression<String> getPath(EntityContext<X> entityContext);

}
