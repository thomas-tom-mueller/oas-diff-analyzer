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
 * Regel: Erkennt verringerte maxItems-Constraints für Arrays (Breaking Change).
 * Wenn maxItems verringert wird, können Arrays mit mehr Elementen ungültig werden.
 */
@Component
public class ArrayMaxItemsDecreasedRule implements BreakingChangeRule {

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

            checkArrayConstraints(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkArrayConstraints(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : oldProperties.entrySet()) {
            String propName = entry.getKey();
            Schema oldPropSchema = entry.getValue();
            Schema newPropSchema = newProperties.get(propName);

            if (newPropSchema != null && isArrayType(oldPropSchema)) {
                Integer oldMaxItems = oldPropSchema.getMaxItems();
                Integer newMaxItems = newPropSchema.getMaxItems();

                // maxItems wurde verringert
                if (hasDecreased(oldMaxItems, newMaxItems)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.ARRAY_MAX_ITEMS_DECREASED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("maxItems verringert (Validierung verschärft)")
                            .oldValue(String.valueOf(oldMaxItems))
                            .newValue(String.valueOf(newMaxItems))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean hasDecreased(Integer oldValue, Integer newValue) {
        if (oldValue == null) {
            return false; // War unbegrenzt
        }
        if (newValue == null) {
            return false; // Wird unbegrenzt = weniger restriktiv
        }
        return newValue < oldValue;
    }

    @SuppressWarnings("rawtypes")
    private boolean isArrayType(Schema schema) {
        return "array".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Array MaxItems Decreased Rule";
    }
}

