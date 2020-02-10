package org.grapple.junit;

import static org.grapple.query.EntityRootBuilder.entityRoot;
import static org.grapple.utils.Utils.toSet;

import javax.persistence.EntityManager;
import app.Launch;
import graphql.GraphQL;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.junit.jupiter.api.Test;
import sandbox.grapple.entity.User;

public class InstantSchemaTests extends SchemaTestSupport {

    @Test
    public void testInstant() {

        final EntityManager entityManager = getEntityManager();

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            entitySchemaScanner.importQueries(new Object() {

                @GrappleQuery
                public QueryResultList<User> listUsers(RootFetchSet<User> fetches) {
                    return fetches.execute(entityManager, entityRoot(User.class));
                }

            });
        });

        System.out.println(entitySchema);
        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();
        Launch.runQuery(graphQL, "query{ listUsers { offset, count, total, results { id, displayName2, lastLoginDate{epochSeconds, local, utc} } } }");
    }
}
