package org.grapple.schema.impl;

import static graphql.schema.FieldCoordinates.coordinates;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.visibility.GraphqlFieldVisibility;
import org.grapple.core.ElementVisibility;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.NoDuplicatesSet;

final class SchemaBuilderElementVisibility {

    private final String rootQueryTypeName;

    private final Map<FieldCoordinates, ElementVisibility> fieldVisibility = new NoDuplicatesMap<>();

    SchemaBuilderElementVisibility(String rootQueryTypeName) {
        this.rootQueryTypeName = requireNonNull(rootQueryTypeName, "rootQueryTypeName");
    }

    void setQueryVisibility(String queryName, ElementVisibility visibility) {
        requireNonNull(queryName, "queryName");
        requireNonNull(visibility, "visibility");
        setFieldVisibility(rootQueryTypeName, queryName, visibility);
    }

    void setFieldVisibility(String typeName, String fieldName, ElementVisibility visibility) {
        requireNonNull(typeName, "typeName");
        requireNonNull(fieldName, "fieldName");
        requireNonNull(visibility, "visibility");
        fieldVisibility.put(coordinates(typeName, fieldName), visibility);
    }

    GraphqlFieldVisibility compileVisibility(Set<String> rolesHeld) {
        requireNonNull(rolesHeld, "rolesHeld");
        final Set<FieldCoordinates> blockedFields = new NoDuplicatesSet<>();
        for (Map.Entry<FieldCoordinates, ElementVisibility> entry: fieldVisibility.entrySet()) {
            if (!entry.getValue().isVisible(rolesHeld)) {
                blockedFields.add(entry.getKey());
            }
        }
        return new GraphqlFieldVisibilityImpl(blockedFields);
    }

    private static final class GraphqlFieldVisibilityImpl implements GraphqlFieldVisibility
    {
        private final Set<FieldCoordinates> blockedFields;

        private GraphqlFieldVisibilityImpl(Set<FieldCoordinates> blockedFields) {
            this.blockedFields = unmodifiableSet(new HashSet<>(blockedFields));
        }

        @Override
        public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
            requireNonNull(fieldsContainer, "fieldsContainer");
            final String typeName = fieldsContainer.getName();
            return seq(fieldsContainer.getFieldDefinitions())
                    .filter(field -> !blockedFields.contains(coordinates(typeName, field.getName())))
                    .toList();
        }

        @Override
        public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
            requireNonNull(fieldsContainer, "fieldsContainer");
            requireNonNull(fieldName, "fieldName");
            if (blockedFields.contains(coordinates(fieldsContainer.getName(), fieldName))) {
                return null;
            }
            return fieldsContainer.getFieldDefinition(fieldName);
        }

        @Override
        public List<GraphQLInputObjectField> getFieldDefinitions(GraphQLInputFieldsContainer fieldsContainer) {
            requireNonNull(fieldsContainer, "fieldsContainer");
            final String typeName = fieldsContainer.getName();
            return seq(fieldsContainer.getFieldDefinitions())
                    .filter(field -> !blockedFields.contains(coordinates(typeName, field.getName())))
                    .toList();
        }

        @Override
        public GraphQLInputObjectField getFieldDefinition(GraphQLInputFieldsContainer fieldsContainer, String fieldName) {
            requireNonNull(fieldsContainer, "fieldsContainer");
            requireNonNull(fieldName, "fieldName");
            if (blockedFields.contains(coordinates(fieldsContainer.getName(), fieldName))) {
                return null;
            }
            return fieldsContainer.getFieldDefinition(fieldName);
        }
    }
}
