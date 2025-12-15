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
 * Regel: Erkennt entfernte Enum-Werte (Breaking Change).
 */
@Component
public class EnumValueRemovedRule implements BreakingChangeRule {

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

            checkSchemaEnums(schemaName, oldSchema, newSchema, changes);
            checkPropertyEnums(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    private void checkSchemaEnums(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        List<Object> oldEnums = oldSchema.getEnum();
        List<Object> newEnums = newSchema.getEnum();

        if (oldEnums == null || newEnums == null) {
            return;
        }

        for (Object oldValue : oldEnums) {
            if (!newEnums.contains(oldValue)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.ENUM_VALUE_REMOVED)
                        .severity(ChangeSeverity.MAJOR)
                        .path("Schema: " + schemaName)
                        .description("Enum-Wert entfernt")
                        .oldValue(String.valueOf(oldValue))
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    private void checkPropertyEnums(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                List<Object> oldEnums = oldPropSchema.getEnum();
                List<Object> newEnums = newPropSchema.getEnum();

                if (oldEnums != null && newEnums != null) {
                    for (Object oldValue : oldEnums) {
                        if (!newEnums.contains(oldValue)) {
                            changes.add(ApiChange.builder()
                                    .type(ChangeType.ENUM_VALUE_REMOVED)
                                    .severity(ChangeSeverity.MAJOR)
                                    .path("Schema: " + schemaName + "." + propName)
                                    .description("Enum-Wert entfernt")
                                    .oldValue(String.valueOf(oldValue))
                                    .newValue(null)
                                    .isBreakingChange(true)
                                    .build());
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Enum Value Removed Rule";
    }
}

