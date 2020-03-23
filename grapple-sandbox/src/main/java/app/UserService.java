package app;

import static java.util.Objects.requireNonNull;
import static org.grapple.query.EntityRootBuilder.entityRoot;

import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import org.grapple.invoker.GrappleParameter;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.Filters;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
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

    @GrappleQuery
    public Optional<QueryResultRow<User>> alwaysThrowException(RootFetchSet<User> fetches) {
        throw new UserNotFoundException("Custom exception");
    }

    @GrappleQuery(value = "getCurrentUser")
    public QueryResultList<User> listUsersX(RootFetchSet<User> fetches, Set<Integer> userIds) {
        return fetches.execute(entityManager, entityRoot(User.class));
    }

    @GrappleQuery(value = "getOptionalCurrentUser")
    public QueryResultRow<User> listUsersY(RootFetchSet<User> fetches, int userId) {
        fetches.filter(Filters.isEqual(UserField.Id, userId));
        return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult().orElse(null);
    }

    @GrappleQuery
    public Optional<QueryResultRow<User>> getUserById(RootFetchSet<User> fetches, @GrappleParameter("id") int userId) {
        requireNonNull(fetches, "fetches");
        if (userId == 0) {
            throw new IllegalArgumentException("userId");
        }
        fetches.filter(Filters.isEqual(UserField.Id, userId));
        return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult();
    }
}
