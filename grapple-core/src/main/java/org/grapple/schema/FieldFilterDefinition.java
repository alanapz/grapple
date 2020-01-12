package org.grapple.schema;

import java.util.Map;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.impl.SimpleFieldFilterItem;
import org.grapple.utils.Chainable;

public interface FieldFilterDefinition<T> extends Chainable<FieldFilterDefinition<T>> {

    TypeLiteral<T> getFieldType();

    String getEntityName();

    FieldFilterDefinition<T> setEntityName(String entityName);

    String getDescription();

    FieldFilterDefinition<T> setDescription(String description);

    Map<String, SimpleFieldFilterItem<T>> getItems();

    FieldFilterDefinition<T> addItem(SimpleFieldFilterItem<T> item);

}
