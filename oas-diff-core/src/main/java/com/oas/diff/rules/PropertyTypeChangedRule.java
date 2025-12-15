package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Property-Typ (Breaking Change).
 */
@Component
public class PropertyTypeChangedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getComponents() == null || oldSpec.getComponents().getSchemas() == null ||
            newSpec.getComponents() == null || newSpec.getComponents().getSchemas() == null) {
            return changes;
        }

        Map<String, Schema> oldSchemas = oldSpec.getComponents().getSchemas();
        Map<String, Schema> newSchemas = newSpec.getComponents().getSchemas();

        for (Map.Entry<String, Schema> entry : oldSchemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema oldSchema = entry.getValue();
            Schema newSchema = newSchemas.get(schemaName);

            if (newSchema == null) {
                continue;
            }

            checkPropertyTypes(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    private void checkPropertyTypes(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : oldProperties.entrySet()) {
            String propName = entry.getKey();
            Schema oldPropSchema = entry.getValue();
            Schema newPropSchema = newProperties.get(propName);

            if (newPropSchema != null) {
                String oldType = getSchemaType(oldPropSchema);
                String newType = getSchemaType(newPropSchema);

                if (!oldType.equals(newType)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_TYPE_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property-Typ geändert")
                            .oldValue(oldType)
                            .newValue(newType)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private String getSchemaType(Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref();
        }
        if (schema.getType() != null) {
            String type = schema.getType();
            String format = schema.getFormat();
            return type + (format != null ? "(" + format + ")" : "");
        }
        return "unknown";
    }

    @Override
    public String getRuleName() {
        return "Property Type Changed Rule";
    }
}

