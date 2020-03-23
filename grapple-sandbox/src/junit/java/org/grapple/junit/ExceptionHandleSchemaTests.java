package org.grapple.junit;

import static org.grapple.utils.Utils.toSet;

import javax.persistence.EntityManager;
import app.Launch;
import app.UserService;
import graphql.GraphQL;
import org.grapple.query.QueryDefinitions;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.junit.jupiter.api.Test;

public class ExceptionHandleSchemaTests extends SchemaTestSupport {

    @Test
    public void testExceptionHandling() {

        final EntityManager entityManager = getEntityManager();

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();

        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            entitySchemaScanner.importQueries(new UserService(entityManager));
        });

        System.out.println(entitySchema);
        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();
        Launch.runQuery(graphQL, "query{ alwaysThrowException { displayName }}");

    }

    public static class ThreadDetails {

        public final long threadId;

        public final String name;

        public final String className;

        private ThreadDetails(Thread thread) {
            this.threadId = thread.getId();
            this.name = thread.getName();
            this.className = thread.getClass().getName();
        }
    }
}
