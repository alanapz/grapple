package org.grapple.query;

import javax.persistence.Tuple;

@FunctionalInterface
public interface NonQuerySelection<T> {

    T get(Tuple tuple);

}
