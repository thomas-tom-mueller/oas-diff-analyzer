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
 * Regel: Erkennt wenn additionalProperties verboten werden (Breaking Change).
 * Wenn additionalProperties von true/schema auf false geändert wird, können zusätzliche Properties nicht mehr übergeben werden.
 */
@Component
public class AdditionalPropertiesForbiddenRule implements BreakingChangeRule {

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

            checkAdditionalProperties(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkAdditionalProperties(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Object oldAdditionalProperties = oldSchema.getAdditionalProperties();
        Object newAdditionalProperties = newSchema.getAdditionalProperties();

        boolean oldAllowed = isAdditionalPropertiesAllowed(oldAdditionalProperties);
        boolean newAllowed = isAdditionalPropertiesAllowed(newAdditionalProperties);

        // additionalProperties wurde von erlaubt auf verboten geändert
        if (oldAllowed && !newAllowed) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.ADDITIONAL_PROPERTIES_FORBIDDEN)
                    .severity(ChangeSeverity.MAJOR)
                    .path("Schema: " + schemaName)
                    .description("additionalProperties verboten (zusätzliche Properties nicht mehr erlaubt)")
                    .oldValue(getAdditionalPropertiesDescription(oldAdditionalProperties))
                    .newValue("false")
                    .isBreakingChange(true)
                    .build());
        }
    }

    private boolean isAdditionalPropertiesAllowed(Object additionalProperties) {
        if (additionalProperties == null) {
            // Default ist true in OpenAPI 3.0
            return true;
        }
        if (additionalProperties instanceof Boolean) {
            return (Boolean) additionalProperties;
        }
        // Wenn es ein Schema-Objekt ist, sind zusätzliche Properties erlaubt
        return true;
    }

    private String getAdditionalPropertiesDescription(Object additionalProperties) {
        if (additionalProperties == null) {
            return "true (default)";
        }
        if (additionalProperties instanceof Boolean) {
            return String.valueOf(additionalProperties);
        }
        return "schema definiert";
    }

    @Override
    public String getRuleName() {
        return "Additional Properties Forbidden Rule";
    }
}

