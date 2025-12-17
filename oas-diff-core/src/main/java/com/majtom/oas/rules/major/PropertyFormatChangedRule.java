package com.majtom.oas.rules.major;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Property-Format (Breaking Change).
 * Z.B. date → date-time
 */
@Component
public class PropertyFormatChangedRule implements BreakingChangeRule {

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

            checkPropertyFormats(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkPropertyFormats(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                String oldFormat = oldPropSchema.getFormat();
                String newFormat = newPropSchema.getFormat();

                if (oldFormat != null && newFormat != null && !oldFormat.equals(newFormat)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_FORMAT_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property-Format geändert")
                            .oldValue(oldFormat)
                            .newValue(newFormat)
                            .isBreakingChange(true)
                            .build());
                } else if (oldFormat != null && newFormat == null) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_FORMAT_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property-Format entfernt")
                            .oldValue(oldFormat)
                            .newValue("kein Format")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Property Format Changed Rule";
    }
}

