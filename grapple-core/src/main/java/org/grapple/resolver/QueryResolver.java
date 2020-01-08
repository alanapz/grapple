package org.grapple.resolver;

import org.grapple.query.QueryResultList;

public interface QueryResolver<X> {

    QueryResultList execute(QueryResolverParameters<X> parameters);
}
