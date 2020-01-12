package app;

import static org.grapple.query.EntityRootBuilder.entityRoot;

import java.util.Set;
import javax.persistence.EntityManager;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.Filters;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntityQueryType;
import sandbox.grapple.UserField;
import sandbox.grapple.entity.User;

public class UserService {

    private EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GrappleQuery
    public QueryResultList<User> listUsers(RootFetchSet<User> fetches, Set<Integer> companyIds) {
        return fetches.execute(entityManager, entityRoot(User.class));
    }

    @GrappleQuery(name = "getCurrentUser", type = EntityQueryType.SCALAR_NON_NULL)
    public QueryResultList<User> listUsersX(RootFetchSet<User> fetches, Set<Integer> userIds) {
        return fetches.execute(entityManager, entityRoot(User.class));
    }

    @GrappleQuery(name = "getOptionalCurrentUser", type = EntityQueryType.SCALAR_NULL_ALLOWED)
    public QueryResultList<User> listUsersY(RootFetchSet<User> fetches, int userId) {
        fetches.filter(Filters.isEqual(UserField.Id, userId));
        return fetches.execute(entityManager, entityRoot(User.class));
    }

}
