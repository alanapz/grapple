package org.grapple.schema;

import graphql.schema.DataFetcher;
import org.grapple.core.Chainable;
import org.grapple.core.ElementVisibility;
import org.grapple.core.Validatable;

public interface UnmanagedQueryDefinition extends Chainable<UnmanagedQueryDefinition>, Validatable {

    String getQueryAlias();

    ElementVisibility getVisibility();

    void setVisibility(ElementVisibility visibility);

    UnmanagedQueryBuilder getQueryBuilder();

    void setQueryBuilder(UnmanagedQueryBuilder queryBuilder);

    DataFetcher<?> getDataFetcher();

    void setDataFetcher(DataFetcher<?> dataFetcher);

}
