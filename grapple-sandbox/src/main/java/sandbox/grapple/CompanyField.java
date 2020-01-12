package sandbox.grapple;

import static org.grapple.query.EntityFieldBuilder.attributeField;
import static org.grapple.query.EntityFieldBuilder.attributeJoin;

import org.grapple.query.EntityJoin;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.Company_;
import sandbox.grapple.entity.User;

public class CompanyField {

    public static final QueryField<Company, Integer> ID = attributeField(Company_.id);

    public static final QueryField<Company, String> NAME = attributeField(Company_.displayName);

    public static final EntityJoin<Company, User> OWNER = attributeJoin(Company_.owner, joinBuilder -> joinBuilder.nullable(false));

}
