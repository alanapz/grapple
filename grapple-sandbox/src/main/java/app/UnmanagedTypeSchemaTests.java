package app;

import static org.grapple.query.EntityRootBuilder.entityRoot;
import static org.grapple.utils.Utils.toSet;

import javax.persistence.EntityManager;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.grapple.schema.EntityDefinitionScannerCallback;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.impl.EntitySchemaProvider;
import sandbox.grapple.entity.UserPrivateMessage;

public class UnmanagedTypeSchemaTests {

    public static void main(String[] args) throws Exception {
        Launch.runTest(UnmanagedTypeSchemaTests::testInstant);
    }

    private static void testInstant(EntityManager entityManager) {
        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.importDefinitions(toSet("app", "sandbox"), new EntityDefinitionScannerCallback());
        entitySchema.getEntity(UserPrivateMessage.class).addQuery(queryDefinition -> {
            queryDefinition.setQueryName("listAllPrivateMessages");
            queryDefinition.setQueryResolver((env, fetchSet, params) -> fetchSet.execute(entityManager, entityRoot(UserPrivateMessage.class)));
        });
        System.out.println(entitySchema);

        final GraphQL graphQL = Launch.buildGraphQL(entitySchema);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllPrivateMessages { results { timestamp { epochSeconds, local(pattern: \"HHmm\"), utc }}}}").build();
        System.out.println(executionInput);
        ExecutionResult executionResult = graphQL.execute(executionInput);
        System.out.print(executionResult);

    }
}
