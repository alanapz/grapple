
package org.grapple2.sample;

import static org.grapple.query.EntityRootBuilder.entityRoot;

import org.grapple.query.Filters;
import org.grapple.query.RootFetchSet;
import org.grapple.query.impl.QueryProvider;
import org.grapple2.schema.SchemaBuilder;
import org.grapple2.schema.UnmanagedEntityBuilder;

public class Test {

    public static void main(String[] args)
    {
        final RootFetchSet<UserPrivateMessage> fetchSet = QueryProvider.newQuery(UserPrivateMessage.class);
        fetchSet.join(x -> x.getUserById(123), user -> {
            user.select(u -> u.id());
            user.select(u -> u.name());
        });
        fetchSet.join(x -> x.listUsers(0, 0), user -> {
            user.filter(Filters.isEqual(u -> u.id(), userId));
            user.filter()
            user.select(u -> u.id());
            user.select(u -> u.name());
        });

        return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult().orElse(null);

        // fetchSet.join(UserPrivateMessageField.RECIPIENT);
        // fetchSet.join(UserPrivateMessageField.RECIPIENT).add(UserField.ID);
        // fetchSet.filter(UserFetches.USERID_1);
        // fetchSet.filter(UserFetches.filterByClientLabel("aaa0", "bbb0", "ccc0"));

        fetchSet.select(UserPrivateMessageField.ID);
        fetchSet.select(UserPrivateMessageField.MESSAGE);
        fetchSet.select(UserPrivateMessageField.TIMESTAMP);


        SchemaBuilder b = new SchemaBuilder();
        b.addUnmanagedEntity(EnvironmentVariableQuery.class, x ->
        {
            UnmanagedEntityBuilder builder;
            builder.configureField(EnvironmentVariableQuery_.getEnvironmentVariables);

        });
        b.addUnmanagedEntity("EnvironmentVariable2");

    }

}
