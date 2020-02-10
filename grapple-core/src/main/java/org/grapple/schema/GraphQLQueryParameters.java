package org.grapple.schema;

import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.QueryParameter;

public class GraphQLQueryParameters {

    public static final QueryParameter<String> QueryName = new QueryParameter<>("queryName");

    public static final QueryParameter<DataFetchingEnvironment> Environment = new QueryParameter<>("environment");

    public static final QueryParameter<Object> QuerySource = new QueryParameter<>("querySource");

    private GraphQLQueryParameters() {

    }
}
