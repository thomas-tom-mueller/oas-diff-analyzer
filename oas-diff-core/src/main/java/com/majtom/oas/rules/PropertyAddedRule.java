package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt hinzugefügte Properties in Schemas (Non-Breaking Change).
 */
@Component
public class PropertyAddedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getComponents() == null || oldSpec.getComponents().getSchemas() == null ||
            newSpec.getComponents() == null || newSpec.getComponents().getSchemas() == null) {
            return changes;
        }

        Map<String, Schema> oldSchemas = oldSpec.getComponents().getSchemas();
        Map<String, Schema> newSchemas = newSpec.getComponents().getSchemas();

        for (Map.Entry<String, Schema> entry : newSchemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema newSchema = entry.getValue();
            Schema oldSchema = oldSchemas.get(schemaName);

            if (oldSchema == null) {
                continue;
            }

            checkSchemaProperties(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    private void checkSchemaProperties(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        Set<String> oldPropNames = oldProperties.keySet();
        Set<String> newPropNames = newProperties.keySet();

        for (String propName : newPropNames) {
            if (!oldPropNames.contains(propName)) {
                // Prüfe ob Required (wird in PropertyRequiredAddedRule behandelt)
                boolean isRequired = isPropertyRequired(newSchema, propName);

                if (!isRequired) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_ADDED)
                            .severity(ChangeSeverity.INFO)
                            .path("Schema: " + schemaName)
                            .description("Optionale Property hinzugefügt: " + propName)
                            .oldValue(null)
                            .newValue(propName)
                            .isBreakingChange(false)
                            .build());
                }
            }
        }
    }

    private boolean isPropertyRequired(Schema schema, String propertyName) {
        List<String> required = schema.getRequired();
        return required != null && required.contains(propertyName);
    }

    @Override
    public String getRuleName() {
        return "Property Added Rule";
    }
}

