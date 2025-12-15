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
 * Regel: Erkennt Änderungen am Typ der additionalProperties (Breaking Change).
 * Wenn der Typ der zusätzlichen Properties geändert wird, können bestehende Werte ungültig werden.
 */
@Component
public class AdditionalPropertiesTypeChangedRule implements BreakingChangeRule {

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

            checkAdditionalPropertiesType(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkAdditionalPropertiesType(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Object oldAdditionalProperties = oldSchema.getAdditionalProperties();
        Object newAdditionalProperties = newSchema.getAdditionalProperties();

        // Beide müssen Schema-Objekte sein
        if (!(oldAdditionalProperties instanceof Schema) || !(newAdditionalProperties instanceof Schema)) {
            return;
        }

        Schema oldAdditionalSchema = (Schema) oldAdditionalProperties;
        Schema newAdditionalSchema = (Schema) newAdditionalProperties;

        String oldType = getSchemaType(oldAdditionalSchema);
        String newType = getSchemaType(newAdditionalSchema);

        if (!oldType.equals(newType)) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.ADDITIONAL_PROPERTIES_TYPE_CHANGED)
                    .severity(ChangeSeverity.MAJOR)
                    .path("Schema: " + schemaName)
                    .description("additionalProperties Typ geändert")
                    .oldValue(oldType)
                    .newValue(newType)
                    .isBreakingChange(true)
                    .build());
        }
    }

    @SuppressWarnings("rawtypes")
    private String getSchemaType(Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref();
        }
        if (schema.getType() != null) {
            String type = schema.getType();
            String format = schema.getFormat();
            return type + (format != null ? "(" + format + ")" : "");
        }
        return "unknown";
    }

    @Override
    public String getRuleName() {
        return "Additional Properties Type Changed Rule";
    }
}

