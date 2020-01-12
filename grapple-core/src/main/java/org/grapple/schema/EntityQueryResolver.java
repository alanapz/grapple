package org.grapple.schema;

import java.util.Map;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;

@FunctionalInterface
public interface EntityQueryResolver<X> {

    QueryResultList<X> execute(DataFetchingEnvironment env, RootFetchSet<X> fetchSet, Map<String, Object> queryParameters);

}
