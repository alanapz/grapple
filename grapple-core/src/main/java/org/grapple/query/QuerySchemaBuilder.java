package org.grapple.query;

public interface QuerySchemaBuilder {

    public <X> void addEntity(Class<X> entityClass);
}
