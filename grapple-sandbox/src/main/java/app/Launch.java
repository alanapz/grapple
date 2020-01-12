package app;

import static org.grapple.query.EntityRootBuilder.entityRoot;
import static org.grapple.reflect.ClassLiteral.classLiteral;
import static org.grapple.utils.Utils.toSet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
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
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.Filters;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;
import org.grapple.query.impl.QueryProvider;
import org.grapple.reflect.GenericLiteral;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityDefinitionScannerCallback;
import org.grapple.schema.EntityQueryDefinition;
import org.grapple.schema.EntityQueryScannerCallback;
import org.grapple.schema.EntityQueryType;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.schema.FieldFilterDefinition;
import org.grapple.schema.QueryResolverFactory;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.grapple.schema.instrumentation.DebugInstrumentation;
import org.grapple.schema.instrumentation.DebugInstrumentationCallback;
import sandbox.grapple.CompanyField;
import sandbox.grapple.UserField;
import sandbox.grapple.UserPrivateMessageField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;

public class Launch {

    static class Temp {

        public int userId;

        public int companyId;

        public Cox cox;
    }

    static class Cox {

        public int companyId;

    }


    public static void main(String[] args) throws Exception {

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("grapple-sandbox");
        final EntityManager entityManager = emf.createEntityManager();

        System.getProperties().put("entityManager", entityManager);
        System.getProperties().put("usersRoot", entityRoot(User.class));


//        System.exit(0);


//        parameterTest(entityManager);

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.addSchemaListener(new EntitySchemaListener(){

            @Override
            public boolean acceptFieldFilter(TypeLiteral<?> fieldType) {
                return true;
            }

            @Override
            public void configureFieldFilter(FieldFilterDefinition<?> fieldFilter) {
//                System.out.println("XXXXXXXXXXXXXXXX");
//                System.out.println(fieldFilter);
//                System.out.println(fieldFilter.getEntityName());
//                System.out.println(fieldFilter.getFieldType());
                fieldFilter.setDescription("ALAN WOZ HERE");
            }
        });
        entitySchema.importDefinitions(toSet("app", "sandbox"), new EntityDefinitionScannerCallback() {

            public boolean acceptEntity(Class<?> entityClass) {
                return true;
            }

            @Override
            public boolean scanDefinitions(Class<?> definitionsClass) {
//                System.out.println(definitionsClass);
                return true;
            }

            public void configureEntity(EntityDefinition<?> entity) {
                if (entity.getEntityClass() == Company.class) {
                    entity.setName("CompanyXxx");
                    entity.setDescription("Description");
                }
            }

            @Override
            public <X, T> boolean acceptField(EntityDefinition<X> entity, EntityField<X, T> field) {
//                System.out.println("field " + entity + " : " + field);
                return true;
            }

            @Override
            public <X, Y> boolean acceptJoin(EntityDefinition<X> entity, EntityJoin<X, Y> join) {
//                System.out.println("join " + entity + " : " + join);
                return true;
            }
        });

        entitySchema.importQueries(new UserService(entityManager), new EntityQueryScannerCallback() {

            public void entityNotFound(Method method, Class<?> entityClass) {
                // Called when a query couldn't be loaded as matching entity not found
            }

            public boolean acceptQuery(Method method) {
                // Called to accept or not the given method
                return true;
            }

            public void configureQuery(Method method, EntityQueryDefinition<?> queryDefinition) {

            }
        });

        entitySchema.addEntity(User.class).addCustomFilter(new GenericLiteral<List<Set<Integer>>>() {}, filter -> {
            filter.setFieldName("apzTest");
            filter.setFilterResolver((env, fetchSet, rawArgs) -> {
                List<Set<Integer>> filterArgs = (List<Set<Integer>>) (Object) rawArgs;
                final List<EntityFilter<User>> filters = new ArrayList<>();
                for (Set<Integer> values : filterArgs) {
                    filters.add(Filters.contains(UserField.Id, (Set<Integer>) (Object) values));
                }
                EntityFilter<User> all = Filters.or(filters);
                return (ctx, queryBuilder) -> {
                    javax.persistence.criteria.Predicate predicate = queryBuilder.equal(ctx.join(UserField.Company).get(CompanyField.ID), ctx.get(UserField.Id));
                    return queryBuilder.and(predicate, all.apply(ctx, queryBuilder));
                };
            });
        });

        entitySchema.getEntity(User.class).addQuery(queryBuilder ->
        {
            queryBuilder.setQueryName("listAllUsers");
            queryBuilder.setQueryType(EntityQueryType.LIST);
            queryBuilder.setQueryResolver(QueryResolverFactory.defaultQueryResolver(emf, entityRoot(User.class)));
            queryBuilder.addParameter(new GenericLiteral<Set<Integer>>() {}, parameterBuilder -> {
                parameterBuilder.setName("companyIds");
            });
            queryBuilder.addParameter(new GenericLiteral<Set<Stack<Integer>>>() {}, parameterBuilder -> {
                parameterBuilder.setName("companyIds2");
                parameterBuilder.setRequired(true);
            });
        });

        entitySchema.getEntity(User.class).addQuery(queryBuilder -> {
            queryBuilder.setQueryName("getUserById");
            queryBuilder.setQueryType(EntityQueryType.SCALAR_NULL_ALLOWED);
            queryBuilder.setQueryResolver(QueryResolverFactory.defaultQueryResolver(emf, entityRoot(User.class), (fetchSet, params) -> {
                fetchSet.filter(Filters.isEqual(UserField.Id, (int) params.get("userId")));
            } ));
            queryBuilder.addParameter(classLiteral(Integer.class), parameterBuilder -> {
                parameterBuilder.setName("userId");
                parameterBuilder.setRequired(true);
            });
        });

        System.out.println(entitySchema);

        DebugInstrumentationCallback callback = new DebugInstrumentationCallback() {

            @Override
            public Object started(String name, Object params) {
                return null;
            }

            @Override
            public void complete(String name, Object params, Object result, Object callbackToken) {

            }

            @Override
            public void error(String name, Object params, Throwable throwable, Object callbackToken) {

            }
        };

//        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { listAllUsers(filter: {displayNamexx: \"alan\"}){ results { displayName } } }")
//                .build();

//        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { listAllUsers(filter:{}) { results { displayName } } }").build();
        try {
            GraphQL graphQL = GraphQL.newGraphQL(entitySchema.generate()).instrumentation(new DebugInstrumentation<>(callback)).build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: {isNameAlan: {is: true}, id: 5, id_in: [1,2,3], NOT: { id: 99, id_in: [991, 992] }, OR: [ {id: 4 }, {id: 5, id_in: [5, 1]}]}) {results{displayName,company{id,owner{displayName}}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: {isNameAlan: {is: true}, displayName: {like: \"123\"} }) {results{displayName,company{id,owner{displayName}}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: {id: null, dep: { is_null: true }, displayName: { not_ilike: \"xyz\" }, isNameAlan: {is: true, not_in: [ true, false, null], is_null: true} }) {results{displayName}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(
//                    "query{ listAllUsers(filter: { " +
//                            "id: null, " +
//                            "dep: { is_null: true }," +
//                            "displayName: { not_ilike: \"xyz\" }," +
//                            "isNameAlan: {is: true, not_in: [ true, false, null], is_null: true}" +
//                            "}) { results{displayName}}}").build();
            String firstBit = "{ company: { id: { is: 123 }, owner: { displayName: { is_null: true }  } }  }";
            String secondBit = "{ company: { id: { is: 456 }, owner: { isNameAlan: { is: false }  } }  }";
            String thirddBit = "{ company: { id: { is: 4561 } }}";
            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ getOptionalCurrentUser(userId: 1) { isNameAlan,displayName,displayName2,userGuid,company2{displayName},company{id,owner{id}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ getUserById(userId: 1) { isNameAlan,displayName,displayName2,userGuid,company2{displayName},company{id,owner{id}}}}").build();
///**/            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(companyIds2: [[1,2,3]], filter: { apzTest: [[1,2,3],[4,5,6]] }) { results{isNameAlan,displayName,displayName2,userGuid,company2{displayName},company{id,owner{id}}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers{ results{isNameAlan,displayName,company{id,owner{id}}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(orderBy: [{ id: DESC }, {dep: ASC}, {company: { displayName: ASC }} ] ) { results{displayName,company{id}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: { or: [" + firstBit + ", {  and: [" +  firstBit + " , " + secondBit + " ]  }] }) { results{displayName,company{id}}}}").build();
//            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: { not: { id: {is: 123 }}, and: { id: { is: 456 }}, or: [{ id: { is: 456 }}, { id: { is: 457 }}, { id: { is_not: 999 }}] }) { results{displayName,company{id}}}}").build();
            System.out.println(executionInput);
            ExecutionResult executionResult = graphQL.execute(executionInput);
            System.out.print(executionResult);
        }
        catch (Exception e) {
            e.printStackTrace();;
        }

        System.exit(0);

        schemaTest();
        if (true) {
            return;
        }






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


    private static void xx() {

//        {
//            final RootFetchSet<User> fetchSet = GrappleQuery.newQuery();
//            fetchSet.select(UserField.Id);
//            fetchSet.select(UserField.IS_GREATESST);
//            fetchSet.select(UserField.DisplayName);
//            fetchSet.join(UserField.Company, tblCompany -> tblCompany.select(CompanyField.ID));
//            fetchSet.filter(Filters.isNotNull());
//            fetchSet.orderBy(UserField.IS_GREATESST, SortDirection.ASC);
//
//            final QueryResultList<User> resultRows = fetchSet.execute(entityManager, entityRoot(User.class));
//            List<Temp> results = resultRows.map(Temp::new, (row, model) -> {
//                model.userId = row.get(UserField.Id);
//
//
////                row.getJoin(UserField.Company).apply(tblCompany -> {
////                    model.companyId = tblCompany.get(CompanyField.ID);
////                });
//
//
//
//            });
//
//        }

    }

    private static void schemaTest() {

        System.out.print(UserField.Id.getResultType());

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
//        entitySchema.addEntity(UserPrivateMessage.class, userPrivateMessage -> userPrivateMessage
//                .addField(UserPrivateMessageField.TIMESTAMP)
//                .importFrom(UserPrivateMessageField.class));
//        entitySchema.addEntity(User.class, user -> user.importFrom(UserField.class));

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
        executionInput = ExecutionInput.newExecutionInput().query("query{ listAllUsers(filter: {isNameAlan: true, id: 5, id_in: [1,2,3], NOT: { id: 99, id_in: [991, 992] }, OR: [ {id: 4 }, {id: 5, id_in: [5, 1]}]}) {results{displayName}}}").build();
        ExecutionResult executionResult = graphQL.execute(executionInput);

        System.out.print(executionResult);



    }

    private QueryResultList listUsers(RootFetchSet<User> fetches) {
        return null;
    }

    private static void selectAllMessagesForX(EntityManager entityManager) {
        final RootFetchSet<UserPrivateMessage> fetchSet = QueryProvider.newQuery(UserPrivateMessage.class);
        fetchSet.select(UserPrivateMessageField.ID);
        fetchSet.select(UserPrivateMessageField.MESSAGE);
        fetchSet.select(UserPrivateMessageField.TIMESTAMP);
        fetchSet.join(UserPrivateMessageField.SENDER, x -> x.join(UserField.Company, y -> y.select(CompanyField.ID)));
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER, x -> x.join(UserField.Company, y -> y.select(CompanyField.ID)));
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER, x -> x.join(UserField.Company, y -> y.select(CompanyField.ID)));
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.join(UserPrivateMessageField.SENDER, x -> x.join(UserField.Company, y -> y.select(CompanyField.ID)));
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).join(UserField.COMPANY).select(CompanyField.ID);
        fetchSet.orderBy(UserPrivateMessageField.TIMESTAMP, SortDirection.ASC);
//        fetchSet.join(UserPrivateMessageField.RECIPIENT).orderBy(UserField.ID, SortDirection.ASC);
//        fetchSet.join(UserPrivateMessageField.RECIPIENT).orderBy(UserSortKey.OPWNER_ID, SortDirection.ASC);
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

//        PrettyPrint(fetchSet.execute(entityManager, UserPrivateMessageField.ALL_PRIVATE_MESSAGES).getResults());

        System.out.print("YY");

    }

//    private static void PrettyPrint(List<QueryResultItem> results) {
//        if (results.isEmpty()) {
//            return;
//        }
//        StringBuffer x;
//        final List<Map<String, Object>> allRows = new ArrayList<>();
//        for (QueryResultItem result: results) {
//            allRows.add(result.getValues());
//        }
//        final List<String> columnNames = new ArrayList<>(allRows.get(0).keySet());
//        final Map<String, Integer> columnWidths = new HashMap<>();
//        // Initialise with column widths
//        for (String columnName: columnNames) {
//            columnWidths.put(columnName, columnName.length());
//        }
//        // Now loop through all rows ...
//        for (Map<String, Object> row: allRows) {
//            for (String columnName: columnNames) {
//                columnWidths.put(columnName, Math.max(columnWidths.get(columnName), String.valueOf(row.get(columnName)).length()));
//            }
//        }
//        for (String columnName: columnNames) {
//            System.out.print(String.format("| %s ", columnName));
//            final int distanceToPad = columnWidths.get(columnName) - columnName.length();
//            if (distanceToPad > 0) {
//                System.out.print(new String(new char[distanceToPad]).replace("\0", " "));
//            }
//        }
//        System.out.println("|");
//        for (String columnName: columnNames) {
//            final int distanceToPad = columnWidths.get(columnName) - columnName.length();
//            System.out.print(new String(new char[columnName.length() + distanceToPad + 4]).replace("\0", "-"));
//        }
//        System.out.println();
//        for (Map<String, Object> row: allRows) {
//            for (String columnName: columnNames) {
//                final String value = String.valueOf(row.get(columnName));
//                System.out.print(String.format("| %s ", value));
//                final int distanceToPad = columnWidths.get(columnName) - value.length();
//                if (distanceToPad > 0) {
//                    System.out.print(new String(new char[distanceToPad]).replace("\0", " "));
//                }
//            }
//            System.out.println("|");
//        }
//    }


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

