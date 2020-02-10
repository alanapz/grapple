package org.grapple.junit;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.grapple.reflect.ClassLiteral.classLiteral;
import static org.grapple.utils.Utils.toSet;

import java.util.Objects;
import javax.persistence.EntityManager;
import app.Launch;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.jooq.lambda.Seq;
import org.junit.jupiter.api.Test;

public class CustomQuerySchemaTests extends SchemaTestSupport {

    @Test
    public void testCustomResolver() {

        final EntityManager entityManager = getEntityManager();

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();

        entitySchema.addUnmanagedType(classLiteral(ThreadDetails.class), consumer -> {
            consumer.setTypeBuilder(ctx -> {
                final GraphQLObjectType.Builder builder = newObject();
                builder.name("Thread");
                builder.field(newFieldDefinition().name("threadId").type(Scalars.GraphQLLong).build());
                builder.field(newFieldDefinition().name("name").type(Scalars.GraphQLString).build());
                builder.field(newFieldDefinition().name("className").type(Scalars.GraphQLString).build());
                return builder.build();
            });
        });

        entitySchema.addUnmanagedQuery("listThread", consumer -> {
            consumer.setQueryBuilder(ctx -> {
                final GraphQLFieldDefinition.Builder builder = newFieldDefinition();
                builder.name("listAllThreads");
                builder.type(nonNull(GraphQLList.list(nonNull(ctx.buildTypeFor(ThreadDetails.class)))));
                return builder.build();
            });
            consumer.setDataFetcher(dataFetchingEnvironment -> {
                Thread[] buffer = new Thread[Thread.activeCount()];
                Thread.enumerate(buffer);
                return Seq.of(buffer).filter(Objects::nonNull).map(ThreadDetails::new).toList();
            });
        });

        System.out.println(entitySchema);
        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();

        Launch.runQuery(graphQL, "query{ listAllThreads { threadId, name, className }}");


    }

    public static class ThreadDetails {

        public final long threadId;

        public final String name;

        public final String className;

        private ThreadDetails(Thread thread) {
            this.threadId = thread.getId();
            this.name = thread.getName();
            this.className = thread.getClass().getName();
        }
    }
}
