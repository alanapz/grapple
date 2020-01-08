package sandbox.grapple;

import org.grapple.query.EntityFieldBuilder;
import org.grapple.query.EntityJoin;
import org.grapple.query.QueryField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.Company_;
import sandbox.grapple.entity.User;

public class CompanyField {

    public static final QueryField<Company, Integer> ID = EntityFieldBuilder.from(Company_.id);

    public static final QueryField<Company, String> NAME = EntityFieldBuilder.from(Company_.displayName);

    public static final EntityJoin<Company, User> OWNER = EntityFieldBuilder.join(Company_.owner, false);

}
