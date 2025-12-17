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
 * Regel: Erkennt geänderte Default-Werte (Breaking Change).
 * Wenn der Default-Wert geändert wird, ändert sich das Verhalten für Clients.
 */
@Component
public class DefaultValueChangedRule implements BreakingChangeRule {

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

            checkDefaultValues(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkDefaultValues(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                Object oldDefault = oldPropSchema.getDefault();
                Object newDefault = newPropSchema.getDefault();

                // Default-Wert wurde geändert
                if (oldDefault != null && newDefault != null && !oldDefault.equals(newDefault)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.DEFAULT_VALUE_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Default-Wert geändert (Verhaltensänderung)")
                            .oldValue(String.valueOf(oldDefault))
                            .newValue(String.valueOf(newDefault))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Default Value Changed Rule";
    }
}

