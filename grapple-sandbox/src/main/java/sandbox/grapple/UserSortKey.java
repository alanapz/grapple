package sandbox.grapple;

import javax.persistence.criteria.Expression;
import org.grapple.query.EntityContext;
import org.grapple.utils.EntitySortKey;
import sandbox.grapple.entity.User;

public enum UserSortKey implements EntitySortKey<User> {

    USER_ID(ctx -> ctx.get(UserField.ID)),
    OPWNER_ID(ctx -> ctx.join(UserField.COMPANY).get(CompanyField.NAME)),
    COMPANY_ID(ctx -> ctx.join(UserField.COMPANY).get(CompanyField.ID));

    private final EntitySortKey<User> sortKey;

    UserSortKey(EntitySortKey<User> sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public Expression<?> getPath(EntityContext<User> ctx) {
        return sortKey.getPath(ctx);
    }
}
