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
 * Regel: Erkennt erhöhte minItems-Constraints für Arrays (Breaking Change).
 * Wenn minItems erhöht wird, können Arrays mit weniger Elementen ungültig werden.
 */
@Component
public class ArrayMinItemsIncreasedRule implements BreakingChangeRule {

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
                Integer oldMinItems = oldPropSchema.getMinItems();
                Integer newMinItems = newPropSchema.getMinItems();

                // minItems wurde erhöht
                if (hasIncreased(oldMinItems, newMinItems)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.ARRAY_MIN_ITEMS_INCREASED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("minItems erhöht (Validierung verschärft)")
                            .oldValue(String.valueOf(oldMinItems != null ? oldMinItems : 0))
                            .newValue(String.valueOf(newMinItems))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean hasIncreased(Integer oldValue, Integer newValue) {
        if (newValue == null) {
            return false; // Constraint entfernt = weniger restriktiv
        }
        int oldVal = oldValue != null ? oldValue : 0;
        return newValue > oldVal;
    }

    @SuppressWarnings("rawtypes")
    private boolean isArrayType(Schema schema) {
        return "array".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Array MinItems Increased Rule";
    }
}

