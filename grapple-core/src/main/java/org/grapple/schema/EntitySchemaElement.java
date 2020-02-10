package org.grapple.schema;

import org.grapple.core.ElementVisibility;

public interface EntitySchemaElement {

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getDeprecationReason();

    void setDeprecationReason(String deprecationReason);

    ElementVisibility getVisibility();

    void setVisibility(ElementVisibility visibility);
}
