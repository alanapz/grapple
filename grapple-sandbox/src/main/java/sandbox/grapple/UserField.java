package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.attributeJoin;
import static org.grapple.query.EntityFieldBuilder.expressionField;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityResultType;
import org.grapple.query.EntityResultTypes;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.User2;
import sandbox.grapple.entity.User2_;
import sandbox.grapple.entity.User_;

public class UserField {

    public static final QueryField<User, Integer> ID = attributeField(User_.id);

    public static final QueryField<User, String> DISPLAY_NAME = attributeField(User_.displayName);

    public static final QueryField<User, String> OLDDISPLAY_NAME = attributeField(User_.displayName, fieldBuilder -> fieldBuilder.name("dep").deprecated());

    public static final EntityJoin<User, Company> COMPANY = attributeJoin(User_.company);

    public static final QueryField<User, String> DISPLAY_NAME_2 = expressionField(fieldBuilder -> fieldBuilder
            .name("displayName2")
            .resultType(EntityResultType.ofNullable(String.class))
            .expression((ctx, builder) -> {
                Subquery<String> q2 = ctx.getQuery().subquery(String.class);
                Root<User2> user2 = q2.from(User2.class);
                q2.where(builder.equal(user2.get(User2_.id), ctx.get(User_.id)));
                q2.select(user2.get(User2_.displayName));
                return q2;
            }));

    public static final QueryField<User, Boolean> IS_NAME_ALAN = expressionField(fieldBuilder -> fieldBuilder
            .name("isNameAlan")
            .resultType(EntityResultTypes.BOOLEAN)
            .expression((ctx, queryBuilder) -> queryBuilder.equal(ctx.get(DISPLAY_NAME), queryBuilder.literal("alan"))));

    public static final QueryField<User, Boolean> IS_GREATESST = EntityFieldBuilder.expressionField(fieldBuilder -> fieldBuilder
            .name("isGreatest")
            .resultType(EntityResultTypes.BOOLEAN)
            .expression((ctx, builder) -> builder.equal(builder.literal(123), builder.greatest(ctx.get(ID), builder.literal(123)))));
}
