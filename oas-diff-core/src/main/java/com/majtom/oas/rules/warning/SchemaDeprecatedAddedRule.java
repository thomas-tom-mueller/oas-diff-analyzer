package com.majtom.oas.rules.warning;

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
 * Regel: Erkennt wenn ein Schema als deprecated markiert wurde (Warnung).
 * Signalisiert zukünftige Breaking Changes - Schema wird in Zukunft entfernt.
 */
@Component
public class SchemaDeprecatedAddedRule implements BreakingChangeRule {

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

            checkSchemaDeprecation(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkSchemaDeprecation(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        boolean wasDeprecated = Boolean.TRUE.equals(oldSchema.getDeprecated());
        boolean isDeprecated = Boolean.TRUE.equals(newSchema.getDeprecated());

        // Schema wurde als deprecated markiert
        if (!wasDeprecated && isDeprecated) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.SCHEMA_DEPRECATED_ADDED)
                    .severity(ChangeSeverity.WARNING)
                    .path("Schema: " + schemaName)
                    .description("Schema als deprecated markiert (wird in Zukunft entfernt)")
                    .oldValue("nicht deprecated")
                    .newValue("deprecated: true")
                    .isBreakingChange(false)
                    .build());
        }
    }

    @Override
    public String getRuleName() {
        return "Schema Deprecated Added Rule";
    }
}

