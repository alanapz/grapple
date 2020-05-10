package sandbox.grapple.tests;

import static org.grapple.query.EntityRootBuilder.entityRoot;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
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

public abstract class TestSupport {

    protected final Set<Object> operationSources = new HashSet<>();

    protected final EntityManager entityManager = Persistence.createEntityManagerFactory("grapple-sandbox").createEntityManager();

    protected GraphQL buildGraphQL() {
        return GraphQL.newGraphQL(buildDefaultSchema().getSchema()).build();
    }

    protected EntitySchemaResult buildDefaultSchema() {
        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            operationSources.forEach(entitySchemaScanner::importOperations);
            entitySchemaScanner.importOperations(new Object() {

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

        return entitySchema.generate();
    }
}
