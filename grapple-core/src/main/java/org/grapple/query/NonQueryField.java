package org.grapple.query;

public interface NonQueryField<X, T> extends EntityField<X, T> {

    NonQueryFieldResolver<X, T> getResolver();

}
