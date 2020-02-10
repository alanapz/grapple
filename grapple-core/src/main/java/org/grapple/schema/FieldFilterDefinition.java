package org.grapple.schema;

import java.util.Map;
import org.grapple.core.Chainable;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.impl.SimpleFieldFilterItem;

public interface FieldFilterDefinition<T> extends EntitySchemaElement, Chainable<FieldFilterDefinition<T>> {

    TypeLiteral<T> getFieldType();

    Map<String, SimpleFieldFilterItem<T>> getItems();

    FieldFilterDefinition<T> addItem(SimpleFieldFilterItem<T> item);

}
