package com.majtom.oas.rules.minor;

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
 * Regel: Erkennt Änderungen am readOnly-Flag (Breaking Change).
 * Wenn readOnly von true auf false geändert wird, könnte das ein Breaking Change sein.
 */
@Component
public class PropertyReadOnlyChangedRule implements BreakingChangeRule {

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

            checkReadOnlyFlags(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkReadOnlyFlags(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                Boolean oldReadOnly = oldPropSchema.getReadOnly();
                Boolean newReadOnly = newPropSchema.getReadOnly();

                // readOnly von true auf false geändert (Property kann nun auch in Requests gesendet werden)
                if (Boolean.TRUE.equals(oldReadOnly) && !Boolean.TRUE.equals(newReadOnly)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_READ_ONLY_CHANGED)
                            .severity(ChangeSeverity.MINOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property readOnly-Flag von true auf false geändert")
                            .oldValue("readOnly: true")
                            .newValue("readOnly: false")
                            .isBreakingChange(true)
                            .build());
                }
                // readOnly von false auf true geändert (Property darf nicht mehr in Requests gesendet werden)
                else if (!Boolean.TRUE.equals(oldReadOnly) && Boolean.TRUE.equals(newReadOnly)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_READ_ONLY_CHANGED)
                            .severity(ChangeSeverity.MINOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property readOnly-Flag von false auf true geändert")
                            .oldValue("readOnly: false")
                            .newValue("readOnly: true")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Property ReadOnly Changed Rule";
    }
}

