package app;

import static org.grapple.query.EntityRootBuilder.entityRoot;
import static org.grapple.utils.Utils.toSet;

import java.util.Optional;
import javax.persistence.EntityManager;
import graphql.GraphQL;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.Filters;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.impl.EntitySchemaProvider;
import sandbox.grapple.UserField;
import sandbox.grapple.entity.User;

public class CustomResolverSchemaTests {

    public static void main(String[] args) throws Exception {
        Launch.runTest(CustomResolverSchemaTests::testCustomResolver);
    }

    private static void testCustomResolver(EntityManager entityManager) {
        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            entitySchemaScanner.importQueries(new Object() {

                @GrappleQuery
                public QueryResultList<User> listUsers(RootFetchSet<User> fetches) {
                    return fetches.execute(entityManager, entityRoot(User.class));
                }

                @GrappleQuery
                public QueryResultRow<User> getUserById(RootFetchSet<User> fetches, int userId) {
                    fetches.filter(Filters.isEqual(UserField.Id, userId));
                    return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult().orElse(null);
                }

                @GrappleQuery
                public Optional<QueryResultRow<User>> getUserByIdOrNull(RootFetchSet<User> fetches, int userId) {
                    fetches.filter(Filters.isEqual(UserField.Id, userId));
                    return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult();
                }
            });
        });

        System.out.println(entitySchema);
        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();
        Launch.runQuery(graphQL, "query{ listUsers { offset, count, total, results { id } } }");
        Launch.runQuery(graphQL, "query{ getUserById(userId: 1) { id } }");
        Launch.runQuery(graphQL, "query{ getUserById(userId: 123) { id } }");
        Launch.runQuery(graphQL, "query{ getUserByIdOrNull(userId: 1) { id } }");
        Launch.runQuery(graphQL, "query{ getUserByIdOrNull(userId: 123) { id } }");
    }
}
