package sandbox.grapple;

import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityRoot;
import org.grapple.query.EntityRootBuilder;
import org.grapple.query.Filters;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;
import sandbox.grapple.entity.UserPrivateMessage_;

import java.time.Instant;

public class UserPrivateMessageField {

    public static final QueryField<UserPrivateMessage, Integer> ID = EntityFieldBuilder.from(UserPrivateMessage_.id);

    public static final QueryField<UserPrivateMessage, Integer> PRIORITY = EntityFieldBuilder.from(UserPrivateMessage_.priority);

    public static final QueryField<UserPrivateMessage, Instant> TIMESTAMP = EntityFieldBuilder.from(UserPrivateMessage_.timestamp);

    // public static final AttributeSelection<UserPrivateMessage, String> TIMESTAMP_AS_LOCAL = AttributeSelection.from(UserPrivateMessage_.displayName);

    public static final EntityJoin<UserPrivateMessage, User> SENDER = EntityFieldBuilder.join(UserPrivateMessage_.sender);

    public static final EntityJoin<UserPrivateMessage, User> RECIPIENT = EntityFieldBuilder.join(UserPrivateMessage_.recipient);

    // public static final AttributeSelection<UserPrivateMessage, User> RECIPIENT_X = AttributeSelection.from(UserPrivateMessage_.recipient);

    public static final QueryField<UserPrivateMessage, String> MESSAGE = EntityFieldBuilder.from(UserPrivateMessage_.message);

    // public static final QueryableField<UserPrivateMessage, String> MESSAGE2 = EntityFieldBuilder.expression("message", EntityResultType.of(null, true), null);

    public static final EntityRoot<UserPrivateMessage> ALL_PRIVATE_MESSAGES = EntityRootBuilder.from(UserPrivateMessage.class);

    public static final EntityRoot<UserPrivateMessage> UNREAD_PRIVATE_MESSAGES = EntityRootBuilder.from(UserPrivateMessage.class, Filters.isTrue(UserPrivateMessage_.unread));
}
