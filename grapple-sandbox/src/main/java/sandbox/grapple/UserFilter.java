package sandbox.grapple;

import static org.jooq.lambda.Seq.seq;

import java.util.List;
import java.util.Set;
import org.grapple.query.EntityFilter;
import org.grapple.query.QueryDefinitions;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.User_;

@QueryDefinitions
public class UserFilter {

    private UserFilter() {

    }

    public static EntityFilter<User> filterByUserName(String username) {
        return (ctx, queryBuilder) ->  queryBuilder.equal(ctx.get(User_.displayName), username);
    }

    public static EntityFilter<User> showOnlyAlan() {
        return (ctx, queryBuilder) ->  queryBuilder.equal(ctx.get(User_.displayName), "alan");
    }

    public static EntityFilter<User> alwaysNull() {
        return null;
    }

    public static EntityFilter<User> filterByUserIdBackwards(List<Set<Integer>> userIds) {
        return (ctx, queryBuilder) ->  queryBuilder.or(seq(userIds).map(x -> queryBuilder.in(ctx.get(User_.id), x)).toList());
    }

}
