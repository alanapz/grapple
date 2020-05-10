package org.grapple.reflect;

import java.lang.reflect.Method;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.EntityFilter;
import org.grapple.query.FetchSet;
import org.grapple.reflect.TypeLiteral;

public interface EntityFilterMethodMetadata<X, T> {

    Class<X> getEntityClass();

    Method getMethod();

    String getFilterName();

    String getDescription();

    String getDeprecationReason();

    TypeLiteral<T> getParameterType();

    EntityFilter<X> generate(DataFetchingEnvironment env, FetchSet<X> fetchSet, T args);

}
