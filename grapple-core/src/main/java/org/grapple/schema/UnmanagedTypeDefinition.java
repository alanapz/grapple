package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.query.EntityField;
import org.grapple.query.QueryField;

public interface UnmanagedTypeDefinition extends Chainable<UnmanagedTypeDefinition> {

    String getTypeName();

    void setFieldName(String fieldName);

    String getDescription();

    void setDescription(String description);

    String getDeprecationReason();

    void setDeprecationReason(String deprecationReason);

}
