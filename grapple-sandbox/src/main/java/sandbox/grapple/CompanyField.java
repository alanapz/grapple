package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.attributeJoin;
import static org.grapple.query.EntityFieldBuilder.literalField;

import org.grapple.query.EntityJoin;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.Company_;
import sandbox.grapple.entity.User;

@QueryDefinitions
public class CompanyField {

    public static final QueryField<Company, Integer> ID = attributeField(Company_.id);

    public static final QueryField<Company, Long> IdAsLong = literalField(fieldBuilder -> fieldBuilder
            .name("idAsLongL")
            .resultType(Long.class));

    public static final QueryField<Company, Long> IdAsPrimitiveLong = literalField(fieldBuilder -> fieldBuilder
            .name("idAsLongP")
            .resultType(long.class)
            .value(123L));

    public static final QueryField<Company, String> Name = attributeField(Company_.displayName);

    public static final EntityJoin<Company, User> OwnerNotNull = attributeJoin(Company_.owner, joinBuilder -> joinBuilder
            .name("owner")
            .nullAllowed(true));

    public static final EntityJoin<Company, User> OwnerNullAllowed = attributeJoin(Company_.owner, joinBuilder -> joinBuilder
            .name("ownerNullAllowed")
            .nullAllowed(true));
}
