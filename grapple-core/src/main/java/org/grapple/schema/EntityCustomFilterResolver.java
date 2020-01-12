package org.grapple.schema;

import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.EntityFilter;
import org.grapple.query.FetchSet;

@FunctionalInterface
public interface EntityCustomFilterResolver<X, T> {

    EntityFilter<X> get(DataFetchingEnvironment env, FetchSet<X> fetchSet, T args);

}

