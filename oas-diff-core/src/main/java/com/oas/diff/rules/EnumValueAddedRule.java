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
 * Regel: Erkennt hinzugefügte Enum-Werte (Non-Breaking Change).
 */
@Component
public class EnumValueAddedRule implements BreakingChangeRule {

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

        for (Object newValue : newEnums) {
            if (!oldEnums.contains(newValue)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.ENUM_VALUE_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path("Schema: " + schemaName)
                        .description("Enum-Wert hinzugefügt")
                        .oldValue(null)
                        .newValue(String.valueOf(newValue))
                        .isBreakingChange(false)
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

        for (Map.Entry<String, Schema> entry : newProperties.entrySet()) {
            String propName = entry.getKey();
            Schema newPropSchema = entry.getValue();
            Schema oldPropSchema = oldProperties.get(propName);

            if (oldPropSchema != null) {
                List<Object> oldEnums = oldPropSchema.getEnum();
                List<Object> newEnums = newPropSchema.getEnum();

                if (oldEnums != null && newEnums != null) {
                    for (Object newValue : newEnums) {
                        if (!oldEnums.contains(newValue)) {
                            changes.add(ApiChange.builder()
                                    .type(ChangeType.ENUM_VALUE_ADDED)
                                    .severity(ChangeSeverity.INFO)
                                    .path("Schema: " + schemaName + "." + propName)
                                    .description("Enum-Wert hinzugefügt")
                                    .oldValue(null)
                                    .newValue(String.valueOf(newValue))
                                    .isBreakingChange(false)
                                    .build());
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Enum Value Added Rule";
    }
}

