package org.grapple.resolver;

import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.RootFetchSet;

public interface QueryResolverParameters<X> {

    RootFetchSet<X> query();

    Class<X> entityType();

    DataFetchingEnvironment dataFetchingEnvironment();

}
