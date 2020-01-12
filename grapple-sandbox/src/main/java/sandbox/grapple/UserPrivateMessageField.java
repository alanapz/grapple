package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.literalField;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityRoot;
import org.grapple.query.EntityRootBuilder;
import org.grapple.query.Filters;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;
import sandbox.grapple.entity.UserPrivateMessage_;

public class UserPrivateMessageField {

    public static final QueryField<UserPrivateMessage, Integer> ID = attributeField(UserPrivateMessage_.id);

    public static final QueryField<UserPrivateMessage, Integer> PRIORITY = attributeField(UserPrivateMessage_.priority);

    public static final QueryField<UserPrivateMessage, Instant> TIMESTAMP = attributeField(UserPrivateMessage_.timestamp);

    public static final QueryField<UserPrivateMessage, Double> LITERAL_DOUBLE_P = literalField("literalDoubleP", Double.class, 1D);

    public static final QueryField<UserPrivateMessage, Double> LITERAL_DOUBLE_O = literalField("literalDoubleO", double.class, 1D);

    public static final QueryField<UserPrivateMessage, YearMonth> YEARMONTH = literalField("yearMonth", YearMonth.class, YearMonth.now());

    // public static final AttributeSelection<UserPrivateMessage, String> TIMESTAMP_AS_LOCAL = AttributeSelection.from(UserPrivateMessage_.displayName);

    public static final EntityJoin<UserPrivateMessage, User> SENDER = EntityFieldBuilder.attributeJoin(UserPrivateMessage_.sender);

    public static final EntityJoin<UserPrivateMessage, User> RECIPIENT = EntityFieldBuilder.attributeJoin(UserPrivateMessage_.recipient);

    // public static final AttributeSelection<UserPrivateMessage, User> RECIPIENT_X = AttributeSelection.from(UserPrivateMessage_.recipient);

    public static final QueryField<UserPrivateMessage, String> MESSAGE = attributeField(UserPrivateMessage_.message);

    // public static final QueryableField<UserPrivateMessage, String> MESSAGE2 = EntityFieldBuilder.expression("message", EntityResultType.of(null, true), null);

    public static final EntityRoot<UserPrivateMessage> ALL_PRIVATE_MESSAGES = EntityRootBuilder.from(UserPrivateMessage.class);

    public static final EntityRoot<UserPrivateMessage> UNREAD_PRIVATE_MESSAGES = EntityRootBuilder.from(UserPrivateMessage.class, Filters.isTrue(UserPrivateMessage_.unread));

    public static final QueryField<UserPrivateMessage, Type> ENUM_TYPE = literalField("enumType", Type.class, Type.TYPE_1);

    public static final QueryField<UserPrivateMessage, Type> ENUM_TYPE_2 = literalField("enumType2", Type.class, Type.TYPE_2);

    public enum Type {
        TYPE_1,
        TYPE_2
    }

    public static final List<QueryField<UserPrivateMessage, String>> MESSAGE_1 = Arrays.asList(
            attributeField(UserPrivateMessage_.message, fieldBuilder -> fieldBuilder.name("m1")),
            attributeField(UserPrivateMessage_.message, fieldBuilder -> fieldBuilder.name("m2")),
            attributeField(UserPrivateMessage_.message, fieldBuilder -> fieldBuilder.name("m3")));

    public static final Map<Object, List<QueryField<UserPrivateMessage, String>>> MESSAGE_12 = new HashMap<>();

    static {
        MESSAGE_12.put(123, Arrays.asList(EntityFieldBuilder.attributeField(UserPrivateMessage_.message, fieldBuilder -> fieldBuilder.name("xxx1"))));
    }

}
