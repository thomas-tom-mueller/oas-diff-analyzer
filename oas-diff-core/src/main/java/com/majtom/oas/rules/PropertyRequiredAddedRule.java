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

/**
 * Regel: Erkennt Properties, die zu Required wurden (Breaking Change).
 */
@Component
public class PropertyRequiredAddedRule implements BreakingChangeRule {

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

            checkRequiredProperties(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    private void checkRequiredProperties(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        List<String> oldRequired = oldSchema.getRequired() != null ? oldSchema.getRequired() : new ArrayList<>();
        List<String> newRequired = newSchema.getRequired() != null ? newSchema.getRequired() : new ArrayList<>();

        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (newProperties == null) {
            return;
        }

        // Neue Required-Properties hinzugefügt
        for (String propName : newRequired) {
            if (!oldRequired.contains(propName)) {
                // Prüfe ob die Property bereits existierte (optional -> required)
                boolean existedBefore = oldProperties != null && oldProperties.containsKey(propName);

                if (existedBefore) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_REQUIRED_ADDED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName)
                            .description("Property wurde zu Required: " + propName)
                            .oldValue("optional")
                            .newValue("required")
                            .isBreakingChange(true)
                            .build());
                } else {
                    // Komplett neue Required-Property
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_REQUIRED_ADDED)
                            .severity(ChangeSeverity.CRITICAL)
                            .path("Schema: " + schemaName)
                            .description("Neue Required-Property hinzugefügt: " + propName)
                            .oldValue(null)
                            .newValue(propName)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Property Required Added Rule";
    }
}

