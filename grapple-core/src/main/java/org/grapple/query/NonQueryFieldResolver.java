package org.grapple.query;

import java.util.function.Function;
import javax.persistence.Tuple;

@FunctionalInterface
public interface NonQueryFieldResolver<X, T> {

    Function<Tuple, T> get(EntityContext<X> ctx, QueryBuilder queryBuilder);

}