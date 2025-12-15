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
 * Regel: Erkennt hinzugefügte Regex-Patterns auf String-Properties (Breaking Change).
 * Wenn ein Pattern hinzugefügt wird, müssen bestehende Werte nun validiert werden.
 */
@Component
public class PropertyPatternAddedRule implements BreakingChangeRule {

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

            checkPropertyPatterns(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkPropertyPatterns(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : newProperties.entrySet()) {
            String propName = entry.getKey();
            Schema newPropSchema = entry.getValue();
            Schema oldPropSchema = oldProperties.get(propName);

            if (oldPropSchema != null && isStringType(newPropSchema)) {
                String oldPattern = oldPropSchema.getPattern();
                String newPattern = newPropSchema.getPattern();

                // Pattern wurde hinzugefügt
                if (oldPattern == null && newPattern != null) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_PATTERN_ADDED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Regex-Pattern hinzugefügt (Validierung verschärft)")
                            .oldValue("kein Pattern")
                            .newValue(newPattern)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean isStringType(Schema schema) {
        return "string".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Property Pattern Added Rule";
    }
}

