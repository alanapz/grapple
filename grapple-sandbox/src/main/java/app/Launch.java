package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.query.EntityRootBuilder;
import org.grapple.query.GrappleQuery;
import org.grapple.query.QueryResultItem;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaBuilder;
import sandbox.grapple.CompanyField;
import sandbox.grapple.UserField;
import sandbox.grapple.UserPrivateMessageField;
import sandbox.grapple.UserSortKey;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;

public class Launch {



    public static void main(String[] args) throws Exception {

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("grapple-sandbox");
        final EntityManager entityManager = emf.createEntityManager();

        schemaTest();

        final RootFetchSet<User> fetchSet = GrappleQuery.newQuery();
        fetchSet.select(UserField.ID);
        fetchSet.select(UserField.IS_GREATESST);
        fetchSet.orderBy(UserField.IS_GREATESST, SortDirection.ASC);
        fetchSet.execute(entityManager, EntityRootBuilder.from(User.class));

        if (true) {
            return;
        }


        selectAllMessagesForX(entityManager);
        if (true) {
            return;
        }


//or

//        Map<String, Object> configOverrides = new HashMap<String, Object>();
        //configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");
        //EntityManagerFactory programmaticEmf =
//                Persistence.createEntityManagerFactory("manager1", configOverrides);
    }

    private static void schemaTest() {

        System.out.print(UserField.ID.getResultType());

        final EntitySchema entitySchema = EntitySchemaBuilder.newSchema();
        entitySchema.addEntity(UserPrivateMessage.class, userPrivateMessage -> userPrivateMessage
                .addField(UserPrivateMessageField.TIMESTAMP)
                .importFrom(UserPrivateMessageField.class));
        entitySchema.addEntity(User.class, user -> user.importFrom(UserField.class));

        GraphQLSchema schema = entitySchema.generate();

        SchemaPrinter printer = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
                .includeScalarTypes(true)
                .includeExtendedScalarTypes(true)
                .includeIntrospectionTypes(false)
                .includeDirectives(false)
                .includeSchemaDefinition(true));
        System.out.println(printer.print(schema));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionInput executionInput = null;
//        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { listAllUsers(filter: {displayNamexx: \"alan\"}){ results { displayName } } }")
//                .build();

//        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { listAllUsers(filter:{}) { results { displayName } } }").build();
        executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: {id_in: [1,2,3]}) {results{displayName}}}").build();
        ExecutionResult executionResult = graphQL.execute(executionInput);

        System.out.print(executionResult);



    }

    private QueryResultList listUsers(RootFetchSet<User> fetches) {
        return null;
    }

    private static void selectAllMessagesForX(EntityManager entityManager) {
        final RootFetchSet<UserPrivateMessage> fetchSet = GrappleQuery.newQuery();
        fetchSet.select(UserPrivateMessageField.ID);
        fetchSet.select(UserPrivateMessageField.MESSAGE);
        fetchSet.select(UserPrivateMessageField.TIMESTAMP);
        fetchSet.join(UserPrivateMessageField.SENDER).join(UserField.COMPANY).select(CompanyField.ID);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER).join(UserField.COMPANY).select(CompanyField.ID);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER).join(UserField.COMPANY).select(CompanyField.ID);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER).join(UserField.COMPANY).select(CompanyField.ID);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.orderBy(UserPrivateMessageField.TIMESTAMP, SortDirection.ASC);
        fetchSet.join(UserPrivateMessageField.RECIPIENT).orderBy(UserField.ID, SortDirection.ASC);
        fetchSet.join(UserPrivateMessageField.RECIPIENT).orderBy(UserSortKey.OPWNER_ID, SortDirection.ASC);
/*
        fetchSet.filter(new EntityFilter<UserPrivateMessage>() {

            @Override
            public Predicate apply(EntityContext<UserPrivateMessage> ctx, QueryBuilder builder) {
                return builder.and(
                        builder.equal(ctx.join(UserPrivateMessageField.SENDER).join(UserField.COMPANY).get(CompanyField.ID), 1),
                        builder.equal(ctx.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).get(CompanyField.ID), 1));
            }
        });*/
        // fetchSet.join(UserPrivateMessageField.RECIPIENT);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).add(UserField.ID);
         // fetchSet.filter(UserFetches.USERID_1);
        // fetchSet.filter(UserFetches.filterByClientLabel("aaa0", "bbb0", "ccc0"));

        System.out.print("XX");

        PrettyPrint(fetchSet.execute(entityManager, UserPrivateMessageField.ALL_PRIVATE_MESSAGES).getResults());

        System.out.print("YY");

    }

    private static void PrettyPrint(List<QueryResultItem> results) {
        if (results.isEmpty()) {
            return;
        }
        StringBuffer x;
        final List<Map<String, Object>> allRows = new ArrayList<>();
        for (QueryResultItem result: results) {
            allRows.add(result.getValues());
        }
        final List<String> columnNames = new ArrayList<>(allRows.get(0).keySet());
        final Map<String, Integer> columnWidths = new HashMap<>();
        // Initialise with column widths
        for (String columnName: columnNames) {
            columnWidths.put(columnName, columnName.length());
        }
        // Now loop through all rows ...
        for (Map<String, Object> row: allRows) {
            for (String columnName: columnNames) {
                columnWidths.put(columnName, Math.max(columnWidths.get(columnName), String.valueOf(row.get(columnName)).length()));
            }
        }
        for (String columnName: columnNames) {
            System.out.print(String.format("| %s ", columnName));
            final int distanceToPad = columnWidths.get(columnName) - columnName.length();
            if (distanceToPad > 0) {
                System.out.print(new String(new char[distanceToPad]).replace("\0", " "));
            }
        }
        System.out.println("|");
        for (String columnName: columnNames) {
            final int distanceToPad = columnWidths.get(columnName) - columnName.length();
            System.out.print(new String(new char[columnName.length() + distanceToPad + 4]).replace("\0", "-"));
        }
        System.out.println();
        for (Map<String, Object> row: allRows) {
            for (String columnName: columnNames) {
                final String value = String.valueOf(row.get(columnName));
                System.out.print(String.format("| %s ", value));
                final int distanceToPad = columnWidths.get(columnName) - value.length();
                if (distanceToPad > 0) {
                    System.out.print(new String(new char[distanceToPad]).replace("\0", " "));
                }
            }
            System.out.println("|");
        }
    }


    private static void main() throws Exception {

        if (true) {
//            return;
        }

        // entitySchema.
//        entitySchema.addEntity(User.class).importAllFrom(UserFetches.class);
        //entitySchema.addEntity(User.class).addField(UserFetches.USER_EXP_1.apply("hello"));
/*
entitySchema.addEntity(User.class).addField(UserFetches.USER_EXP_1.apply("world"));
entitySchema.addEntity(User.class).addField(UserFetches.USER_EXP_2.apply("goodbye"));
entitySchema.addEntity(User.class).addField(UserFetches.USER_EXP_2.apply("fun"));
entitySchema.addEntity(Client.class).importAllFrom(UserFetches.class);

entitySchema.addEntity(Context.class).addFields(ContextFetches.ID, ContextFetches.NAME, ContextFetches.BILLING_ENTITY_F).addJoin(ContextFetches.BILLING_ENTITY_J);
entitySchema.addEntity(BillingEntity.class).addFields(BillingEntityFetches.ID, BillingEntityFetches.NAME, BillingEntityFetches.CONTEXT_f).addJoin(BillingEntityFetches.CONTEXT_j);
*/
        /*
        .addFilter(new EntityFilterBuilder().setName("maxDateCreated").setInputType(new TypeToken<List<Instant>>() {} }));
        ;
        */
//        entitySchema.addEntity(EitTransaction.class)
//                .addFields(EitTransactionFetches.ID, EitTransactionFetches.TIMESTAMP, EitTransactionFetches.TYPE);
//                .addFilter("filterById", Integer.class, val ->
//                {

//                });

//        entitySchema.addUnmanagedObject(GraphQLObjectType.newObject().
        //.name("ThreadDetails")
//                .
//                build());

        GraphQLInputType x;

        GraphQLSchema schema = null; //entitySchema.build();


        SchemaPrinter printer = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
                .includeScalarTypes(true)
                .includeExtendedScalarTypes(true)
                .includeIntrospectionTypes(false)
                .includeDirectives(false)
                .includeSchemaDefinition(true));
        System.out.println(printer.print(schema));

        GraphQL graphQL = GraphQL.newGraphQL(schema)
                .build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { listAllUsers { results { username, client1(filter: {label: \"123\"}){id}, client2{id}, client3{id} } } }")
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        Object data = executionResult.getData();
        List<GraphQLError> errors = executionResult.getErrors();

        System.out.println(data);
        System.out.println(errors);


        // entityManager.getCriteriaBuilder().createTupleQuery();
    }

    private static <X> void execute(EntityManager entityManager) {
//        FetchSet<User> fetchSet = new FetchSet<User>();
//        fetchSet.add(UserFetches.ID);
//        fetchSet.add(UserFetches.LABEL);
//        fetchSet.add(UserFetches.CLIENT_XX);
        // fetchSet.filter(UserFetches.USERID_1);
//        fetchSet.filter(UserFetches.filterByClientLabel("aaa0", "bbb0", "ccc0"));
//        fetchSet.filter(UserFetches.filterByClientLabel("aaa1", "bbb1", "ccc1"));
//        fetchSet.filter(UserFetches.filterByClientLabel("aaa2", "bbb2", "ccc2"));
//        fetchSet.filter(UserFetches.filterByClientLabel("aaa3", "bbb3", "ccc3"));
//        fetchSet.filter(UserFetches.filterByClientLabel2("aaa0", "bbb0", "ccc0"));
//        fetchSet.filter(UserFetches.filterByClientLabel2("aaa1", "bbb1", "ccc1"));
//        fetchSet.filter(UserFetches.filterByClientLabel2("aaa2", "bbb2", "ccc2"));
//        fetchSet.filter(UserFetches.filterByClientLabel2("aaa3", "bbb3", "ccc3"));

//        FetchSet<Client> c1= fetchSet.join(UserFetches.CLIENT_1);
        //FetchSet<Client> c2= fetchSet.join(UserFetches.CLIENT_2);
        //c2.add(UserFetches.CLIENT_ID);
        //FetchSet<Client> c3= fetchSet.join(UserFetches.CLIENT_3);
        //c3.add(UserFetches.CLIENT_ID);
        //c3.add(UserFetches.CLIENT_LABEL);

//        FetchSet<User> c1Owner = c1.join(UserFetches.CLIENT_OWNER);
//        FetchSet<Client> c1OwnerClient = c1Owner.join(UserFetches.CLIENT_1);
//        FetchSet<User> c1OwnerClientOwner = c1OwnerClient.join(UserFetches.CLIENT_OWNER);
//        // c1OwnerClientOwner.add(UserFetches.USER_EXP.apply("2"));
//        FetchSet<Client> c1OwnerClientOwnerClient = c1OwnerClientOwner.join(UserFetches.CLIENT_1);
//        FetchSet<User> c1OwnerClientOwnerClientOwner = c1OwnerClientOwnerClient.join(UserFetches.CLIENT_OWNER);
//        // c1OwnerClientOwnerClientOwner.add(UserFetches.USER_EXP.apply("3"));
//        FetchSet<Client> c1OwnerClientOwnerClientOwnerClient = c1OwnerClientOwnerClientOwner.join(UserFetches.CLIENT_1);
//        FetchSet<User> c1OwnerClientOwnerClientOwnerClientOwner = c1OwnerClientOwnerClientOwnerClient.join(UserFetches.CLIENT_OWNER);
//
//        EntitySelection<User, ?> selection =  UserFetches.USER_EXP_1.apply("4");
//
//        EntitySelection<User, ?> selection2 =  UserFetches.USER_EXP_2.apply("4");
//
//        c1OwnerClientOwnerClientOwnerClientOwner.add(selection, selection2);
//
//        EntitySort sort = new EntitySort();
////        sort.add(c2, UserFetches.CLIENT_ID, SortDirection.ASC);
////        sort.add(c2, UserFetches.CLIENT_LABEL, SortDirection.DESC);
////
////        sort.add(c3, UserFetches.CLIENT_LABEL, SortDirection.DESC);
//
//        QueryResultList x = new ExecutionContext(entityManager).execute(User.class, fetchSet, sort, 0, Integer.MAX_VALUE);
//        System.out.println(x);
//        System.out.println(x.getResults().get(0));
//        System.out.println(x.getResults().get(0).get(c1OwnerClientOwnerClientOwnerClientOwner, selection));


    }
}

