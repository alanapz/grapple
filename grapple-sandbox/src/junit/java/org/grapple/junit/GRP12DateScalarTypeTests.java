package org.grapple.junit;

import app.Launch;
import graphql.GraphQL;
import org.junit.Test;
import sandbox.grapple.tests.TestSupport;

public class GRP12DateScalarTypeTests extends TestSupport {

    @Test
    public void testDateLocalTypes() {
        final GraphQL graphQL = buildGraphQL();
        Launch.runQuery(graphQL, "query { listUsers { results { lastLoginDate { timestamp, local } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, local(pattern: \"HH:mm\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, local(pattern: \"dd/MM/yyyy HH:mm:ss\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, local(pattern: \"iso\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, local(pattern: \"rfc1123\") } } } }");
    }

    @Test
    public void testDateUtcTypes() {
        final GraphQL graphQL = buildGraphQL();
        Launch.runQuery(graphQL, "query { listUsers { results { lastLoginDate { timestamp, utc } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, utc(pattern: \"HH:mm\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, utc(pattern: \"dd/MM/yyyy HH:mm:ss\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, utc(pattern: \"iso\") } } } }");
        Launch.runQuery(graphQL, "query{ listUsers { results { lastLoginDate { timestamp, utc(pattern: \"rfc1123\") } } } }");
    }

    @Test
    public void testDateFilterTypes() {
        final GraphQL graphQL = buildGraphQL();
        Launch.runQuery(graphQL, "query { listUsers(filter: {lastLoginDate: {is: 1581851886}}) { results { lastLoginDate { timestamp } } } }");
        Launch.runQuery(graphQL, "query { listUsers(filter: {lastLoginDate: {gt: \"2011-12-03T10:15:30Z\"}}) { results { id, lastLoginDate { timestamp } } } }");
    }
}
