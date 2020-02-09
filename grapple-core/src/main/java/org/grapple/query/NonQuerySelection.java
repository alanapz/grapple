package org.grapple.query;

import javax.persistence.Tuple;

public interface NonQuerySelection<X, T> {

    T get(Tuple tuple);

}
