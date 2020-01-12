package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.attributeJoin;
import static org.grapple.query.EntityFieldBuilder.expressionField;
import static org.grapple.query.EntityFieldBuilder.expressionJoin;
import static org.grapple.query.EntityResultType.nonNull;
import static org.grapple.query.EntityResultType.nullAllowed;

import java.util.UUID;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.User2;
import sandbox.grapple.entity.User2_;
import sandbox.grapple.entity.User_;

@QueryDefinitions
public class UserField {

    public static final QueryField<User, Integer> Id = attributeField(User_.id);

    public static final QueryField<User, String> DisplayName = attributeField(User_.displayName);

    public static final QueryField<User, String> DeprecatedDisplayName = attributeField(User_.displayName, fieldBuilder -> fieldBuilder.name("dep").deprecated(true));

    public static final EntityJoin<User, Company> Company = attributeJoin(User_.company);

    public static final EntityJoin<User, Company> Company2 = expressionJoin(fieldBuilder -> fieldBuilder
        .name("company2")
        .resultType(nullAllowed(sandbox.grapple.entity.Company.class))
        .expression((ctx, builder) -> {
            return ctx.join(User_.company);
        }));

    public static final QueryField<User, String> DISPLAY_NAME2 = expressionField(fieldBuilder -> fieldBuilder
            .name("displayName2")
            .resultType(nullAllowed(String.class))
            .expression((ctx, queryBuilder) -> {

                Subquery<String> q2 = ctx.getQuery().subquery(String.class);
                Root<User2> user2 = q2.from(User2.class);
                q2.where(queryBuilder.equal(user2.get(User2_.id), ctx.get(User_.id)));
                q2.select(user2.get(User2_.displayName));
                return q2;
            }));

    public static final QueryField<User, Boolean> IS_NAME_ALAN = expressionField(fieldBuilder -> fieldBuilder
            .name("isNameAlan")
            .resultType(nonNull(boolean.class))
            .expression((ctx, queryBuilder) -> queryBuilder.toExpression(queryBuilder.equal(ctx.get(DisplayName), queryBuilder.literal("alan")))));

    public static final QueryField<User, Boolean> IS_GREATESST = EntityFieldBuilder.expressionField(fieldBuilder -> fieldBuilder
            .name("isGreatest")
            .resultType(nonNull(boolean.class))
            .expression((ctx, builder) -> builder.equal(builder.literal(123), builder.greatest(ctx.get(Id), builder.literal(123)))));

    public static final QueryField<User, Boolean> IS_GREATESST_2 = EntityFieldBuilder.expressionField(fieldBuilder -> fieldBuilder
            .name("isGreatest2")
            .resultType(nullAllowed(Boolean.class))
            .expression((ctx, builder) -> builder.equal(builder.literal(123), builder.greatest(ctx.get(Id), builder.literal(123)))));


    public static final EntityField<User, UserDetails> DETAILS = EntityFieldBuilder.selectionField(fieldBuilder -> fieldBuilder
            .name("userDetails")
            .resultType(nullAllowed(UserDetails.class))
            .resolver((ctx, builder) -> tuple -> (UserDetails) null));

    public static final EntityField<User, String> UserGuid = EntityFieldBuilder.selectionField(fieldBuilder -> fieldBuilder
            .name("userGuid")
            .resultType(nullAllowed(String.class))
            .resolver((ctx, builder) -> tuple -> UUID.randomUUID().toString()));

    public static class UserDetails {

    }

}
