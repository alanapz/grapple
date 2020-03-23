package org.grapple.junit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import app.UserService;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.grapple.query.QueryDefinitions;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.impl.EntitySchemaProvider;

public abstract class SchemaTestSupport {

    private static EntityManagerFactory entityManagerFactory;

    protected EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    protected GraphQL buildSampleSchema() {
        getEntityManager();
        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            entitySchemaScanner.importQueries(new UserService(getEntityManager()));
        });
        System.out.println(entitySchema);
        final GraphQLSchema graphQLSchema = entitySchema.generate().getSchema();
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    static {
        SchemaTestSupport.entityManagerFactory = Persistence.createEntityManagerFactory("grapple-sandbox");
    }
}
