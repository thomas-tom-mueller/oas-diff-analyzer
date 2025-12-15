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
 * Regel: Erkennt verringerte maxLength-Constraints (Breaking Change).
 * Wenn maxLength verringert wird, können längere Werte ungültig werden.
 */
@Component
public class PropertyMaxLengthDecreasedRule implements BreakingChangeRule {

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

            checkMaxLengthConstraints(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkMaxLengthConstraints(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
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
                Integer oldMaxLength = oldPropSchema.getMaxLength();
                Integer newMaxLength = newPropSchema.getMaxLength();

                // maxLength wurde verringert
                if (hasDecreased(oldMaxLength, newMaxLength)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_MAX_LENGTH_DECREASED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("maxLength verringert (Validierung verschärft)")
                            .oldValue(String.valueOf(oldMaxLength))
                            .newValue(String.valueOf(newMaxLength))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean hasDecreased(Integer oldValue, Integer newValue) {
        if (oldValue == null) {
            return false; // War unbegrenzt, kann nur verschärft werden wenn newValue gesetzt wird
        }
        if (newValue == null) {
            return false; // Wird unbegrenzt = weniger restriktiv
        }
        return newValue < oldValue;
    }

    @SuppressWarnings("rawtypes")
    private boolean isStringType(Schema schema) {
        return "string".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Property MaxLength Decreased Rule";
    }
}

