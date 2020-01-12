package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.attributeJoin;
import static org.grapple.query.EntityFieldBuilder.literalField;
import static org.grapple.query.EntityRootBuilder.entityRoot;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityRoot;
import org.grapple.query.Filters;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.User;
import sandbox.grapple.entity.UserPrivateMessage;
import sandbox.grapple.entity.UserPrivateMessage_;

@QueryDefinitions
public class UserPrivateMessageField {

    public static final QueryField<UserPrivateMessage, Integer> ID = attributeField(UserPrivateMessage_.id);

    public static final QueryField<UserPrivateMessage, Integer> PRIORITY = attributeField(UserPrivateMessage_.priority);

    public static final QueryField<UserPrivateMessage, Instant> TIMESTAMP = attributeField(UserPrivateMessage_.timestamp);

//    public static final EntityField<UserPrivateMessage, Double> LITERAL_DOUBLE_P = literalField("literalDoubleP", Double.class, 1D);

    // public static final QueryField<UserPrivateMessage, List<Double>> LITERAL_DOUBLE_LIST = literalField("literalDoubleList", (Class<List<Double>>) (Object) List.class, Arrays.asList(1D, 2D, 3D));

//    public static final EntityField<UserPrivateMessage, Double[]> LITERAL_DOUBLE_ARRAY = literalField("literalDoubleArray1", Double[].class, new Double[] { 1D, 2D, 3D });
//
//    public static final EntityField<UserPrivateMessage, Double[][][]> LITERAL_DOUBLE_ARRAY_3 = literalField("literalDoubleArray2", Double[][][].class, new Double[][][] { });
//
//    public static final EntityField<UserPrivateMessage, List<List<Double[][]>[]>> LITERAL_DOUBLE_ARRAY_4 = literalField(fieldBuilder -> fieldBuilder
//            .name("literalDoubleList")
//            .resultType(new GenericLiteral<List<List<Double[][]>[]>>(){}));
//
//    public static final EntityField<UserPrivateMessage, Double> LITERAL_DOUBLE_O = literalField("literalDoubleO", double.class, 1D);
//
//    public static final EntityField<UserPrivateMessage, YearMonth> YEARMONTH = literalField("yearMonth", YearMonth.class, YearMonth.now());

    // public static final AttributeSelection<UserPrivateMessage, String> TIMESTAMP_AS_LOCAL = AttributeSelection.from(UserPrivateMessage_.displayName);

    public static final EntityJoin<UserPrivateMessage, User> SENDER = EntityFieldBuilder.attributeJoin(UserPrivateMessage_.sender);

    public static final EntityJoin<UserPrivateMessage, User> RECIPIENT = EntityFieldBuilder.attributeJoin(UserPrivateMessage_.recipient);

    // public static final AttributeSelection<UserPrivateMessage, User> RECIPIENT_X = AttributeSelection.from(UserPrivateMessage_.recipient);

    public static final QueryField<UserPrivateMessage, String> MESSAGE = attributeField(UserPrivateMessage_.message);

    // public static final QueryableField<UserPrivateMessage, String> MESSAGE2 = EntityFieldBuilder.expression("message", EntityResultType.of(null, true), null);

    public static final EntityRoot<UserPrivateMessage> ALL_PRIVATE_MESSAGES = entityRoot(UserPrivateMessage.class);

    public static final EntityRoot<UserPrivateMessage> UNREAD_PRIVATE_MESSAGES = entityRoot(UserPrivateMessage.class, Filters.isTrue(UserPrivateMessage_.unread));

    public static final QueryField<UserPrivateMessage, PMType> ENUM_TYPE = literalField(fieldBuilder -> fieldBuilder
            .name("messageType")
            .resultType(PMType.class)
            .value(PMType.TYPE_1));

    public static final QueryField<UserPrivateMessage, PMType> ENUM_TYPE2 = literalField(fieldBuilder -> fieldBuilder
            .name("messageType2")
            .resultType(PMType.class)
            .value(null));

    public static final EntityJoin<UserPrivateMessage, User> SENDER_2 = attributeJoin(UserPrivateMessage_.sender2, fieldBuilder -> fieldBuilder.nullAllowed(true));

    public enum PMType {
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
