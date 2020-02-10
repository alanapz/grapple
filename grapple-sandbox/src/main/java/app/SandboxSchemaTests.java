//package app;
//
//import static org.grapple.utils.Utils.toSet;
//
//import javax.persistence.EntityManager;
//import org.grapple.schema.EntityDefinitionScannerCallback;
//import org.grapple.schema.EntitySchema;
//import org.grapple.schema.impl.EntitySchemaProvider;
//
//public class SandboxSchemaTests {
//
//    public static void main(String[] args) throws Exception {
//        Launch.runTest(SandboxSchemaTests::testDescription);
//    }
//
//    private static void testDescription(EntityManager entityManager) {
//        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
//        entitySchema.importDefinitions(toSet("app", "sandbox"), new EntityDefinitionScannerCallback());
//        System.out.println(entitySchema);
//    }
//}
