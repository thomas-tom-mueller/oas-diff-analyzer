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
 * Regel: Erkennt Änderungen am writeOnly-Flag (Breaking Change).
 * Wenn writeOnly von true auf false geändert wird, könnte das ein Breaking Change sein.
 */
@Component
public class PropertyWriteOnlyChangedRule implements BreakingChangeRule {

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

            checkWriteOnlyFlags(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkWriteOnlyFlags(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                Boolean oldWriteOnly = oldPropSchema.getWriteOnly();
                Boolean newWriteOnly = newPropSchema.getWriteOnly();

                // writeOnly von true auf false geändert (Property wird nun auch in Responses zurückgegeben)
                if (Boolean.TRUE.equals(oldWriteOnly) && !Boolean.TRUE.equals(newWriteOnly)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_WRITE_ONLY_CHANGED)
                            .severity(ChangeSeverity.MINOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property writeOnly-Flag von true auf false geändert")
                            .oldValue("writeOnly: true")
                            .newValue("writeOnly: false")
                            .isBreakingChange(true)
                            .build());
                }
                // writeOnly von false auf true geändert (Property wird nicht mehr in Responses zurückgegeben)
                else if (!Boolean.TRUE.equals(oldWriteOnly) && Boolean.TRUE.equals(newWriteOnly)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_WRITE_ONLY_CHANGED)
                            .severity(ChangeSeverity.MINOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Property writeOnly-Flag von false auf true geändert")
                            .oldValue("writeOnly: false")
                            .newValue("writeOnly: true")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Property WriteOnly Changed Rule";
    }
}

