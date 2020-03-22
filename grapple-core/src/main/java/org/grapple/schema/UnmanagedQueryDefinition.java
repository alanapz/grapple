package org.grapple.schema;

import graphql.schema.DataFetcher;
import org.grapple.core.Chainable;
import org.grapple.core.Validatable;

public interface UnmanagedQueryDefinition extends EntitySchemaElement, Chainable<UnmanagedQueryDefinition>, Validatable {

    String getQueryAlias();

    UnmanagedQueryBuilder getQueryBuilder();

    void setQueryBuilder(UnmanagedQueryBuilder queryBuilder);

    DataFetcher<?> getDataFetcher();

    void setDataFetcher(DataFetcher<?> dataFetcher);

}
