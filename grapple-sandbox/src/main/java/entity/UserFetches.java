/*
package entity;

import org.grapple.query.*;

import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.function.Function;

public class UserFetches {

    public static final AttributeSelection<User, String> ID = AttributeSelection.from(User_.id);

    public static final AttributeSelection<User, Instant> DATE_CREATED = AttributeSelection.from(User_.dateCreated);

    public static final AttributeSelection<User, Boolean> IS_ADMIN = AttributeSelection.from(User_.admin);

    public static final AttributeSelection<User, String> USERNAME = AttributeSelection.from(User_.username);

    public static final AttributeSelection<User, String> LABEL = AttributeSelection.from(User_.label);

    public static final EntityJoin<User, Client> CLIENT_1 = AttributeJoin.from(User_.client1);

    public static final EntityJoin<User, Client> CLIENT_2 = AttributeJoin.from(User_.client2);

    public static final EntityJoin<User, Client> CLIENT_3 = AttributeJoin.from(User_.client3);

    public static final AttributeSelection<Client, String> CLIENT_ID = AttributeSelection.from(Client_.id);

    public static final AttributeSelection<Client, String> CLIENT_LABEL = AttributeSelection.from(Client_.label);

    public static final EntityJoin<Client, User> CLIENT_OWNER = AttributeJoin.from(Client_.owner);

    public static final Function<String, EntityExpression<User, String>> USER_EXP_1 = x -> new EntityExpression<>("zebr1" + x,  String.class, true, (ctx, builder) -> builder.literal(x));

    public static final Function<String, EntityExpression<User, String>> USER_EXP_2 = x -> new EntityExpression<>("zebr2" + x,  String.class, false, (ctx, builder) -> builder.literal(x));

    public static final EntityExpression<User, String> CLIENT_XX = new EntityExpression<>("conat", String.class, true, (ctx, builder) ->
    {
        return builder.concat(builder.concat(ctx.get(LABEL), ctx.get(LABEL)), ctx.get(LABEL));
    });

    public static final QueryFilter<User> USERID_1 = new QueryFilter<User>() {

        @Override
        public Predicate apply(EntityContext<User> ctx, QueryBuilder builder) {
            return builder.equal(ctx.get(User_.id), 1);
        }
    };

    public static QueryFilter<User> filterByClientLabel(String label1, String label2, String label3) {
        return new QueryFilter<User>() {

            @Override
            public Predicate apply(EntityContext<User> ctx, QueryBuilder builder) {
                return builder.or(
                        builder.equal(ctx.join(CLIENT_1).get(Client_.label), label1),
                        builder.equal(ctx.join(CLIENT_1).get(Client_.label), label2),
                        builder.equal(ctx.join(CLIENT_1).get(Client_.label), label3));
            }
        };
    }

    public static QueryFilter<User> filterByClientLabel2(String label1, String label2, String label3) {
        return new QueryFilter<User>() {

            @Override
            public Predicate apply(EntityContext<User> ctx, QueryBuilder builder) {
                return builder.or(
                        builder.equal(ctx.join(CLIENT_2).get(CLIENT_LABEL), label1),
                        builder.equal(ctx.join(CLIENT_2).get(CLIENT_LABEL), label2),
                        builder.equal(ctx.join(CLIENT_2).get(CLIENT_LABEL), label3));
            }
        };
    }
}
*/