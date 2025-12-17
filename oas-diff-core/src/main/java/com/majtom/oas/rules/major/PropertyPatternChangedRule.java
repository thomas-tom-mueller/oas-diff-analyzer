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
 * Regel: Erkennt geänderte Regex-Patterns auf String-Properties (Breaking Change).
 * Wenn ein Pattern verschärft wird, können bestehende Werte ungültig werden.
 */
@Component
public class PropertyPatternChangedRule implements BreakingChangeRule {

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

        for (Map.Entry<String, Schema> entry : oldProperties.entrySet()) {
            String propName = entry.getKey();
            Schema oldPropSchema = entry.getValue();
            Schema newPropSchema = newProperties.get(propName);

            if (newPropSchema != null && isStringType(oldPropSchema)) {
                String oldPattern = oldPropSchema.getPattern();
                String newPattern = newPropSchema.getPattern();

                // Pattern wurde geändert (beide nicht null und unterschiedlich)
                if (oldPattern != null && newPattern != null && !oldPattern.equals(newPattern)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_PATTERN_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("Regex-Pattern geändert (Validierung verschärft)")
                            .oldValue(oldPattern)
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
        return "Property Pattern Changed Rule";
    }
}

