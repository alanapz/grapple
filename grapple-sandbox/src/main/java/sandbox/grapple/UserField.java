package sandbox.grapple;

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

    public static final QueryField<User, Integer> ID = EntityFieldBuilder.from(User_.id);

    public static final QueryField<User, String> DISPLAY_NAME = EntityFieldBuilder.from(User_.displayName);

    public static final QueryField<User, String> OLDDISPLAY_NAME = EntityFieldBuilder.attribute(a -> a.name("dep").attribute(User_.displayName).deprecated());

    public static final EntityJoin<User, Company> COMPANY = EntityFieldBuilder.join(User_.company);

    public static final QueryField<User, String> DISPLAY_NAME_2 = EntityFieldBuilder.from("displayName2", EntityResultType.ofNullable(String.class), (ctx, builder) -> {
        Subquery<String> q2 = ctx.getQuery().subquery(String.class);
        Root<User2> user2 = q2.from(User2.class);
        q2.where(builder.equal(user2.get(User2_.id), ctx.get(User_.id)));
        q2.select(user2.get(User2_.displayName));
        return q2;
    });

    public static final QueryField<User, Boolean> IS_NAME_ALAN = EntityFieldBuilder.from("isNameAlan", EntityResultTypes.BOOLEAN, (ctx, builder) -> {
        return builder.equal(ctx.get(DISPLAY_NAME), builder.literal("alan"));
    });

    public static final QueryField<User, Boolean> IS_GREATESST = EntityFieldBuilder.expression(exp -> exp
            .name("isGreatest")
            .resultType(EntityResultTypes.BOOLEAN)
            .supplier((ctx, builder) -> builder.equal(builder.literal(123), builder.greatest(ctx.get(ID), builder.literal(123)))));
}
