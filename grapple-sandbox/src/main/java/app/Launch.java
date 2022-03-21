package app;

import static graphql.ExecutionInput.newExecutionInput;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import sandbox.grapple.CompanyField;
import sandbox.grapple.UserField;
import sandbox.grapple.UserPrivateMessageField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;

import org.grapple.query.EntityFilter;
import org.grapple.query.Filters;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;
import org.grapple.query.impl.QueryProviderFactory;
import org.grapple.reflect.GenericLiteral;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaListener;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.FieldFilterDefinition;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.grapple.schema.instrumentation.DebugInstrumentationCallback;

public class Launch {

    static class Temp {

        public int userId;

        public int companyId;

        public Cox cox;
    }

    static class Cox {

        public int companyId;

    }



    private static EntityManagerFactory entityManagerFactory;

    public static void runTest(GrappleTestCallback testCallback) throws Exception {
        requireNonNull(testCallback, "testCallback");
        testCallback.execute(getEntityManager());
    }

    public static EntitySchemaResult buildGraphQL(EntitySchema entitySchema) {

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

        return entitySchema.generate();
    }

    public static void runQuery(GraphQL graphQL, String query) {
        requireNonNull(graphQL, "graphQL");
        requireNonNull(query, "query");
        final ExecutionInput executionInput = newExecutionInput().query(query).build();
        System.out.println(format("Query: %s", executionInput.getQuery()));
        final ExecutionResult executionResult = graphQL.execute(executionInput);
        System.out.println(format("*** Request : %s", executionInput.getQuery()));
        System.out.println(format("*** Response: %s", executionResult));
        if (!executionResult.getErrors().isEmpty()) {
            throw new RuntimeException(executionResult.getErrors().toString());
        }
        System.out.println(format("Response: %s", (Map<?, ?>) executionResult.getData()));
    }

    private static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            Launch.entityManagerFactory = Persistence.createEntityManagerFactory("grapple-sandbox");
        }
        return entityManagerFactory.createEntityManager();
    }

    private static DateTimeFormatter w(DateTimeFormatter dateTimeFormatter)
    {
        return dateTimeFormatter.withZone(ZoneId.systemDefault());
        //return dateTimeFormatter.withZone(ZoneId.of("UTC"));
    }

    public static void main(String[] args) throws Exception {


        BasicTest basicTest = new BasicTest();
        basicTest.go();

        System.exit(0);

/*

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        System.out.println(formatter.format(Instant.now()));
        System.out.println(Instant.from(formatter.parse(formatter.format(Instant.now()))));
        System.out.println(Instant.from(formatter.parse("2020-05-01T14:30:36Z")));

        System.exit(0);


        Instant i = Instant.now();
        System.out.println(i);
        ZonedDateTime localDateTime = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
        System.out.println(localDateTime);
        System.out.println(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
        System.out.println(DateTimeFormatter.RFC_1123_DATE_TIME.format(localDateTime));

        System.exit(0);
*/
//        System.getProperties().put("entityManager", entityManager);
//        System.getProperties().put("usersRoot", entityRoot(User.class));


//        System.exit(0);


//        parameterTest(entityManager);

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback(){}).importDefinitions(CompanyField.class);
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback(){}).importDefinitions(UserField.class);
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


//        entitySchema.importQueries(new UserService(entityManager), new EntityQueryScannerCallback() {
//
//            public void entityNotFound(Method method, Class<?> entityClass) {
//                // Called when a query couldn't be loaded as matching entity not found
//            }
//
//            public boolean acceptQuery(Method method) {
//                // Called to accept or not the given method
//                return true;
//            }
//
//            public void configureQuery(Method method, EntityQueryDefinition<?> queryDefinition) {
//
//            }
//        });

        entitySchema.addEntity(User.class).addFilterItem(new GenericLiteral<List<Set<Integer>>>() {}, filter -> {
            filter.setName("apzTest");
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

        entitySchema.addEntity(Company.class);

//        entitySchema.getEntity(User.class).addQuery(queryBuilder ->
//        {
//            queryBuilder.setName("listAllUsers");
//            queryBuilder.setQueryType(EntityQueryType.LIST);
//            queryBuilder.setQueryResolver(QueryResolverFactory.defaultQueryResolver(emf, entityRoot(User.class)));
//            queryBuilder.addParameter(new GenericLiteral<Set<Integer>>() {}, parameterBuilder -> {
//                parameterBuilder.setName("companyIds");
//            });
//            queryBuilder.addParameter(new GenericLiteral<Set<Stack<Integer>>>() {}, parameterBuilder -> {
//                parameterBuilder.setName("companyIds2");
//                parameterBuilder.setRequired(true);
//            });
//        });
//
//        entitySchema.getEntity(User.class).addQuery(queryBuilder -> {
//            queryBuilder.setQueryName("getUserById");
//            queryBuilder.setQueryType(EntityQueryType.SCALAR_NULL_ALLOWED);
//            queryBuilder.setQueryResolver(QueryResolverFactory.defaultQueryResolver(emf, entityRoot(User.class), (fetchSet, params) -> {
//                fetchSet.filter(Filters.isEqual(UserField.Id, (int) params.get("userId")));
//            } ));
//            queryBuilder.addParameter(classLiteral(Integer.class), parameterBuilder -> {
//                parameterBuilder.setName("userId");
//                parameterBuilder.setRequired(true);
//            });
//        });

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

        System.exit(0);

        if (true) {
            return;
        }






        if (true) {
            return;
        }


//        selectAllMessagesForX(entityManager);
        if (true) {
            return;
        }


//or

//        Map<String, Object> configOverrides = new HashMap<String, Object>();
        //configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");
        //EntityManagerFactory programmaticEmf =
//                Persistence.createEntityManagerFactory("manager1", configOverrides);
    }

    private QueryResultList listUsers(RootFetchSet<User> fetches) {
        return null;
    }

    private static void selectAllMessagesForX(EntityManager entityManager) {
        final RootFetchSet<UserPrivateMessage> fetchSet = QueryProviderFactory.buildQueryProvider(c -> c.setEntityManager(() -> entityManager)).newQuery(UserPrivateMessage.class);
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
                .includeIntrospectionTypes(false)
                .includeDirectives(false)
                .includeSchemaDefinition(true));
        System.out.println(printer.print(schema));

        GraphQL graphQL = GraphQL.newGraphQL(schema)
                .build();

        ExecutionInput executionInput = newExecutionInput().query("query { listAllUsers { results { username, client1(filter: {label: \"123\"}){id}, client2{id}, client3{id} } } }")
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        Object data = executionResult.getData();
        List<GraphQLError> errors = executionResult.getErrors();

        System.out.println(data);
        System.out.println(errors);


        // entityManager.getCriteriaBuilder().createTupleQuery();
    }
}

