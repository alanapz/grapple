package org.grapple.query;

public interface EntityRoot<T> {

    Class<T> getEntityClass();

    EntityFilter<T> getFilter();

}
