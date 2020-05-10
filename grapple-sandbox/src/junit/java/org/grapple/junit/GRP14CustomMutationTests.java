package org.grapple.junit;

import app.Launch;
import graphql.GraphQL;
import org.grapple.invoker.GrappleMutation;
import org.junit.Test;
import sandbox.grapple.tests.TestSupport;

public class GRP14CustomMutationTests extends TestSupport {

    public GRP14CustomMutationTests() {
        operationSources.add(new CustomMutationService());
    }

    public static class CustomMutationService {

        @GrappleMutation
        public void setSystemProperty(String key, String value) {
            Thread.dumpStack();
        }
    }

    @Test
    public void testCustomMutation() {
        final GraphQL graphQL = buildGraphQL();
        Launch.runQuery(graphQL, "mutation { setSystemProperty(key: \"alan\", value: \"123\") }");
    }

}
