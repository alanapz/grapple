package org.grapple.junit;

import app.Launch;
import graphql.GraphQL;
import org.junit.jupiter.api.Test;

public class GRP10BatchedQueryTests extends SchemaTestSupport {

    @Test
    public void testIndividualQueries() {
        final GraphQL graphQL = buildSampleSchema();
//        Launch.runQuery(graphQL, "query { getUserById(id: 1){ id }, queries1{ getUserById(id: 1){ id }}, queries2{ getUserById(id: 2){ id }} }");
        Launch.runQuery(graphQL, "query {" +
                "   item1: getUserById(id: 2){ id, displayName }, " +
                "   item2: getUserById(id: 40){ id, displayName }, " +
                "   item4: getCurrentUser { results { id, displayName }}, " +
                "   item3: getUserById(id: 3){ id, displayName } }");


//        Launch.runQuery(graphQL, "query { multipleQueries{" +
//                "_2 { getUserById(id: 2){ id, displayName }}, " +
//                "_3 { getUserById(id: 40){ id, displayName }}, " +
//                "_1 { getUserById(id: 3){ id, displayName }}} }");
    }
}
