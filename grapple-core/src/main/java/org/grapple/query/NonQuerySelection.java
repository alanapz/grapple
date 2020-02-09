package org.grapple.query;

import javax.persistence.Tuple;

@FunctionalInterface
public interface NonQuerySelection<X, T> {

    T get(Tuple tuple);

}
