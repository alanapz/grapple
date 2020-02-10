//package app;
//
//import static org.grapple.query.EntityRootBuilder.entityRoot;
//import static org.grapple.utils.Utils.toSet;
//
//import javax.persistence.EntityManager;
//import graphql.ExecutionInput;
//import graphql.ExecutionResult;
//import graphql.GraphQL;
//import org.grapple.schema.EntityDefinitionScannerCallback;
//import org.grapple.schema.EntitySchema;
//import org.grapple.schema.EntitySchemaResult;
//import org.grapple.schema.impl.EntitySchemaProvider;
//import sandbox.grapple.entity.UserPrivateMessage;
//
//public class QueryFieldVisibilitySchemaTests {
//
//    public static void main(String[] args) throws Exception {
//        Launch.runTest(QueryFieldVisibilitySchemaTests::testQueryFieldVisibility);
//    }
//
//    private static void testQueryFieldVisibility(EntityManager entityManager) {
//        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
//        entitySchema.importDefinitions(toSet("app", "sandbox"), new EntityDefinitionScannerCallback());
//        entitySchema.getEntity(UserPrivateMessage.class).addQuery(queryDefinition -> {
//            queryDefinition.setName("listAllPrivateMessages");
//            queryDefinition.setQueryResolver((env, fetchSet, params) -> fetchSet.execute(entityManager, entityRoot(UserPrivateMessage.class)));
//            queryDefinition.setRolesRequired(toSet("admin"));
//        });
//        System.out.println(entitySchema);
//        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
//        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();
//        System.out.println(graphQL);
//        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllPrivateMessages { offset, count, total, results { timestamp { epochSeconds, local(pattern: \"HHmm\"), utc }}}}").build();
//        System.out.println(executionInput);
//        ExecutionResult executionResult = graphQL.execute(executionInput);
//        System.out.print(executionResult);
//    }
//}
