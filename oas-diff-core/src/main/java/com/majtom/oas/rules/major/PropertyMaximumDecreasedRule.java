package com.majtom.oas.rules.major;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt verringerte maximum-Constraints (Breaking Change).
 * Wenn maximum verringert wird, können größere Werte ungültig werden.
 */
@Component
public class PropertyMaximumDecreasedRule implements BreakingChangeRule {

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

            checkMaximumConstraints(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkMaximumConstraints(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : oldProperties.entrySet()) {
            String propName = entry.getKey();
            Schema oldPropSchema = entry.getValue();
            Schema newPropSchema = newProperties.get(propName);

            if (newPropSchema != null && isNumericType(oldPropSchema)) {
                BigDecimal oldMaximum = oldPropSchema.getMaximum();
                BigDecimal newMaximum = newPropSchema.getMaximum();

                // maximum wurde verringert
                if (hasDecreased(oldMaximum, newMaximum)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_MAXIMUM_DECREASED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("maximum-Wert verringert (Validierung verschärft)")
                            .oldValue(oldMaximum.toString())
                            .newValue(newMaximum != null ? newMaximum.toString() : "keine Grenze")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean hasDecreased(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null) {
            return false; // War unbegrenzt
        }
        if (newValue == null) {
            return false; // Wird unbegrenzt = weniger restriktiv
        }
        return newValue.compareTo(oldValue) < 0;
    }

    @SuppressWarnings("rawtypes")
    private boolean isNumericType(Schema schema) {
        String type = schema.getType();
        return "number".equals(type) || "integer".equals(type);
    }

    @Override
    public String getRuleName() {
        return "Property Maximum Decreased Rule";
    }
}

